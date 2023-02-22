package capitalistspz.twwadh.mixin.block;

import capitalistspz.twwadh.advancement.criterion.CustomCriteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public class BlockMxn {
    @Inject(method="afterBreak", at=@At("HEAD"))
    public void triggerDestroyBlockAdvancementCriteria(World world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack stack, CallbackInfo ci){
        if (player instanceof ServerPlayerEntity serverPlayerEntity)
            CustomCriteria.DESTROYED_BLOCK.trigger(serverPlayerEntity, state, pos, stack);
    }
}
