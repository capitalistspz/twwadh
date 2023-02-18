package capitalistspz.twwadh.mixin.server;

import capitalistspz.twwadh.command.LocationStorage;
import capitalistspz.twwadh.interfaces.IHaveLocationStorage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMxn implements IHaveLocationStorage {
    @Shadow public abstract Iterable<ServerWorld> getWorlds();

    LocationStorage locationStorage;
    @Inject(method="createWorlds", at=@At("TAIL"))
    private void createLocationStorage(WorldGenerationProgressListener worldGenerationProgressListener, CallbackInfo ci){
        // :)
        for (var world : this.getWorlds()) {
            this.locationStorage = new LocationStorage(world.getPersistentStateManager());
            break;
        }
    }
    @Override
    public LocationStorage getLocationStorage() {
        if (this.locationStorage == null) {
            throw new NullPointerException("Called before server init");
        }
        return this.locationStorage;
    }
}
