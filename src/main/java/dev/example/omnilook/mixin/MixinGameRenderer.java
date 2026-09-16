package dev.example.omnilook.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;

import dev.example.omnilook.OmnilookLiteClient;
import dev.example.omnilook.OmnilookState;

import net.minecraft.client.render.GameRenderer;

import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {


    /*
     * ============================================================
     * MAIN FRAME HOOK
     * ============================================================
     *
     * Runs our client state/key logic.
     */
    @Inject(
            method = "renderWorld",
            at = @At("HEAD")
    )
    private void omnilook$onRenderWorld(
            float partialTicks,
            long sysTime,
            CallbackInfo ci
    ) {

        OmnilookLiteClient.onFrame(
                partialTicks
        );
    }


    /*
     * ============================================================
     * CAMERA FREELOOK TURN INTERCEPTION
     * ============================================================
     *
     * This follows the Omnilook Legacy Fabric approach.
     *
     * Vanilla:
     *
     * mouse
     *   ↓
     * GameRenderer calculates turn amount
     *   ↓
     * Local player rotation method
     *
     *
     * CAMERA_FREELOOK:
     *
     * mouse
     *   ↓
     * GameRenderer calculates turn amount
     *   ↓
     * WE INTERCEPT IT
     *   ↓
     * camYaw/camPitch change
     *   ↓
     * return FALSE
     *   ↓
     * player does NOT rotate
     */
    @Dynamic
    @WrapWithCondition(
            method = "method_1331(F)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/class_481;method_2534(FF)V"
            )
    )
    private boolean omnilook$interceptCameraTurn(
            @Coerce Object player,
            float yaw,
            float pitch
    ) {

        /*
         * Any time CAMERA_FREELOOK is NOT active:
         *
         * allow Minecraft to execute its normal turn.
         *
         * This means YAW_FOLLOW_LOOK is not modified by this
         * interception.
         */
        if (!OmnilookState.isCameraFreelookActive()) {

            return true;
        }


        /*
         * Omnilook-style independent camera rotation.
         *
         * Original Omnilook effectively performs:
         *
         * updateCamera(-pitch, yaw)
         *
         * with the game's 0.15 rotation multiplier.
         */

        OmnilookState.camPitch +=
                -pitch * 0.15F;


        OmnilookState.camPitch =
                OmnilookState.clampPitch(
                        OmnilookState.camPitch
                );


        OmnilookState.camYaw +=
                yaw * 0.15F;


        /*
         * DO NOT call the actual player rotation method.
         *
         * This is what locks the player.
         */
        return false;
    }


    /*
     * ============================================================
     * REPLACE CAMERA YAW
     * ============================================================
     *
     * transformCamera normally reads the player's yaw.
     *
     * During CAMERA_FREELOOK we replace those reads with camYaw.
     */
    @Dynamic
    @ModifyExpressionValue(
            method = "transformCamera",
            at = {

                    @At(
                            value = "FIELD",
                            target = "Lnet/minecraft/entity/Entity;yaw:F"
                    ),

                    @At(
                            value = "FIELD",
                            target = "Lnet/minecraft/entity/Entity;prevYaw:F"
                    ),

                    /*
                     * Legacy/intermediary field alternatives.
                     */
                    @At(
                            value = "FIELD",
                            target = "Lnet/minecraft/class_1699;field_3258:F"
                    ),

                    @At(
                            value = "FIELD",
                            target = "Lnet/minecraft/class_1699;field_3194:F"
                    )
            }
    )
    private float omnilook$replaceCameraYaw(
            float originalYaw
    ) {

        if (OmnilookState.isCameraFreelookActive()) {

            return OmnilookState.camYaw;
        }


        return originalYaw;
    }


    /*
     * ============================================================
     * REPLACE CAMERA PITCH
     * ============================================================
     */
    @Dynamic
    @ModifyExpressionValue(
            method = "transformCamera",
            at = {

                    @At(
                            value = "FIELD",
                            target = "Lnet/minecraft/entity/Entity;pitch:F"
                    ),

                    @At(
                            value = "FIELD",
                            target = "Lnet/minecraft/entity/Entity;prevPitch:F"
                    ),

                    /*
                     * Legacy/intermediary field alternatives.
                     */
                    @At(
                            value = "FIELD",
                            target = "Lnet/minecraft/class_1699;field_3193:F"
                    ),

                    @At(
                            value = "FIELD",
                            target = "Lnet/minecraft/class_1699;field_3195:F"
                    )
            }
    )
    private float omnilook$replaceCameraPitch(
            float originalPitch
    ) {

        if (OmnilookState.isCameraFreelookActive()) {

            return OmnilookState.camPitch;
        }


        return originalPitch;
    }
}