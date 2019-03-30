package ihamfp.exppipes.containers.slot;

import ihamfp.exppipes.tileentities.pipeconfig.ConfigRoutingPipe;
import ihamfp.exppipes.tileentities.pipeconfig.FilterConfig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ConfigSlot extends Slot {
	public ConfigRoutingPipe config;
	int slotCapacity;
	// this.slotNumber is used to store the config index

	public ConfigSlot(ConfigRoutingPipe config, int index, int xPosition, int yPosition) {
		this(config, index, xPosition, yPosition, 1);
	}
	
	public ConfigSlot(ConfigRoutingPipe config, int index, int xPosition, int yPosition, int slotCapacity) {
		super(null, index, xPosition, yPosition);
		this.config = config;
		this.slotCapacity = slotCapacity;
	}

	@Override
	public void onSlotChange(ItemStack p_75220_1_, ItemStack p_75220_2_) {
		// no onCrafting for you today
	}

	@Override
	public ItemStack onTake(EntityPlayer thePlayer, ItemStack stack) {
		return stack;
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		/*if (config.filters.size() == 0) return (this.slotNumber == 0);
		// prevent adding the same filter twice. Useful for suppliers.
		for (FilterConfig filter : config.filters) {
			if (filter.doesMatch(stack)) return false;
		}*/
		return (this.slotNumber <= config.filters.size());
	}

	@Override
	public ItemStack getStack() {
		if (config.filters.size() > this.slotNumber) {
			return config.filters.get(slotNumber).reference;
		} else {
			return ItemStack.EMPTY;
		}
	}

	@Override
	public boolean getHasStack() {
		return (config.filters.size() > this.slotNumber);
	}

	@Override
	public void putStack(ItemStack stack) {
		if (config.filters.size() <= this.slotNumber) {
			config.filters.add(new FilterConfig(stack, 0, false));
		} else {
			config.filters.get(slotNumber).reference = stack;
		}
	}

	@Override
	public void onSlotChanged() {
		
	}

	@Override
	public int getSlotStackLimit() {
		return this.slotCapacity;
	}

	@Override
	public int getItemStackLimit(ItemStack stack) {
		return Integer.min(slotCapacity, stack.getMaxStackSize());
	}

	@Override
	public ItemStack decrStackSize(int amount) {
		if (amount > 0 && config.filters.size() > this.slotNumber) {
			ItemStack toReturn = config.filters.get(slotNumber).reference.copy();
			config.filters.remove(this.slotNumber);
			return toReturn;
		}
		return ItemStack.EMPTY;
	}

	@Override
	public boolean isHere(IInventory inv, int slotIn) {
		return super.isHere(inv, slotIn);
	}

	@Override
	public boolean canTakeStack(EntityPlayer playerIn) {
		return (this.slotNumber < config.filters.size()); // don't add things in the middle of nowhere
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public boolean isSameInventory(Slot other) {
		return (other instanceof ConfigSlot);
	}
}
