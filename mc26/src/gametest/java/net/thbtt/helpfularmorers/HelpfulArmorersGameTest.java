package net.thbtt.helpfularmorers;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.fabricmc.fabric.api.gametest.v1.CustomTestMethodInvoker;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public final class HelpfulArmorersGameTest implements CustomTestMethodInvoker {
    @GameTest
    public void armorerRepairsDamagedIronGolem(GameTestHelper helper) throws ReflectiveOperationException {
        helper.setTime(1000L);

        Villager armorer = helper.spawn(EntityTypes.VILLAGER, 1, 1, 1);
        armorer.setNoGravity(true);
        armorer.setVillagerData(armorer.getVillagerData().withProfession(
                helper.getLevel().registryAccess(),
                VillagerProfession.ARMORER
        ));

        IronGolem golem = helper.spawn(EntityTypes.IRON_GOLEM, 2, 1, 1);
        golem.setNoAi(true);
        golem.setNoGravity(true);
        float damagedHealth = 1.0F;
        golem.setHealth(damagedHealth);

        helper.assertFalse(helper.getLevel().isDarkOutside(), "GameTest must run during daylight");
        helper.assertTrue(
                armorer.getVillagerData().profession().is(VillagerProfession.ARMORER),
                "Villager is not an armorer"
        );
        helper.assertFalse(armorer.isBaby(), "Villager is a baby");
        helper.assertFalse(armorer.isNoAi(), "Villager AI is disabled");
        helper.assertTrue(armorer.isEffectiveAi(), "Villager AI is not effective");
        helper.assertTrue(golem.isAlive(), "Iron golem is not alive");
        helper.assertTrue(golem.getTarget() == null, "Iron golem has a target");
        helper.assertTrue(
                armorer.distanceToSqr(golem) < 2.4D * 2.4D,
                "Iron golem is outside repair distance"
        );
        helper.assertTrue(
                helper.getLevel().getEntitiesOfClass(
                        IronGolem.class,
                        armorer.getBoundingBox().inflate(16.0D)
                ).contains(golem),
                "Iron golem is not discoverable"
        );
        helper.assertTrue(
                helper.getLevel().getEntitiesOfClass(
                        Monster.class,
                        armorer.getBoundingBox().inflate(10.0D)
                ).isEmpty(),
                "A hostile mob is near the armorer"
        );

        helper.runAfterDelay(1, () -> {
            try {
                armorer.setVillagerData(armorer.getVillagerData().withProfession(
                        helper.getLevel().registryAccess(),
                        VillagerProfession.ARMORER
                ));
                writeIntField(armorer, "ha$scanCooldown", 0);
                writeIntField(armorer, "ha$repairCooldown", 0);
                writeIntField(armorer, "ha$dangerCooldown", 10);
                writeIntField(armorer, "ha$fleeTicks", 0);
                writeFloatField(armorer, "ha$lastHealth", armorer.getHealth());
                writeBooleanField(armorer, "ha$dangerNearby", false);
                writeBooleanField(armorer, "ha$holdingIronIngot", false);
                writeObjectField(armorer, "ha$targetGolemUuid", golem.getUUID());
                armorer.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                invokeRepairTick(armorer);

                helper.assertTrue(
                        armorer.distanceToSqr(golem) < 2.4D * 2.4D,
                        "Iron golem moved outside repair distance"
                );
                helper.assertTrue(
                        golem.getUUID().equals(readObjectField(armorer, "ha$targetGolemUuid")),
                        "Armorer selected a different iron golem: expected " + golem.getUUID()
                                + ", actual " + readObjectField(armorer, "ha$targetGolemUuid")
                                + ", health " + golem.getHealth()
                );
                helper.assertTrue(
                        golem.getHealth() > damagedHealth,
                        "Armorer did not repair the damaged iron golem"
                );
                helper.assertTrue(
                        armorer.getMainHandItem().is(Items.IRON_INGOT),
                        "Armorer did not keep the iron ingot equipped"
                );
                helper.succeed();
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException(exception);
            }
        });
    }

    private static void writeIntField(Villager villager, String name, int value) throws ReflectiveOperationException {
        Field field = villager.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.setInt(villager, value);
    }

    private static void writeFloatField(Villager villager, String name, float value) throws ReflectiveOperationException {
        Field field = villager.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.setFloat(villager, value);
    }

    private static void writeBooleanField(Villager villager, String name, boolean value) throws ReflectiveOperationException {
        Field field = villager.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.setBoolean(villager, value);
    }

    private static void writeObjectField(Villager villager, String name, Object value) throws ReflectiveOperationException {
        Field field = villager.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(villager, value);
    }

    private static Object readObjectField(Villager villager, String name) throws ReflectiveOperationException {
        Field field = villager.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(villager);
    }

    private static void invokeRepairTick(Villager villager) throws ReflectiveOperationException {
        Method method = java.util.Arrays.stream(Villager.class.getDeclaredMethods())
                .filter(candidate -> candidate.getName().contains("tickGolemRepair"))
                .findFirst()
                .orElseThrow(NoSuchMethodException::new);
        method.setAccessible(true);
        method.invoke(villager, new CallbackInfo("customServerAiStep", false));
    }

    @Override
    public void invokeTestMethod(GameTestHelper helper, Method method) throws ReflectiveOperationException {
        method.invoke(this, helper);
    }
}
