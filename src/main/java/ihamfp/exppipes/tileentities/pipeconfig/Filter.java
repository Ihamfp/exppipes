package ihamfp.exppipes.tileentities.pipeconfig;

import net.minecraft.item.ItemStack;

public abstract class Filter {
	/***
	 * Displayed when hovering cursor, may be localized (i.e don't use as identifier or on server side)
	 */
	public abstract String getLongName();
	/***
	 * Displayed on the button, never localized
	 */
	public abstract String getShortName();
	
	public abstract boolean doesMatch(ItemStack reference, ItemStack stack);
	
	/***
	 * Short text saying what's actually matched.
	 * Example: for OreDict filter, returns all OreDict entries.
	 */
	public String getMatchingHint(ItemStack reference) {
		return reference.getDisplayName();
	}
}