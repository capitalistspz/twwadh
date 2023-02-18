package capitalistspz.twwadh.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.concurrent.CompletableFuture;

public class AxisArgumentType implements ArgumentType<Direction.Axis> {
    private static final Collection<String> OPTIONS = Arrays.asList("x","y","z");
    private static final SimpleCommandExceptionType INVALID_AXIS_EXCEPTION = new SimpleCommandExceptionType(
            Text.translatableWithFallback("twwadh.arguments.axis.invalid", "Invalid axis"));
    @Override
    public Direction.Axis parse(StringReader reader) throws CommandSyntaxException {
        if (reader.canRead() && reader.peek() != ' '){
            char c = reader.read();
            return switch (c) {
                case 'x' -> Direction.Axis.X;
                case 'y' -> Direction.Axis.Y;
                case 'z' -> Direction.Axis.Z;
                default -> throw INVALID_AXIS_EXCEPTION.create();
            };
        }
        throw INVALID_AXIS_EXCEPTION.create();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        if (context.getSource() instanceof CommandSource){
            return CommandSource.suggestMatching(OPTIONS, builder);
        }
        return Suggestions.empty();
    }

    @Override
    public Collection<String> getExamples() {
        return AxisArgumentType.OPTIONS;
    }
}
