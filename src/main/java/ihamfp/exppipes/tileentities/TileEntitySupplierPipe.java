package ihamfp.exppipes.tileentities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ihamfp.exppipes.pipenetwork.ItemDirection;
import ihamfp.exppipes.pipenetwork.PipeNetwork.Request;
import ihamfp.exppipes.tileentities.pipeconfig.ConfigRoutingPipe;
import ihamfp.exppipes.tileentities.pipeconfig.FilterConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileEntitySupplierPipe extends TileEntityRoutingPipe {
	public ConfigRoutingPipe supplyConfig = new ConfigRoutingPipe();
	//private List<Request> requests = new ArrayList<Request>();
	private Map<FilterConfig,Request> requests = new HashMap<FilterConfig,Request>();
	
	public boolean invContains(Map<ItemStack,TileEntity> inv, FilterConfig filter) {
		boolean didMatch = false;
		for (ItemStack stack : inv.keySet()) {
			if (filter.doesMatch(stack)) didMatch = true;
			break;
		}
		return didMatch;
	}
	
	@Override
	public void serverUpdate() {
		Map<ItemStack,TileEntity> inv = this.getInventories();

		// remove all completed requests
		this.itemHandler.tick(this.world.getTotalWorldTime());
		List<Request> rRemove = new ArrayList<Request>();
		for (Request r : this.requests.values()) {
			for (ItemDirection itemDir : itemHandler.storedItems) {
				if (itemDir.request != null && itemDir.request == r && itemDir.request.completed) {
					rRemove.add(r);
				}
			}
		}
		this.requests.values().removeAll(rRemove);
		
		for (FilterConfig filter : supplyConfig.filters) {
			if (!invContains(inv, filter) && this.network != null && !this.requests.containsKey(filter) && this.canInsert(filter.stack)) {
				this.requests.put(filter, this.network.request(this, new FilterConfig(filter.stack.copy(), filter.filterType)));
			}
		}
		for (FilterConfig filter : supplyConfig.computerFilters) {
			if (!invContains(inv, filter) && this.network != null && !this.requests.containsKey(filter) && this.canInsert(filter.stack)) {
				this.requests.put(filter, this.network.request(this, new FilterConfig(filter.stack.copy(), filter.filterType)));
			}
		}
		super.serverUpdate();
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		this.supplyConfig.deserializeNBT(compound.getCompoundTag("supplyConfig"));
		super.readFromNBT(compound);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setTag("supplyConfig", this.supplyConfig.serializeNBT());
		return super.writeToNBT(compound);
	}
}
