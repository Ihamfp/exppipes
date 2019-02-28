package ihamfp.exppipes.tileentities.pipeconfig;

import ihamfp.exppipes.ExppipesMod;
import ihamfp.exppipes.tileentities.pipeconfig.FilterConfig.FilterType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

// No, this is not used anywhere. It was just a test, and now it is a piece of code screaming "do not do this !"
public class PipeConfigItemHandler implements IItemHandler, IItemHandlerModifiable {
	ItemStackHandler rerze;
	ConfigRoutingPipe config;
	
	public PipeConfigItemHandler(ConfigRoutingPipe config) {
		this.config = config;
	}
	
	@Override
	public int getSlots() {
		return 9;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		if (slot < config.filters.size()) {
			return config.filters.get(slot).stack;
		} else {
			return ItemStack.EMPTY;
		}
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		ExppipesMod.logger.info("inserting item in " + Integer.toString(slot) + ": " + stack.toString() + (simulate?" (simulating)":""));
		if (!simulate) {
			ItemStack filterStack = stack.copy();
			filterStack.setCount(1);
			if (slot < this.config.filters.size()) { // replace existing filter
				this.config.filters.get(slot).stack = stack.copy();
			} else { // add new filter
				this.config.filters.add(new FilterConfig(stack.copy(), FilterType.DEFAULT));
			}
		}
		return stack;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		ExppipesMod.logger.info("extracting item from " + Integer.toString(slot) + ": " + Integer.toString(amount) + (simulate?" (simulating)":""));
		if (!simulate && slot < this.config.filters.size()) {
			this.config.filters.remove(slot);
		}
		return ItemStack.EMPTY;
	}

	@Override
	public int getSlotLimit(int slot) {
		return 64;
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack) {
		ExppipesMod.logger.info("setting item in " + Integer.toString(slot) + ": " + stack.toString());
		if (slot < this.config.filters.size()) { // replace existing filter
			this.config.filters.get(slot).stack = stack.copy();
		} else { // add new filter
			this.config.filters.add(new FilterConfig(stack.copy(), FilterType.DEFAULT));
		}
	}
}
