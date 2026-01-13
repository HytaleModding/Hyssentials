package com.leclowndu93150.hyssentials.manager;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.universe.world.World;
import com.leclowndu93150.hyssentials.data.LocationData;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SpawnManager {
    private final DataManager dataManager;
    private LocationData spawn;

    public SpawnManager(@Nonnull DataManager dataManager) {
        this.dataManager = dataManager;
        this.spawn = dataManager.loadSpawn();
    }

    public void setSpawn(@Nonnull World world, @Nonnull Vector3d position, @Nonnull Vector3f rotation) {
        spawn = LocationData.from(world.getName(), position, rotation);
        dataManager.saveSpawn(spawn);
    }

    @Nullable
    public LocationData getSpawn() {
        return spawn;
    }

    public boolean hasSpawn() {
        return spawn != null;
    }

    public void save() {
        if (spawn != null) {
            dataManager.saveSpawn(spawn);
        }
    }
}
