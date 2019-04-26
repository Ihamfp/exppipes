package ihamfp.exppipes.tileentities;

import ihamfp.exppipes.pipenetwork.BlockDimPos;
import ihamfp.exppipes.pipenetwork.ItemDirection;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.IItemHandler;

//Facing-Wrapped, simulates a sided inventory
// Only used when other blocks insert in the pipe to prevent the item from going back
public class WrappedItemHandler implements IItemHandler {
	EnumFacing from;
	PipeItemHandler handler;
	
	public WrappedItemHandler(EnumFacing from, PipeItemHandler handler) {
		this.from = from;
		this.handler = handler;
	}

	@Override
	public int getSlots() {
		return handler.getSlots();
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return handler.getStackInSlot(slot);
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		//ExppipesMod.logger.info("Inserting " + stack.toString() + " in slot " + Integer.toString(slot) + " from " + from.getName());
		if (!simulate) {
			this.handler.insertedItems.add(new ItemDirection(stack, from, (BlockDimPos)null, 0));
		}
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		return handler.extractItem(slot, amount, simulate);
	}

	@Override
	public int getSlotLimit(int slot) {
		return handler.getSlotLimit(slot);
	}
}