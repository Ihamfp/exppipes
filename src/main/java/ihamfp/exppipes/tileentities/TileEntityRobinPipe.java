package ihamfp.exppipes.tileentities;

import java.util.ArrayList;

import ihamfp.exppipes.pipenetwork.BlockDimPos;
import ihamfp.exppipes.pipenetwork.ItemDirection;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants.NBT;

public class TileEntityRobinPipe extends TileEntityRoutingPipe {
	
	public ArrayList<BlockDimPos> robinList = new ArrayList<BlockDimPos>();
	public int robinIndex = 0; // index of the next pipe to send things to 
	
	protected boolean isDestPosValid(BlockDimPos destpos, ItemStack stack) {
		if (!destpos.hasTE()) return false;
		TileEntity te = destpos.getTE();
		if (te instanceof TileEntityRoutingPipe && ((TileEntityRoutingPipe)te).network == this.network && this.network != null) {
			if (((TileEntityRoutingPipe)te).sinkConfig.doesMatchAllFilters(stack)) return true;
		}
		return false;
	}
	
	@Override
	public boolean canInsert(ItemStack stack) { // Override so that the pipe can receive items and dispatch them later
		for (BlockDimPos p : this.robinList) {
			if (!isDestPosValid(p, stack)) continue;
			if (((TileEntityRoutingPipe)p.getTE()).canInsert(stack)) return true;
		}
		return super.canInsert(stack);
	}
	
	@Override
	public void serverUpdate() {
		super.serverUpdate();
		if (robinList.size() > 0) {
			for (ItemDirection i : this.itemHandler.storedItems) {
				this.robinIndex %= this.robinList.size();
				if (robinList.contains(i.destinationPos)) continue; // only change path for items that haven't already been redirected
				int startPos = this.robinIndex;
				boolean destPosValid = isDestPosValid(robinList.get(this.robinIndex), i.itemStack);
				while (!destPosValid && (this.robinIndex-startPos) < this.robinList.size()) {
					this.robinIndex++;
					destPosValid = isDestPosValid(robinList.get(this.robinIndex%this.robinList.size()), i.itemStack);
				}
				if (!destPosValid) break; // not a single valid destination... just stop
				
				i.destinationPos = this.robinList.get(this.robinIndex);
				i.to = this.network.getShortestFace(this, i.destinationPos.getTE()); // force re-routing
				this.robinIndex++;
			}
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		NBTTagList positions = compound.hasKey("positions")?compound.getTagList("positions", NBT.TAG_INT_ARRAY):(new NBTTagList());
		this.robinList.clear();
		for (int i=0; i<positions.tagCount(); i++) {
			int pipePos[] = ((NBTTagIntArray) positions.get(i)).getIntArray();
			this.robinList.add(new BlockDimPos(pipePos[0], pipePos[1], pipePos[2], pipePos[3]));
		}
		this.robinIndex = compound.getInteger("index");
		super.readFromNBT(compound);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagList positions = compound.hasKey("positions")?compound.getTagList("positions", NBT.TAG_INT_ARRAY):(new NBTTagList());
		for (BlockDimPos pos : this.robinList) {
			positions.appendTag(new NBTTagIntArray(new int[] {pos.getX(), pos.getY(), pos.getZ(), pos.dimension}));
		}
		compound.setTag("positions", positions);
		compound.setInteger("index", this.robinIndex);
		return super.writeToNBT(compound);
	}
}
