package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.skyblock.dungeon.DungeonClass;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlayerComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// this widget shows info about a player in the current dungeon group

public class DungeonPlayerWidget extends TabHudWidget {

	private static final MutableText TITLE = Text.literal("Player").formatted(Formatting.DARK_PURPLE,
			Formatting.BOLD);

	// match a player entry
	// group 1: name
	// group 2: class (or literal "EMPTY" pre dungeon start)
	// group 3: level (or nothing, if pre dungeon start)
	// this regex filters out the ironman icon as well as rank prefixes and emblems
	// \[\d*\] (?:\[[A-Za-z]+\] )?(?<name>[A-Za-z0-9_]*) (?:.* )?\((?<class>\S*) ?(?<level>[LXVI]*)\)
	public static final Pattern PLAYER_PATTERN = Pattern
			.compile("\\[\\d*\\] (?:\\[[A-Za-z]+\\] )?(?<name>[A-Za-z0-9_]*) (?:.* )?\\((?<class>\\S*) ?(?<level>[LXVI]*)\\)");

	private static final List<String> MSGS = List.of("???", "PRESS A TO JOIN", "Invite a friend!", "But nobody came.", "More is better!");

	private final int player;

	// title needs to be changeable here
	public DungeonPlayerWidget(int player) {
		super("Dungeon Player " + player, TITLE, Formatting.DARK_PURPLE.getColorValue());
		this.player = player;
	}

	@Override
	public void updateContent(List<Text> ignored) {
		int start = 1 + (player - 1) * 4;

		if (PlayerListManager.strAt(start) == null) {
			int idx = player - 1;
			IcoTextComponent noplayer = new IcoTextComponent(Ico.SIGN,
					Text.literal(MSGS.get(idx)).formatted(Formatting.GRAY));
			this.addComponent(noplayer);
			return;
		}
		Matcher m = PlayerListManager.regexAt(start, PLAYER_PATTERN);
		if (m == null) {
			this.addComponent(new IcoTextComponent());
			this.addComponent(new IcoTextComponent());
		} else {

			Text name = Text.literal("Name: ").append(Text.literal(m.group("name")).formatted(Formatting.YELLOW));
			this.addComponent(new PlayerComponent(PlayerListManager.getRaw(start), name));

			String cl = m.group("class");
			String level = m.group("level");

			if (level == null) {
				PlainTextComponent ptc = new PlainTextComponent(
						Text.literal("Player is dead").formatted(Formatting.RED));
				this.addComponent(ptc);
			} else {
				DungeonClass dungeonClass = DungeonClass.from(cl);

				Formatting clf = Formatting.GRAY;
				ItemStack cli = dungeonClass.icon();
				if (dungeonClass != DungeonClass.UNKNOWN) {
					clf = Formatting.LIGHT_PURPLE;
					cl += " " + m.group("level");
				}

				Text clazz = Text.literal("Class: ").append(Text.literal(cl).formatted(clf));
				IcoTextComponent itclass = new IcoTextComponent(cli, clazz);
				this.addComponent(itclass);
			}
		}

		this.addSimpleIcoText(Ico.CLOCK, "Ult Cooldown:", Formatting.GOLD, start + 1);
		this.addSimpleIcoText(Ico.POTION, "Revives:", Formatting.DARK_PURPLE, start + 2);

	}
}
