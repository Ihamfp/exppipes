package ihamfp.exppipes.items;

import ihamfp.exppipes.ExppipesMod;
import ihamfp.exppipes.tileentities.pipeconfig.FilterConfig;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemCraftingPattern extends Item {
	public ItemCraftingPattern() {
		this.setRegistryName(ExppipesMod.MODID, "craftingPattern");
		this.setMaxStackSize(1);
	}
	
	public static ItemStack[] getPatternResults(ItemStack pattern) {
		if (!(pattern.getItem() instanceof ItemCraftingPattern)) return new ItemStack[0];
		
		return new ItemStack[0]; // TODO patterns in general
	}
	
	public static FilterConfig[] getPatternIngredients(ItemStack pattern) {
		return new FilterConfig[0]; // also this
	}
}
