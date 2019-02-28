package ihamfp.exppipes;

import ihamfp.exppipes.blocks.ModBlocks;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public class ModCreativeTabs {
	public static CreativeTabs PIPES = new CreativeTabs(ExppipesMod.MODID + ".pipes") {
		@Override
		public ItemStack createIcon() {
			return new ItemStack(ModBlocks.routingPipe);
		}
	};
}
