package ihamfp.exppipes.tileentities.pipeconfig;

import net.minecraft.item.ItemStack;

public abstract class Filter {
	/***
	 * Displayed when hovering cursor, may be localized (i.e don't use as identifier or on server side)
	 */
	public abstract String getLongName();
	/***
	 * Displayed on the button, never localized, must be unique
	 */
	public abstract String getShortName();
	
	/***
	 * Check whether the items match.
	 */
	public abstract boolean doesMatch(ItemStack reference, ItemStack stack);
	
	/***
	 * Short text saying what's actually matched.
	 * Example: for OreDict filter, returns all OreDict entries.
	 * todo: use this somewhere
	 */
	public String getMatchingHint(ItemStack reference) {
		return "";
	}
	
	/***
	 * Check if the filter can even match something with this reference
	 */
	public boolean willEverMatch(ItemStack reference) {
		return true;
	}
}