package ihamfp.exppipes.tileentities;

import ihamfp.exppipes.common.Configs;
import ihamfp.exppipes.pipenetwork.ItemDirection;
import ihamfp.exppipes.tileentities.pipeconfig.ConfigRoutingPipe;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class TileEntityExtractionPipe extends TileEntityRoutingPipe {
	public ConfigRoutingPipe extractConfig = new ConfigRoutingPipe();
	private String oldExtractConfig = "";
	
	private long lastExtract = 0;
	
	@Override
	public void serverUpdate() {
		if (!this.extractConfig.toString().equals(this.oldExtractConfig)) {
			this.oldExtractConfig = this.extractConfig.toString();
			IBlockState currentState = this.world.getBlockState(this.pos);
			this.world.notifyBlockUpdate(this.pos, currentState, currentState, 2);
		}
		
		if (this.lastExtract+Configs.extractTime <= this.world.getTotalWorldTime()) {
			this.lastExtract = this.world.getTotalWorldTime();
			for (EnumFacing f : EnumFacing.VALUES) {
				TileEntity te = this.world.getTileEntity(this.pos.offset(f));
				if (te == null || te instanceof TileEntityPipe) continue;
				if (!te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, f.getOpposite())) continue;
				
				IItemHandler itemHandler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, f.getOpposite());
				
				ItemStack extracted = null;
				for (int i=0; i<itemHandler.getSlots(); i++) {
					ItemStack stackInSlotPre =itemHandler.getStackInSlot(i);
					ItemStack stackInSlot = itemHandler.extractItem(i,Math.min(stackInSlotPre.getCount(),Configs.extractSize), true);
					if (!stackInSlot.isEmpty() && (this.extractConfig.filters.size() == 0 || this.extractConfig.doesMatchAnyFilter(stackInSlot))) {
						extracted = itemHandler.extractItem(i, Math.min(stackInSlotPre.getCount(),Configs.extractSize), false);
						break;
					}
				}
				
				if (extracted == null || extracted.isEmpty()) continue;
				
				this.itemHandler.insertedItems.add(new ItemDirection(extracted, f, null, this.world.getTotalWorldTime()));
				break;
			}
		}
		super.serverUpdate();
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		this.extractConfig.deserializeNBT(compound.getCompoundTag("extractConfig"));
		super.readFromNBT(compound);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setTag("extractConfig", this.extractConfig.serializeNBT());
		return super.writeToNBT(compound);
	}
	
	@Override
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound nbtTag = super.getUpdateTag();
		if (!this.world.isRemote) { // sending from the server
			nbtTag.setTag("extractConfig", this.extractConfig.serializeNBT());
		}
		return nbtTag;
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		NBTTagCompound nbtTag = pkt.getNbtCompound();
		if (this.world.isRemote) { // receiving from the client
			this.extractConfig.deserializeNBT(nbtTag.getCompoundTag("extractConfig"));
		}
		super.onDataPacket(net, pkt);
	}
}
