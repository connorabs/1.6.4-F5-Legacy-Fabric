package dev.example.omnilook.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.ControllablePlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accessor mixin exposing two MinecraftClient members regardless of their
 * actual Java visibility (public/private) - this sidesteps needing to
 * know or guess the access modifier, which tiny-format mapping files
 * don't encode. @Accessor works against private fields too, generating
 * the necessary bridge at compile time.
 * <p>
 * field_3805 (the local player) has no human-readable "named" mapping in
 * this project's mappings build - only the raw intermediary name exists -
 * so we reference it that way here too; Mixin resolves it correctly
 * either way. Its declared field type is ControllablePlayerEntity.
 * currentScreen (field_3816) does have a named mapping, and was very
 * likely already public going by other Minecraft mapping generations,
 * but routing both through one accessor mixin means we don't have to
 * treat them differently or take that on faith.
 */
@Mixin(MinecraftClient.class)
public interface MinecraftClientAccessor {

    @Accessor("field_3805")
    ControllablePlayerEntity omnilook$getPlayer();

    @Accessor("currentScreen")
    Screen omnilook$getCurrentScreen();
}
