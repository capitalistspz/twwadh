package capitalistspz.twwadh.command;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.argument.EnumArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.Direction;

public class AxisArgumentType extends EnumArgumentType<Direction.Axis> {
    private AxisArgumentType() {
        super(Direction.Axis.CODEC, Direction.Axis::values);
    }
    public static AxisArgumentType axis(){ return new AxisArgumentType(); }

    public static Direction.Axis getAxis(CommandContext<ServerCommandSource> context, String id){
        return context.getArgument(id, Direction.Axis.class);
    }
}
