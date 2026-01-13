package com.leclowndu93150.hyssentials.manager;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.universe.world.World;
import com.leclowndu93150.hyssentials.data.LocationData;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WarpManager {
    private final DataManager dataManager;
    private final Map<String, LocationData> warps;

    public WarpManager(@Nonnull DataManager dataManager) {
        this.dataManager = dataManager;
        this.warps = new ConcurrentHashMap<>(dataManager.loadWarps());
    }

    public void setWarp(@Nonnull String name, @Nonnull World world,
                        @Nonnull Vector3d position, @Nonnull Vector3f rotation) {
        warps.put(name.toLowerCase(), LocationData.from(world.getName(), position, rotation));
        dataManager.saveWarps(warps);
    }

    @Nullable
    public LocationData getWarp(@Nonnull String name) {
        return warps.get(name.toLowerCase());
    }

    public boolean deleteWarp(@Nonnull String name) {
        LocationData removed = warps.remove(name.toLowerCase());
        if (removed != null) {
            dataManager.saveWarps(warps);
            return true;
        }
        return false;
    }

    @Nonnull
    public Set<String> getWarpNames() {
        return Collections.unmodifiableSet(warps.keySet());
    }

    public int getWarpCount() {
        return warps.size();
    }

    public void save() {
        dataManager.saveWarps(warps);
    }
}
