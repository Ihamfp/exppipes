package ihamfp.exppipes.tileentities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ihamfp.exppipes.pipenetwork.ItemDirection;
import ihamfp.exppipes.tileentities.pipeconfig.FilterConfig;
import ihamfp.exppipes.tileentities.pipeconfig.FilterConfig.FilterType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class TileEntityPipe extends TileEntity implements ITickable {
	public PipeItemHandler itemHandler = new PipeItemHandler();
	
	@Override
	public void update() {
		if (!this.world.isRemote) { // only update server
			this.serverUpdate();
		} else {
			this.clientUpdate();
		}
	}
	
	/**
	 * Try to insert a stack into an itemHandler
	 * @return the non-inserted stack
	 */
	ItemStack insertInto(IItemHandler itemHandler, ItemDirection stack, boolean simulate) {
		if (itemHandler instanceof PipeItemHandler) {
			((PipeItemHandler) itemHandler).insertItemFromPipe(stack);
			return ItemStack.EMPTY;
		}
		for (int j=0; j<itemHandler.getSlots();j++) {
			stack.itemStack = itemHandler.insertItem(j, stack.itemStack, simulate); // try to insert in all slots
			if (stack.itemStack.isEmpty()) break; // nothing left, exit slots loop
		}
		return stack.itemStack;
	}
	
	ItemStack insertInto(IItemHandler itemHandler, ItemDirection stack) {
		return this.insertInto(itemHandler, stack, false);
	}
	
	/***
	 * Check all inventories around (except for other pipes) and check if the item can be inserted
	 * @param stack
	 * @return
	 */
	public boolean canInsert(ItemStack stack) {
		stack = stack.copy();
		for (EnumFacing f : EnumFacing.VALUES) {
			BlockPos check = this.pos.offset(f);
			if (this.world.getTileEntity(check) == null) continue;
			if (!this.world.getTileEntity(check).hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, f.getOpposite())) continue;
			IItemHandler itemHandler = this.world.getTileEntity(check).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, f.getOpposite());
			if (itemHandler instanceof PipeItemHandler || itemHandler instanceof WrappedItemHandler) continue;
			
			stack = this.insertInto(itemHandler, new ItemDirection(stack, null, null, this.world.getTotalWorldTime()), true);
			if (stack.isEmpty()) return true;
		}
		return false;
	}
	
	// output stack may not match the input stack
	ItemStack extractFrom(IItemHandler itemHandler, ItemStack stack) {
		return this.extractFrom(itemHandler, new FilterConfig(stack, FilterType.STRICT));
	}
	
	ItemStack extractFrom(IItemHandler itemHandler, FilterConfig filter) {
		ItemStack outStack = null;
		for (int i=0; i<itemHandler.getSlots();i++) {
			if (filter.doesMatch(itemHandler.getStackInSlot(i))) {
				int needed = filter.stack.getCount();
				if (outStack != null) needed -= outStack.getCount();
				if (needed <= 0) break; // safety measure
				
				if (outStack == null) {
					outStack = itemHandler.extractItem(i, needed, false);
				} else {
					ItemStack stackInSlot = itemHandler.extractItem(i, needed, true);
					if (ItemStack.areItemStackTagsEqual(outStack, stackInSlot)) {
						itemHandler.extractItem(i, needed, false);
						outStack.grow(stackInSlot.getCount());
					}
				}
			}
			if (outStack != null && filter.doesMatch(outStack) && outStack.getCount() == filter.stack.getCount()) {
				break; // don't need to add more
			}
		}
		return outStack;
	}
	
	public void serverUpdate() {
		// Simple non-routing pipe: passes the items through
		this.itemHandler.tick(this.world.getTotalWorldTime());
		
		if (this.itemHandler.storedItems.size() == 0) return;
		List<ItemDirection> toRemove = new ArrayList<ItemDirection>();
		EnumFacing[] faceOrder = EnumFacing.VALUES.clone();
		
		for (ItemDirection i : this.itemHandler.storedItems) {
			if (this.world.getTotalWorldTime() - i.insertTime < PipeItemHandler.travelTime) continue; // ignore if not ready
			//ExppipesMod.logger.info("[tick " + Long.toString(this.world.getWorldTime()) +"]Got " + i.itemStack.toString() + " from " + i.from.getName());
			Collections.shuffle(Arrays.asList(faceOrder));
			for (EnumFacing e : faceOrder) {
				if (e == i.from) continue;
				BlockPos target = this.pos.offset(e);
				if (this.world.getTileEntity(target) == null) continue;
				//ExppipesMod.logger.info("Trying to insert to " + e.getName() + "...");
				
				if (!this.world.getTileEntity(target).hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, e.getOpposite())) continue;
				
				IItemHandler targetItemHandler = this.world.getTileEntity(target).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, e.getOpposite());
				//if (targetItemHandler == null) continue; // prevent nullPointer, just in case
				
				//ExppipesMod.logger.info("Inserting " + i.itemStack.toString() + " from " + this.pos.toString() + " to " + target.toString() + ", " + targetItemHandler.getClass().getSimpleName());

				//ExppipesMod.logger.info("[" + this.pos.toString() +" @ " + Long.toString(this.world.getWorldTime()) + "]Inserting item from " + i.from.getName() + " to " + e.getName() + " (target's " + e.getOpposite().getName() + ")");
				if (targetItemHandler instanceof WrappedItemHandler) {
					i.from = e.getOpposite();
					((WrappedItemHandler)targetItemHandler).handler.insertItemFromPipe(i);
					toRemove.add(i);
					break;
				}
				
				i.itemStack = insertInto(targetItemHandler, i);
				if (i.itemStack.isEmpty() || i.itemStack.getItem() == Items.AIR) {
					toRemove.add(i);
					break; // nothing left, exit facing loop
				}
			}
		}
		
		if (!this.itemHandler.storedItems.isEmpty()) {
			IBlockState currentState = this.world.getBlockState(this.pos);
			this.world.notifyBlockUpdate(this.pos, currentState, currentState, 2);
		}
		
		this.itemHandler.storedItems.removeAll(toRemove); // only keep non-empty stacks
	}
	
	@SideOnly(Side.CLIENT)
	public void clientUpdate() {
		
	}
	
	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
		if (oldState.getBlock() == newState.getBlock()) {
			return false; // only refresh if block is changed
		}
		// remove this from network
		return super.shouldRefresh(world, pos, oldState, newState);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			//ExppipesMod.logger.info("I has items @ [" + this.pos.toString() + "] from " + facing.getName());
			return true;
		}
		return super.hasCapability(capability, facing);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			if (facing == null) {
				//ExppipesMod.logger.info("Returning self item handler");
				return (T) this.itemHandler;
			}
			//ExppipesMod.logger.info("Returning wrapped item handler");
			return (T) (new WrappedItemHandler(facing, this.itemHandler));
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		this.itemHandler.deserializeNBT(compound.getCompoundTag("itemhandler"), this.getWorld());
		super.readFromNBT(compound);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setTag("itemhandler", this.itemHandler.serializeNBT());
		return super.writeToNBT(compound);
	}
	
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound nbtTag = new NBTTagCompound();
		if (!this.world.isRemote) { // sending from the server
			nbtTag.setTag("items", this.itemHandler.serializeNBT());
		}
		return nbtTag;
	}
	
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(this.getPos(), 1, this.getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		NBTTagCompound nbtTag = pkt.getNbtCompound();
		if (this.world.isRemote) { // receiving from the client
			if (nbtTag.hasKey("items")) {
				this.itemHandler.deserializeNBT(nbtTag.getCompoundTag("items"));
			}
		}
		super.onDataPacket(net, pkt);
	}
}
