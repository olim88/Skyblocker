package de.hysky.skyblocker.skyblock.dwarven.fossilSolver;

import de.hysky.skyblocker.skyblock.dwarven.fossilSolver.structures.tileGrid;
import de.hysky.skyblocker.skyblock.item.tooltip.adders.LineSmoothener;
import de.hysky.skyblocker.utils.container.SimpleContainerSolver;
import de.hysky.skyblocker.utils.container.TooltipAdder;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalDouble;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.hysky.skyblocker.skyblock.dwarven.fossilSolver.fossilCalculations.*;

public class fossilSolver extends SimpleContainerSolver implements TooltipAdder {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static final Pattern PERCENTAGE_PATTERN = Pattern.compile("Fossil Excavation Progress: (\\d{2}.\\d)%");


	private String percentage;
	private static double[] probability;

	public fossilSolver() {
		super("Fossil Excavator");
		percentage = null;
	}

	@Override
	public List<ColorHighlight> getColors(Int2ObjectMap<ItemStack> slots) {
		//convert to container
		tileGrid mainTileGrid = convertItemsToTiles(slots);
		//get the fossil chance percentage
		if (percentage == null) {
			percentage = getFossilPercentage(slots);
		}
		//get chance for each
		probability = getFossilChance(mainTileGrid, percentage);
		//get the highlight amount and return
		return convertChanceToColor(probability);
	}

	/**
	 * See if there is any found fossils then see if there is a fossil chance percentage in the tool tips
	 *
	 * @param slots items to check tool tip of
	 * @return null if there is none or the value of the percentage
	 */
	private String getFossilPercentage(Int2ObjectMap<ItemStack> slots) {
		for (ItemStack item : slots.values()) {
			for (Text line : item.getTooltip(Item.TooltipContext.DEFAULT, CLIENT.player, TooltipType.BASIC)) {
				Matcher matcher = PERCENTAGE_PATTERN.matcher(line.getString());
				if (matcher.matches()) {
					return matcher.group(2);
				}
			}
		}
		return null;
	}

	@Override
	public boolean isEnabled() {
		//todo is enabled toggle
		return true;
	}

	private static List<ColorHighlight> convertChanceToColor(double[] chances) {
		List<ColorHighlight> outputColors = new ArrayList<>();
		Color gradientColor = Color.BLUE; //todo config
		//loop though all the chance values and set the color to match probability. full color means that its 100%
		OptionalDouble highProbability = Arrays.stream(chances).max();
		for (int i = 0; i < chances.length; i++) {
			double chance = chances[i];
			if (Double.isNaN(chances[i]) || chances[i] == 0) {
				continue;
			}
			if (chances[i] == highProbability.getAsDouble()) {
				outputColors.add(ColorHighlight.green(i));
				continue;
			}
			outputColors.add(new ColorHighlight(i, (int) (chance * 255) << 24 | gradientColor.getRed() << 16 | gradientColor.getGreen() << 8 | gradientColor.getBlue()));
		}
		return outputColors;
	}

	/**
	 * add solver info to tooltips
	 *
	 * @param focusedSlot the slot focused by the player
	 * @param stack       unused
	 * @param lines       the lines for the tooltip
	 */
	@Override
	public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Text> lines) { //todo translatable
		//only add if fossil or dirt
		if (stack.getItem() != Items.GRAY_STAINED_GLASS_PANE && stack.getItem() != Items.BROWN_STAINED_GLASS_PANE) {
			return;
		}
		//add spacer
		lines.add(LineSmoothener.createSmoothLine());

		//if no permutation say this instead of other stats
		if (permutations == 0) {
			lines.add(Text.literal("No fossil found").formatted(Formatting.GOLD));
			return;
		}

		//add permutation count
		lines.add(Text.literal("Possible Patterns: ").append(Text.literal(String.valueOf(permutations)).formatted(Formatting.YELLOW)));
		//add minimum tiles left count
		lines.add(Text.literal("Minimum fossil left : ").append(Text.literal(String.valueOf(minimumTiles)).formatted(Formatting.YELLOW))); //todo go red if less than uses left
		//add probability if available and not uncovered
		if (focusedSlot != null && probability != null && probability.length > focusedSlot.getIndex() && stack.getItem() == Items.BROWN_STAINED_GLASS_PANE) {
			lines.add(Text.literal("Probability: ").append(Text.literal(Math.round(probability[focusedSlot.getIndex()] * 100) + "%").formatted(Formatting.YELLOW)));
		}
	}

	@Override
	public int getPriority() {
		return 0;
	}


}


