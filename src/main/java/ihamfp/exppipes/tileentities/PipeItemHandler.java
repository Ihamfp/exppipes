package ihamfp.exppipes.tileentities;

import java.util.ArrayList;
import java.util.List;

import ihamfp.exppipes.pipenetwork.BlockDimPos;
import ihamfp.exppipes.pipenetwork.ItemDirection;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandler;

public class PipeItemHandler implements IItemHandler, INBTSerializable<NBTTagCompound> {
	public List<ItemDirection> storedItems = new ArrayList<ItemDirection>();
	public List<ItemDirection> insertedItems = new ArrayList<ItemDirection>(); // items inserted last tick
	
	public void insertItemFromPipe(ItemDirection stack) {
		this.insertedItems.add(stack);
	}
	
	public void tick(long worldTick) {
		if (insertedItems.size() > 0) {
			for (ItemDirection it : this.insertedItems) {
				it.insertTime = worldTick;
				this.storedItems.add(it);
			}
			insertedItems.clear();
		}
	}
	
	@Override
	public int getSlots() {
		return storedItems.size()+1;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		if (slot >= storedItems.size()) {
			return ItemStack.EMPTY;
		}
		return storedItems.get(slot).itemStack;
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		//ExppipesMod.logger.info("Inserting " + stack.toString() + " in slot " + Integer.toString(slot));
		// Don't care about the slot
		// Always return empty
		if (!simulate) {
			this.insertedItems.add(new ItemDirection(stack, null, null, (BlockDimPos)null, 0));
		}
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		return ItemStack.EMPTY;
	}

	@Override
	public int getSlotLimit(int slot) {
		return 64;
	}

	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagList nbtList = new NBTTagList();
		for (ItemDirection itemDir : this.storedItems) {
			NBTTagCompound entry = new NBTTagCompound();
			itemDir.itemStack.writeToNBT(entry);
			entry.setByte("from", (byte)itemDir.from.getIndex()); // getIndex() return 0-5 anyway
			if (itemDir.to != null) {
				entry.setByte("to", (byte)itemDir.to.getIndex());
			}
			entry.setLong("insertTime", itemDir.insertTime);
			if (itemDir.destinationPos != null && !itemDir.destinationPos.hasTE()) {
				BlockDimPos destPos = itemDir.destinationPos;
				int[] posXYZ = {destPos.getX(), destPos.getY(), destPos.getZ(), destPos.dimension};
				entry.setIntArray("destination", posXYZ);
			}
			
			nbtList.appendTag(entry);
		}
		
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setTag("items", nbtList);
		return nbt;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		this.deserializeNBT(nbt, null);
	}
	
	public void deserializeNBT(NBTTagCompound nbt, World world) {
		NBTTagList nbtList = nbt.getTagList("items", Constants.NBT.TAG_COMPOUND);
		
		this.storedItems.clear();
		for (int i=0; i<nbtList.tagCount(); i++) {
			NBTTagCompound entry = nbtList.getCompoundTagAt(i);
			EnumFacing from = EnumFacing.byIndex(entry.getByte("from"));
			EnumFacing to = null;
			if (entry.hasKey("to")) {
				to = EnumFacing.byIndex(entry.getByte("to"));
			}
			ItemStack item = new ItemStack(entry);
			long insertTime = entry.getLong("insertTime");
			
			if (!entry.hasKey("destination")) {
				this.storedItems.add(new ItemDirection(item, from, to, (BlockDimPos)null, insertTime)); // cannot get the world, cannot get the destination
				continue;
			}
			
			int[] destPos = entry.getIntArray("destination");
			BlockDimPos destTE = null;
			if (destPos.length < 4) {
				destTE = new BlockDimPos(destPos[0], destPos[1], destPos[2], destPos[3]);
			} else {
				destTE = new BlockDimPos(destPos[0], destPos[1], destPos[2], 0);
			}
			int timer = entry.getInteger("timer");
			if (!destTE.hasTE() || !(destTE.getTE() instanceof TileEntityRoutingPipe)) {
				this.storedItems.add(new ItemDirection(item, from, to, (BlockDimPos)null, timer)); // cannot get the world, cannot get the destination
				continue;
			}
			
			this.storedItems.add(new ItemDirection(item, from, to, destTE, timer));
		}
	}
	
}