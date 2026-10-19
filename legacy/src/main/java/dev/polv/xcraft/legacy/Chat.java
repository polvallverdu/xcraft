package dev.polv.xcraft.legacy;

//? if >=1.7 {
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
//?} else if >=1.6 {
/*import net.minecraft.text.ChatMessage;
import net.minecraft.util.Formatting;
*///?}
//? if <1.13
import net.minecraft.command.CommandSource;

/** Coloured command feedback: text components from 1.7, ChatMessage on 1.6, plain strings with colour codes before. */
public final class Chat {
	private Chat() {
	}

	//? if >=1.6 {
	private static Formatting formatting(char code) {
		// toString() is the formatting code ("§" + code) on every version
		for (Formatting f : Formatting.values()) if (f.toString().charAt(1) == code) return f;
		return Formatting.WHITE;
	}
	//?}

	//? if >=1.7 {
	public static Text text(String message, char color) {
		Text text = new LiteralText(message);
		text.setStyle(new Style().setFormatting(formatting(color)));
		return text;
	}
	//?}

	//? if >=1.7 && <1.13 {
	public static void send(CommandSource source, String message, char color) {
		source.sendMessage(text(message, color));
	}
	//?} else if >=1.6 && <1.7 {
	/*public static void send(CommandSource source, String message, char color) {
		source.method_5505(ChatMessage.createTextMessage(message).setColor(formatting(color)));
	}
	*///?} else if <1.6 {
	/*public static void send(CommandSource source, String message, char color) {
		source.method_3331("§" + color + message);
	}
	*///?}
}
