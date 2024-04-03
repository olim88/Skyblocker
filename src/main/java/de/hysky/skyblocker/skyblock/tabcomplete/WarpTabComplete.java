package de.hysky.skyblocker.skyblock.tabcomplete;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.NEURepoManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class WarpTabComplete {

    private static final List<String> warps = new ArrayList<>();

    public static void init() {
        NEURepoManager.runAsyncAfterLoad(WarpTabComplete::LoadWarps);
    }

    public static void LoadWarps() {
        NEURepoManager.NEU_REPO.getConstants().getIslands().getWarps().forEach(warp -> warps.add(warp.getWarp()));

        if (SkyblockerConfigManager.get().general.enableWarpTabComplete) {
            ClientCommandRegistrationCallback.EVENT.register(WarpTabComplete::registerWarpCommands);
        }
    }

    private static void registerWarpCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        warpSuggestionsProvider suggestions = new warpSuggestionsProvider();
        dispatcher.register(literal("warp").then(argument("location", StringArgumentType.word()).suggests(suggestions)));



    }
    public static class warpSuggestionsProvider implements SuggestionProvider<FabricClientCommandSource> {
        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) {
            // Add all the warp locations
            for (String warp : warps) {
                builder.suggest(warp);
            }
            // Lock the suggestions after we've modified them.
            return builder.buildFuture();
        }
    }

}

