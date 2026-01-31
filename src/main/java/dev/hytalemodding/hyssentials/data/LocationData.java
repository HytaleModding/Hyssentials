package dev.hytalemodding.hyssentials.data;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import javax.annotation.Nonnull;

public record LocationData(
    @Nonnull String worldName,
    double x,
    double y,
    double z,
    float pitch,
    float yaw
) {
    public Vector3d toPosition() {
        return new Vector3d(x, y + 1.0, z);
    }

    /**
     * Returns body rotation for the Teleport component.
     * Body rotation only uses yaw (horizontal facing direction), pitch is 0.
     * Vector3f constructor is (pitch, yaw, roll).
     */
    public Vector3f toBodyRotation() {
        return new Vector3f(0.0f, yaw, 0.0f);
    }

    /**
     * Returns head rotation for the Teleport.setHeadRotation() call.
     * Head rotation uses both pitch (vertical look) and yaw (horizontal look).
     * Vector3f constructor is (pitch, yaw, roll).
     */
    public Vector3f toHeadRotation() {
        return new Vector3f(pitch, yaw, 0.0f);
    }

    public static LocationData from(String worldName, Vector3d position, Vector3f rotation) {
        return new LocationData(
            worldName,
            position.getX(),
            position.getY(),
            position.getZ(),
            rotation.getPitch(),
            rotation.getYaw()
        );
    }
}
