package ihamfp.exppipes.tileentities;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

// Refactoring
@SideOnly(Side.CLIENT)
public class InvCacheEntry {
	public ItemStack stack;
	/**
	 * 0 means "craftable"
	 */
	public int count;
	
	public InvCacheEntry(ItemStack stack, int count) {
		this.stack = stack;
		this.count = count;
	}
}