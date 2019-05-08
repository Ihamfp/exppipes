package ihamfp.exppipes.tileentities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ihamfp.exppipes.common.Configs;
import ihamfp.exppipes.pipenetwork.BlockDimPos;
import ihamfp.exppipes.pipenetwork.ItemDirection;
import ihamfp.exppipes.tileentities.pipeconfig.FilterConfig;
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
	public Map<EnumFacing,Boolean> disableConnection = new HashMap<EnumFacing,Boolean>();
	
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
		
		ItemStack itemStack = stack.itemStack;
		if (simulate) itemStack = itemStack.copy();
		
		for (int j=0; j<itemHandler.getSlots();j++) {
			itemStack = itemHandler.insertItem(j, itemStack, simulate); // try to insert in all slots
			if (itemStack.isEmpty()) break; // nothing left, exit slots loop
		}
		return itemStack;
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
			
			stack = this.insertInto(itemHandler, new ItemDirection(stack, (EnumFacing)null, (BlockDimPos)null, this.world.getTotalWorldTime()), true);
			if (stack.isEmpty()) return true;
		}
		return false;
	}
	
	// output stack may not match the input stack
	ItemStack extractFrom(IItemHandler itemHandler, ItemStack stack, int count) {
		return this.extractFrom(itemHandler, new FilterConfig(stack, 0, false), count);
	}
	
	ItemStack extractFrom(IItemHandler itemHandler, FilterConfig filter, int count) {
		ItemStack outStack = null;
		for (int i=0; i<itemHandler.getSlots();i++) {
			if (!itemHandler.getStackInSlot(i).isEmpty() && filter.doesMatch(itemHandler.getStackInSlot(i))) {
				if (outStack == null) {
					outStack = itemHandler.extractItem(i, count, false);
					count = Integer.min(count, outStack.getMaxStackSize());
				} else {
					ItemStack stackInSlot = itemHandler.extractItem(i, count-outStack.getCount(), true);
					if (ItemStack.areItemStackTagsEqual(outStack, stackInSlot)) {
						itemHandler.extractItem(i, count-outStack.getCount(), false);
						outStack.grow(stackInSlot.getCount());
					}
				}
			}
			if (outStack != null && filter.doesMatch(outStack) && outStack.getCount() == count) {
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
			if (i.insertTime > this.world.getTotalWorldTime()) {
				i.insertTime = this.world.getTotalWorldTime()-Configs.travelTime;
			}
			
			if (i.itemStack.isEmpty() || i.itemStack.getItem() == Items.AIR) { // It's air, who gives a fuck
				toRemove.add(i);
			} else if ((this.world.getTotalWorldTime() - i.insertTime) >= Configs.travelTime && i.to != null) { // send it to destination
				BlockPos target = this.pos.offset(i.to);
				if (this.world.getTileEntity(target) == null || !this.world.getTileEntity(target).hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, i.to.getOpposite())) {
					i.to = null;
					continue;
				}
				
				IItemHandler targetItemHandler = this.world.getTileEntity(target).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, i.to.getOpposite());
				
				if (targetItemHandler instanceof WrappedItemHandler) {
					i.from = i.to.getOpposite();
					i.to = null;
					((WrappedItemHandler)targetItemHandler).handler.insertItemFromPipe(i);
					toRemove.add(i);
				} else {
					i.itemStack = insertInto(targetItemHandler, i);
					if (i.itemStack.isEmpty() || i.itemStack.getItem() == Items.AIR) {
						toRemove.add(i);
					} else {
						i.from = i.to;
						i.to = null;
						i.destinationPos = null;
						i.insertTime = this.world.getTotalWorldTime();
					}
				}
			} else if (i.to == null) { // search where to send to
				Collections.shuffle(Arrays.asList(faceOrder));
				for (EnumFacing e : faceOrder) {
					if (e == i.from || this.disableConnection.getOrDefault(e, false)) continue;
					BlockPos target = this.pos.offset(e);
					if (this.world.getTileEntity(target) == null) continue;
					
					if (!this.world.getTileEntity(target).hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, e.getOpposite())) continue;
					
					IItemHandler targetItemHandler = this.world.getTileEntity(target).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, e.getOpposite());
					
					if (targetItemHandler instanceof WrappedItemHandler) {
						i.to = e;
						break;
					}
					
					ItemStack stack = insertInto(targetItemHandler, i, true);
					if (i.itemStack.getCount() != stack.getCount()) {
						i.to = e;
						break;
					}
				}
				if (i.to == null) { // still nothing ? Send it back
					i.to = i.from;
					i.from = i.to.getOpposite();
				}
			}
		}
		
		if (!this.itemHandler.storedItems.isEmpty()) {
			IBlockState currentState = this.world.getBlockState(this.pos);
			this.world.notifyBlockUpdate(this.pos, currentState, currentState, 2);
		}
		
		this.itemHandler.storedItems.removeAll(toRemove); // only keep non-empty stacks
		
		this.markDirty();
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
			if (facing != null && this.disableConnection.getOrDefault(facing, false)) {
				return false;
			}
			return true;
		}
		return super.hasCapability(capability, facing);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			if (facing == null) {
				return (T) this.itemHandler;
			} else if (!this.disableConnection.getOrDefault(facing, false)) {
				return (T) (new WrappedItemHandler(facing, this.itemHandler));
			}
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		this.itemHandler.deserializeNBT(compound.getCompoundTag("itemhandler"), this.getWorld());
		NBTTagCompound discon = compound.getCompoundTag("discon");
		for (EnumFacing f : EnumFacing.VALUES) {
			if (discon.getBoolean(f.getName())) {
				this.disableConnection.put(f, true);
			}
		}
		super.readFromNBT(compound);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		this.itemHandler.tick(this.world.getTotalWorldTime());
		compound.setTag("itemhandler", this.itemHandler.serializeNBT());
		NBTTagCompound disCon = new NBTTagCompound();
		for (EnumFacing f : this.disableConnection.keySet()) {
			disCon.setBoolean(f.getName(), this.disableConnection.get(f));
		}
		compound.setTag("discon", disCon);
		return super.writeToNBT(compound);
	}
	
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound nbtTag = new NBTTagCompound();
		if (!this.world.isRemote) { // sending from the server
			nbtTag.setTag("itemhandler", this.itemHandler.serializeNBT());
		}
		NBTTagCompound disCon = new NBTTagCompound();
		for (EnumFacing f : this.disableConnection.keySet()) {
			disCon.setBoolean(f.getName(), this.disableConnection.get(f));
		}
		nbtTag.setTag("discon", disCon);
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
			if (nbtTag.hasKey("itemhandler")) {
				this.itemHandler.deserializeNBT(nbtTag.getCompoundTag("itemhandler"));
			}
			if (nbtTag.hasKey("discon")) {
				NBTTagCompound discon = nbtTag.getCompoundTag("discon");
				for (EnumFacing f : EnumFacing.VALUES) {
					if (discon.getBoolean(f.getName())) {
						this.disableConnection.put(f, true);
					}
				}
			}
		}
		super.onDataPacket(net, pkt);
	}
}
