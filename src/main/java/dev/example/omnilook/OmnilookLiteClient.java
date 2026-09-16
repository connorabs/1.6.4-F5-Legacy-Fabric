package dev.example.omnilook;

import dev.example.omnilook.mixin.MinecraftClientAccessor;

import net.fabricmc.api.ClientModInitializer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.player.ClientPlayerEntity;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.lang.reflect.Array;

public class OmnilookLiteClient implements ClientModInitializer {

    public static KeyBinding cameraFreelookKey;
    public static KeyBinding yawFollowKey;

    /*
     * Vanilla perspectives:
     *
     * 0 = first person
     * 1 = third person back
     * 2 = third person front
     */
    private static final int PERSPECTIVE_FIRST_PERSON = 0;
    private static final int PERSPECTIVE_THIRD_PERSON_BACK = 1;

    /*
     * Perspective to restore when either Omnilook mode ends.
     */
    private static int perspectiveBeforeLookMode =
            PERSPECTIVE_FIRST_PERSON;

    /*
     * Used for toggle edge detection.
     */
    private static boolean prevCameraDown = false;
    private static boolean prevYawFollowDown = false;

    private static boolean cameraToggledOn = false;
    private static boolean yawFollowToggledOn = false;


    @Override
    public void onInitializeClient() {

        OmnilookConfig.load();

        /*
         * Camera lock / freelook.
         *
         * Default:
         * `
         */
        cameraFreelookKey = new KeyBinding(
                "key.omnilook.cameraFreelook",
                Keyboard.KEY_GRAVE
        );

        /*
         * F5 yaw-follow mode.
         *
         * No default key.
         */
        yawFollowKey = new KeyBinding(
                "key.omnilook.yawFollowLook",
                Keyboard.KEY_NONE
        );

        registerKeyBinding(cameraFreelookKey);
        registerKeyBinding(yawFollowKey);
    }


    private static void registerKeyBinding(KeyBinding binding) {

        GameOptions options =
                MinecraftClient.getInstance().options;

        KeyBinding[] old =
                options.allKeys;

        KeyBinding[] updated =
                (KeyBinding[]) Array.newInstance(
                        KeyBinding.class,
                        old.length + 1
                );

        System.arraycopy(
                old,
                0,
                updated,
                0,
                old.length
        );

        updated[old.length] =
                binding;

        options.allKeys =
                updated;
    }


    private static boolean isDown(
            GameOptions options,
            KeyBinding binding
    ) {

        return options.isPressed(binding);
    }


    /*
     * Called every frame from MixinGameRenderer.
     */
    public static void onFrame(float partialTicks) {

        MinecraftClient mc =
                MinecraftClient.getInstance();

        MinecraftClientAccessor mcAccessor =
                (MinecraftClientAccessor) mc;


        /*
         * =========================================================
         * GET LOCAL PLAYER
         * =========================================================
         */

        Object rawPlayer =
                mcAccessor.omnilook$getPlayer();


        if (!(rawPlayer instanceof ClientPlayerEntity)) {

            if (OmnilookState.isAnyActive()) {

                mc.options.perspective =
                        perspectiveBeforeLookMode;

                OmnilookState.deactivate();
            }

            return;
        }


        ClientPlayerEntity player =
                (ClientPlayerEntity) rawPlayer;


        /*
         * =========================================================
         * GUI / INVENTORY / CHAT
         * =========================================================
         */

        if (mcAccessor.omnilook$getCurrentScreen() != null) {

            if (OmnilookState.isAnyActive()) {

                /*
                 * Restore the perspective we had before
                 * activating either look mode.
                 */
                mc.options.perspective =
                        perspectiveBeforeLookMode;

                OmnilookState.deactivate();
            }


            /*
             * Hold-mode keys need to reset when a GUI opens.
             */
            if (!OmnilookConfig.cameraModeToggle) {
                cameraToggledOn = false;
            }

            if (!OmnilookConfig.yawFollowModeToggle) {
                yawFollowToggledOn = false;
            }


            prevCameraDown = false;
            prevYawFollowDown = false;

            return;
        }


        /*
         * =========================================================
         * KEY STATES
         * =========================================================
         */

        boolean cameraDown =
                isDown(
                        mc.options,
                        cameraFreelookKey
                );

        boolean yawFollowDown =
                isDown(
                        mc.options,
                        yawFollowKey
                );


        boolean cameraPressedEdge =
                cameraDown
                        && !prevCameraDown;

        boolean yawFollowPressedEdge =
                yawFollowDown
                        && !prevYawFollowDown;


        prevCameraDown =
                cameraDown;

        prevYawFollowDown =
                yawFollowDown;


        /*
         * =========================================================
         * CAMERA FREELOOK HOLD / TOGGLE
         * =========================================================
         */

        if (OmnilookConfig.cameraModeToggle) {

            if (cameraPressedEdge) {

                cameraToggledOn =
                        !cameraToggledOn;
            }

        } else {

            cameraToggledOn =
                    cameraDown;
        }


        /*
         * =========================================================
         * F5 YAW FOLLOW HOLD / TOGGLE
         * =========================================================
         */

        if (OmnilookConfig.yawFollowModeToggle) {

            if (yawFollowPressedEdge) {

                yawFollowToggledOn =
                        !yawFollowToggledOn;
            }

        } else {

            yawFollowToggledOn =
                    yawFollowDown;
        }


        /*
         * =========================================================
         * DETERMINE WHICH MODE SHOULD BE ACTIVE
         * =========================================================
         *
         * Yaw-follow gets priority if both keys are active.
         */

        OmnilookState.Mode desired;


        if (yawFollowToggledOn) {

            desired =
                    OmnilookState.Mode.YAW_FOLLOW_LOOK;

        } else if (cameraToggledOn) {

            desired =
                    OmnilookState.Mode.CAMERA_FREELOOK;

        } else {

            desired =
                    OmnilookState.Mode.NONE;
        }


        OmnilookState.Mode current =
                OmnilookState.getActiveMode();


        /*
         * =========================================================
         * MODE CHANGE
         * =========================================================
         */

        if (desired != current) {

            boolean currentlyLooking =
                    current != OmnilookState.Mode.NONE;

            boolean wantsLookMode =
                    desired != OmnilookState.Mode.NONE;


            /*
             * Entering either mode from normal Minecraft.
             *
             * Save whatever perspective the player was using.
             */
            if (!currentlyLooking && wantsLookMode) {

                perspectiveBeforeLookMode =
                        mc.options.perspective;


                /*
                 * BOTH CAMERA MODES USE F5 THIRD-PERSON-BACK.
                 */
                mc.options.perspective =
                        PERSPECTIVE_THIRD_PERSON_BACK;
            }


            /*
             * Switching directly between:
             *
             * CAMERA_FREELOOK
             *
             * and
             *
             * YAW_FOLLOW_LOOK
             *
             * Keep F5 third-person active.
             */
            if (currentlyLooking && wantsLookMode) {

                mc.options.perspective =
                        PERSPECTIVE_THIRD_PERSON_BACK;
            }


            /*
             * Leaving Omnilook entirely.
             */
            if (currentlyLooking && !wantsLookMode) {

                mc.options.perspective =
                        perspectiveBeforeLookMode;
            }


            /*
             * Update actual Omnilook state.
             */
            if (desired == OmnilookState.Mode.NONE) {

                OmnilookState.deactivate();

            } else {

                OmnilookState.activate(
                        desired,
                        player.yaw,
                        player.pitch
                );
            }
        }


        /*
         * No camera mode active.
         */
        if (!OmnilookState.isAnyActive()) {
            return;
        }


        /*
         * =========================================================
         * CAMERA FREELOOK
         * =========================================================
         *
         * IMPORTANT:
         *
         * Do NOT read Mouse.getDX()/getDY() here.
         *
         * Our MixinGameRenderer handles this using vanilla's
         * already-calculated mouse-turn values.
         *
         * It:
         *
         * - moves camYaw / camPitch
         * - prevents the player turn call
         * - renders using those camera angles
         *
         * Therefore:
         *
         * CAMERA MOVES
         * PLAYER STAYS LOCKED
         */
        if (OmnilookState.isCameraFreelookActive()) {
            return;
        }


        /*
         * =========================================================
         * YAW FOLLOW / F5 EMULATION
         * =========================================================
         *
         * KEEP THIS MODE USING OUR ORIGINAL SYSTEM.
         */

        if (!OmnilookState.isYawFollowActive()) {
            return;
        }


        int dxRaw =
                Mouse.getDX();

        int dyRaw =
                Mouse.getDY();


        if (dxRaw == 0 && dyRaw == 0) {
            return;
        }


        /*
         * Existing mouse sensitivity calculation.
         */
        float sensitivity =
                (float) (
                        mc.options.sensitivity
                                * 0.6F
                                + 0.2F
                );


        float f =
                sensitivity
                        * sensitivity
                        * sensitivity
                        * 8.0F;


        float mult =
                (float)
                        OmnilookConfig
                                .mouseSensitivityMultiplier;


        float dYaw =
                dxRaw
                        * f
                        * mult
                        * 0.15F;


        float dPitch =
                dyRaw
                        * f
                        * mult
                        * 0.15F
                        * (
                        mc.options.invertYMouse
                                ? -1
                                : 1
                );


        /*
         * =========================================================
         * EXISTING F5 FOLLOW LOGIC
         * =========================================================
         */

        OmnilookState.followYaw +=
                dYaw;


        OmnilookState.followPitch =
                OmnilookState.clampPitch(
                        OmnilookState.followPitch
                                + dPitch
                );


        /*
         * Real player rotates with camera.
         */
        player.yaw =
                wrapDegrees(
                        OmnilookState.followYaw
                );


        player.pitch =
                OmnilookState.followPitch;


        /*
         * Remove interpolation lag.
         */
        player.prevYaw =
                player.yaw;

        player.prevPitch =
                player.pitch;
    }


    private static float wrapDegrees(float angle) {

        angle =
                angle % 360.0F;


        if (angle < -180.0F) {

            angle +=
                    360.0F;
        }


        if (angle >= 180.0F) {

            angle -=
                    360.0F;
        }


        return angle;
    }
}