package io.github.sakurawald.module.mixin.command_rewrite;

import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.core.structure.RegexRewriteNode;
import io.github.sakurawald.module.initializer.command_rewrite.CommandRewriteInitializer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = ServerPlayNetworkHandler.class, priority = 1000 - 500)
public class ServerPlayNetworkHandlerMixin {

    @ModifyVariable(method = "executeCommand", at = @At(value = "HEAD"), ordinal = 0, argsOnly = true)
    public String interceptPacketsOfIssuedCommand(@NotNull String string) {
        for (RegexRewriteNode entry : CommandRewriteInitializer.config.model().rewrite) {
            if (entry.getRegex() == null || entry.getReplacement() == null) {
                LogUtil.warn("there is an invalid `null` entry in `command_rewrite.regex`, you should remove it: {}", entry);
                continue;
            }

            if (string.matches(entry.getRegex())) {
                LogUtil.debug("rewrite the command: /{}", string);
                return string.replaceAll(entry.getRegex(), entry.getReplacement());
            }
        }

        return string;
    }
}
