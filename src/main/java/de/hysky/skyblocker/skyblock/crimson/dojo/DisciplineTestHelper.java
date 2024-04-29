package de.hysky.skyblocker.skyblock.crimson.dojo;

import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import java.awt.*;
import java.util.HashMap;

public class DisciplineTestHelper {
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

    private static final HashMap<String, String> SWORD_TO_NAME_LOOKUP = Util.make(new HashMap<>(), map -> {
        map.put("WOOD_SWORD","Wood");
        map.put("IRON_SWORD","Iron");
        map.put("GOLD_SWORD","Gold");
        map.put("DIAMOND_SWORD","Diamond");
    });
    private static final HashMap<String, Integer> SWORD_TO_COLOR_LOOKUP = Util.make(new HashMap<>(), map -> {
        map.put("WOOD_SWORD",0xa52a2a);
        map.put("IRON_SWORD",0xc0c0c0);
        map.put("GOLD_SWORD",0xffd700);
        map.put("DIAMOND_SWORD",0x00ffff);
    });

    public static boolean isCorrect(String name) {
        if (DojoManager.currentChallenge != DojoManager.DojoChallenges.DISCIPLINE || CLIENT == null || CLIENT.player == null) {
            return false;
        }
        String heldId = ItemTooltip.getInternalNameFromNBT(CLIENT.player.getMainHandStack(), true);
        if (SWORD_TO_NAME_LOOKUP.containsKey(heldId)) {

            return SWORD_TO_NAME_LOOKUP.get(heldId).equals(name);
        }
        return false;
    }

    public static int getColor() {
        if (DojoManager.currentChallenge != DojoManager.DojoChallenges.DISCIPLINE || CLIENT == null || CLIENT.player == null) {
            return 0;
        }
        String heldId = ItemTooltip.getInternalNameFromNBT(CLIENT.player.getMainHandStack(), true);
        if (SWORD_TO_COLOR_LOOKUP.containsKey(heldId)) {
            return SWORD_TO_COLOR_LOOKUP.get(heldId);
        }
        return 0;
    }
}
