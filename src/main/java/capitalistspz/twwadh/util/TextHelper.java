package capitalistspz.twwadh.util;

import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

public class TextHelper {

    public static MutableText getInteractivePosText(Vec3d pos) {
        var clickEvent = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, String.format("/tp @s %s %s %s", pos.x, pos.y, pos.z));
        var hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("chat.coordinates.tooltip"));
        var coordsText = Text.translatable("chat.coordinates", pos.x, pos.y, pos.z);

        return Texts.bracketed(coordsText).styled(style -> style
                .withColor(Formatting.GREEN)
                .withClickEvent(clickEvent)
                .withHoverEvent(hoverEvent));
    }
}
