package ihamfp.exppipes.tileentities;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class TileEntityRequestStation extends TileEntityNetworkBlock implements ITickable {
	public List<InvCacheEntry> invCache = null;
	public ItemStackHandler inventory = new ItemStackHandler(27);
	// crafting things
	public ItemStackHandler craftMatrix = new ItemStackHandler(9);
	public ItemStackHandler craftResult = new ItemStackHandler(1);
	// return slot
	public ItemStackHandler returnSlot = new ItemStackHandler(1) {
		@Override
		protected void onContentsChanged(int slot) {
			if (!this.getStackInSlot(slot).isEmpty()) {
				ItemStack insertStack = this.getStackInSlot(slot);
				BlockPos tePos = TileEntityRequestStation.this.getPos();
				World teWorld = TileEntityRequestStation.this.getWorld();
				for (EnumFacing f : EnumFacing.VALUES) {
					BlockPos offPos = tePos.offset(f);
					if (teWorld.getTileEntity(offPos) != null && teWorld.getTileEntity(offPos).hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, f.getOpposite())) {
						IItemHandler itemHandler = teWorld.getTileEntity(offPos).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, f.getOpposite());
						for (int i=0; i<itemHandler.getSlots() && !insertStack.isEmpty();i++) {
							insertStack = itemHandler.insertItem(i, insertStack, false);
						}
					}
				}
				this.stacks.set(slot, insertStack);
			}
		}
	};
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return true;
		}
		return super.hasCapability(capability, facing);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return (T) this.inventory;
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public void update() {
		// TODO manage requests
	}
}
