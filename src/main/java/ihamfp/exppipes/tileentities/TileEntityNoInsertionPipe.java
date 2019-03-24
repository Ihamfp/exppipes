package ihamfp.exppipes.tileentities;

import ihamfp.exppipes.pipenetwork.ItemDirection;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class TileEntityNoInsertionPipe extends TileEntityPipe {
	ItemStack insertInto(IItemHandler itemHandler, ItemDirection stack, boolean simulate) {
		if (!(itemHandler instanceof PipeItemHandler || itemHandler instanceof WrappedItemHandler)) {
			return stack.itemStack; // No insertion, as the name says
		}
		return super.insertInto(itemHandler, stack, simulate);
	}
}
