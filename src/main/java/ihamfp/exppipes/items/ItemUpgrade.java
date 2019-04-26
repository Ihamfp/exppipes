package ihamfp.exppipes.items;

import ihamfp.exppipes.ExppipesMod;
import ihamfp.exppipes.ModCreativeTabs;
import net.minecraft.item.Item;

public class ItemUpgrade extends Item {
	public ItemUpgrade(String id) {
		this.setRegistryName(ExppipesMod.MODID, id);
		this.setCreativeTab(ModCreativeTabs.PIPES);
	}
}
