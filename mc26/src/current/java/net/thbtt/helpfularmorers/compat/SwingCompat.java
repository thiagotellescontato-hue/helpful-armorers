package net.thbtt.helpfularmorers.compat;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.component.SwingAnimation;

public final class SwingCompat {
    private SwingCompat() {
    }

    public static void swingMainHand(Villager villager) {
        villager.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT);
    }
}
