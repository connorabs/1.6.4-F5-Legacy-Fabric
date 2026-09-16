package dev.example.omnilook;

/**
 * Shared mutable state for the two Omnilook modes.
 * <p>
 * CAMERA mode: the render camera is free to look anywhere without touching
 * the entity's actual yaw/pitch (and therefore without affecting where you
 * aim or where your body faces). This is the classic "freelook" behaviour -
 * nothing about the player entity changes, only what's rendered.
 * <p>
 * YAW-FOLLOW mode: behaves like vanilla's F5 rear-view camera. While active,
 * mouse movement rotates the camera around the player like third person,
 * but unlike vanilla F5 the player's actual yaw is continuously synced to
 * follow the camera (this is the "akin to F5 but your yaw changes" behaviour
 * requested - vanilla F5 on its own does NOT change your yaw, it just moves
 * the camera behind you, so this mode re-applies the camera yaw back onto
 * the entity every tick while held/toggled).
 */
public final class OmnilookState {
    private OmnilookState() {}

    public enum Mode {
        NONE,
        CAMERA_FREELOOK,
        YAW_FOLLOW_LOOK
    }

    private static volatile Mode activeMode = Mode.NONE;

    /** Camera-only yaw/pitch offset accumulated while CAMERA_FREELOOK is active. */
    public static volatile float camYaw;
    public static volatile float camPitch;

    /** Absolute camera yaw/pitch used while YAW_FOLLOW_LOOK is active. */
    public static volatile float followYaw;
    public static volatile float followPitch;

    public static Mode getActiveMode() {
        return activeMode;
    }

    public static boolean isCameraFreelookActive() {
        return activeMode == Mode.CAMERA_FREELOOK;
    }

    public static boolean isYawFollowActive() {
        return activeMode == Mode.YAW_FOLLOW_LOOK;
    }

    public static boolean isAnyActive() {
        return activeMode != Mode.NONE;
    }

    /**
     * Called when entering a mode. Seeds the mode's working angles from the
     * player's current look direction so the camera doesn't jump on
     * activation.
     */
    public static void activate(Mode mode, float currentYaw, float currentPitch) {
        activeMode = mode;
        if (mode == Mode.CAMERA_FREELOOK) {
            camYaw = currentYaw;
            camPitch = currentPitch;
        } else if (mode == Mode.YAW_FOLLOW_LOOK) {
            followYaw = currentYaw;
            followPitch = currentPitch;
        }
    }

    /**
     * Called when leaving a mode, e.g. releasing a held key or toggling off.
     */
    public static void deactivate() {
        activeMode = Mode.NONE;
    }

    public static float clampPitch(float pitch) {
        if (pitch > 90.0F) return 90.0F;
        if (pitch < -90.0F) return -90.0F;
        return pitch;
    }
}
