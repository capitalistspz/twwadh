package capitalistspz.twwadh.command.argument;

import com.google.common.collect.Maps;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.PersistentStateManager;

import java.util.Map;
import java.util.stream.Stream;

public class LocationStorage {
    private static final String LOCATION_STORAGE_PREFIX = "location_storage_";
    private final Map<String, PersistentState> locationStorages = Maps.newHashMap();
    private final PersistentStateManager stateManager;

    public LocationStorage(PersistentStateManager stateManager) {
        this.stateManager = stateManager;
    }

    private PersistentState createStorage(String namespace) {
        PersistentState persistentState = new PersistentState();
        this.locationStorages.put(namespace, persistentState);
        return persistentState;
    }

    public Stream<String> getKeys() {
        return this.locationStorages.keySet().stream();
    }

    public Vec3d get(Identifier id) {
        String string = id.getNamespace();
        PersistentState persistentState = this.stateManager.get(data -> this.createStorage(string).readNbt(data), LocationStorage.getSaveKey(string));
        return persistentState != null ? persistentState.get(id.getPath()) : null;
    }

    public Stream<Map.Entry<String, Vec3d>> getEntries(String namespace) {
        var storage = this.locationStorages.get(namespace);
        if (storage == null)
            return Stream.empty();
        var entries = storage.getEntries();
        if (entries == null)
            return Stream.empty();
        return entries;
    }

    public void set(Identifier id, Vec3d pos) {
        String string = id.getNamespace();
        this.stateManager.getOrCreate(data -> this.createStorage(string).readNbt(data), () -> this.createStorage(string), LocationStorage.getSaveKey(string)).set(id.getPath(), pos);
    }

    public boolean remove(Identifier id) {
        String string = id.getNamespace();
        PersistentState persistentState = this.stateManager.get(data -> this.createStorage(string).readNbt(data), LocationStorage.getSaveKey(string));
        return (persistentState != null) && persistentState.remove(id.getPath());
    }

    public Stream<Identifier> getIds() {
        return this.locationStorages.entrySet().stream().flatMap(entry -> entry.getValue().getIds(entry.getKey()));
    }

    private static String getSaveKey(String namespace) {
        return LOCATION_STORAGE_PREFIX + namespace;
    }

    static class PersistentState extends net.minecraft.world.PersistentState {
        private static final String CONTENTS_KEY = "positions";
        private final Map<String, Vec3d> map = Maps.newHashMap();

        PersistentState() {
        }

        public PersistentState readNbt(NbtCompound nbt) {
            var positions = nbt.getCompound(CONTENTS_KEY);
            for (String element : positions.getKeys()) {
                var posCompound = positions.getCompound(element);
                var pos = new Vec3d(
                        posCompound.getDouble("X"),
                        posCompound.getDouble("Y"),
                        posCompound.getDouble("Z"));
                this.map.put(element, pos);
            }
            return this;
        }

        @Override
        public NbtCompound writeNbt(NbtCompound nbt) {
            NbtCompound nbtCompound = new NbtCompound();
            this.map.forEach((key, value) -> {
                NbtCompound nbt3d = new NbtCompound();
                nbt3d.putDouble("X", value.x);
                nbt3d.putDouble("Y", value.y);
                nbt3d.putDouble("Z", value.z);
                nbtCompound.put(key, nbt3d);
            });
            nbt.put(CONTENTS_KEY, nbtCompound);
            return nbt;
        }

        public Vec3d get(String name) {
            return this.map.get(name);
        }

        public Stream<Map.Entry<String, Vec3d>> getEntries() {
            return this.map.entrySet().stream();
        }

        public void set(String name, Vec3d pos) {
            this.map.put(name, pos);
            this.markDirty();
        }

        public boolean remove(String name) {
            return this.map.remove(name) != null;
        }

        public Stream<Identifier> getIds(String namespace) {
            return this.map.keySet().stream().map(key -> new Identifier(namespace, key));
        }
    }
}
