package ihamfp.exppipes.tileentities;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

// Refactoring
@SideOnly(Side.CLIENT)
public class InvCacheEntry {
	public ItemStack stack;
	
	public int count;
	
	public boolean craftable;
	
	public InvCacheEntry(ItemStack stack, int count, boolean craftable) {
		this.stack = stack;
		this.count = count;
		this.craftable = craftable;
	}
}