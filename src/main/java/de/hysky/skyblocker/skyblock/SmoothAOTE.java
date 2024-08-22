package de.hysky.skyblocker.skyblock;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.ItemUtils;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class SmoothAOTE {

    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

    private static final long maxTeleportTime = 1000;

    private static long startTime;
    private static Vec3d startPos;
    private static Vec3d teleportVector;
    private static long lastPing;

    public static void init() {
        UseItemCallback.EVENT.register(SmoothAOTE::onItemInteract);
    }

    public static void reset() {
        //reset when player has reached the end of the teleport
        startPos = null;
        teleportVector = null;
    }

    private static int extractTeleporterCustomData(NbtCompound customData, int baseRange) {
        return customData != null && customData.contains("tuned_transmission") ? baseRange + customData.getInt("tuned_transmission") : baseRange;
    }


    private static TypedActionResult<ItemStack> onItemInteract(PlayerEntity playerEntity, World world, Hand hand) {
        //todo add manna check
        //stop checking if player does not exist
        if (CLIENT.player == null) {
            return null;
        }
        //get return item
        ItemStack stack = CLIENT.player.getStackInHand(hand);
        if (!SkyblockerConfigManager.get().uiAndVisuals.smoothAOTE.enabled) {
            return TypedActionResult.pass(stack);
        }

        //work out if the player is holding a teleporting item that is enabled and if so how far the item will take them
        ItemStack heldItem = CLIENT.player.getMainHandStack();
        String itemId = heldItem.getSkyblockId();
        NbtCompound customData = ItemUtils.getCustomData(heldItem);
        int distance = 0;
        switch (itemId) {
            case "ASPECT_OF_THE_LEECH_1" -> {
                if (SkyblockerConfigManager.get().uiAndVisuals.smoothAOTE.enableWeirdTransmission) {
                    distance = 3;
                } else {
                    return TypedActionResult.pass(stack);
                }
            }
            case "ASPECT_OF_THE_LEECH_2" -> {
                if (SkyblockerConfigManager.get().uiAndVisuals.smoothAOTE.enableWeirdTransmission) {
                    distance = 4;
                } else {
                    return TypedActionResult.pass(stack);
                }
            }
            case "ASPECT_OF_THE_END", "ASPECT_OF_THE_VOID" -> {
                if (SkyblockerConfigManager.get().uiAndVisuals.smoothAOTE.enableEtherTransmission && CLIENT.options.sneakKey.isPressed() && customData.getInt("ethermerge") == 1) {
                    distance = extractTeleporterCustomData(customData, 57);
                } else if (SkyblockerConfigManager.get().uiAndVisuals.smoothAOTE.enableInstantTransmission) {
                    distance = extractTeleporterCustomData(customData, 8);
                } else {
                    return TypedActionResult.pass(stack);
                }
            }
            case "ETHERWARP_CONDUIT" -> {
                if (SkyblockerConfigManager.get().uiAndVisuals.smoothAOTE.enableEtherTransmission) {
                    distance = extractTeleporterCustomData(customData, 57);
                } else {
                    return TypedActionResult.pass(stack);
                }
            }
            case "SINSEEKER_SCYTHE" -> {
                if (SkyblockerConfigManager.get().uiAndVisuals.smoothAOTE.enableSinrecallTransmission) {
                    distance = extractTeleporterCustomData(customData, 4);
                } else {
                    return TypedActionResult.pass(stack);
                }
            }
            case "NECRON_BLADE", "ASTRAEA", "HYPERION", "SCYLLA", "VALKYRIE" -> {
                if (SkyblockerConfigManager.get().uiAndVisuals.smoothAOTE.enableWitherImpact) {
                    distance = 10;
                } else {
                    return TypedActionResult.pass(stack);
                }
            }
            default -> {
                return TypedActionResult.pass(stack);
            }
        }


        //work out start pos of warp and set start time. if there is an active warp going on make the end of that the start of the next one
        if (startPos == null || teleportVector == null) {
            startPos = CLIENT.player.getEyePos();
        } else {
            startPos = startPos.add(teleportVector);
        }
        startTime = System.currentTimeMillis();

        // calculate the vector the player will follow for the teleport
        //get direction
        float pitch = CLIENT.player.getPitch();
        float yaw = CLIENT.player.getYaw();
        Vec3d look = CLIENT.player.getRotationVector(pitch, yaw);
        //find target location depending on how far the item they are using takes them
        teleportVector = look.multiply(distance);
        //compensate for pixel rounding the end position to x.5 y.62 z.5
        Vec3d predictedEnd = startPos.add(teleportVector);
        Vec3d offset = new Vec3d(predictedEnd.x - (Math.floor(predictedEnd.x) + 0.5), predictedEnd.y - (Math.ceil(predictedEnd.y) + 0.62), predictedEnd.z - (Math.floor(predictedEnd.z) + 0.5));
        teleportVector = teleportVector.subtract(offset);

        return TypedActionResult.pass(stack);
    }

    public static Vec3d getInterpolatedPos() {
        if (CLIENT.player == null || teleportVector == null || startPos == null) {
            return null;
        }
        long gap = System.currentTimeMillis() - startTime;
        //if teleport has taken over max time reset and return null
        if (gap > maxTeleportTime) {
            reset();
            return null;
        }
        double percentage = Math.min((double) (gap) / Math.min(lastPing, maxTeleportTime), 1);

        return startPos.add(teleportVector.multiply(percentage));
    }

    public static void updatePing(long ping) {
        lastPing = ping;
    }
}
