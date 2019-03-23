package ihamfp.exppipes.tileentities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ihamfp.exppipes.pipenetwork.ItemDirection;
import ihamfp.exppipes.pipenetwork.PipeNetwork.Request;
import ihamfp.exppipes.tileentities.pipeconfig.ConfigRoutingPipe;
import ihamfp.exppipes.tileentities.pipeconfig.FilterConfig;
import ihamfp.exppipes.tileentities.pipeconfig.FilterConfig.FilterType;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class TileEntitySupplierPipe extends TileEntityRoutingPipe {
	public ConfigRoutingPipe supplyConfig = new ConfigRoutingPipe();
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
		// remove all completed requests
		this.itemHandler.tick(this.world.getTotalWorldTime());
		List<Request> rRemove = new ArrayList<Request>();
		for (Request r : this.requests.values()) {
			for (ItemDirection itemDir : itemHandler.storedItems) {
				if (itemDir.destination == this && (this.world.getTotalWorldTime()-itemDir.insertTime)>=PipeItemHandler.travelTime && r.filter.doesMatch(itemDir.itemStack)) {
					r.processingCount.addAndGet(-itemDir.itemStack.getCount());
					if (r.processingCount.get() < 0) r.processingCount.set(0);
					r.processedCount += itemDir.itemStack.getCount();
				}
			}
			if (r.processedCount >= r.requestedCount) {
				rRemove.add(r);
			}
		}
		this.requests.values().removeAll(rRemove);
		if (this.network != null) {
			this.network.requests.removeAll(rRemove);
		}

		super.serverUpdate();

		Map<ItemStack,TileEntity> inv = this.getInventories();
		if (this.requests.size() == 0) { // temporary
			for (FilterConfig filter : supplyConfig.filters) {
				if (!invContains(inv, filter) && this.network != null && !this.requests.containsKey(filter) && this.canInsert(filter.stack)) {
					this.requests.put(filter, this.network.request(this, new FilterConfig(filter.stack.copy(), filter.filterType), 1));
					break;
				}
			}
		}
		if (this.requests.size() == 0) {
			for (FilterConfig filter : supplyConfig.computerFilters) {
				if (!invContains(inv, filter) && this.network != null && !this.requests.containsKey(filter) && this.canInsert(filter.stack)) {
					this.requests.put(filter, this.network.request(this, new FilterConfig(filter.stack.copy(), filter.filterType), 1));
					break;
				}
			}
		}
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
	
	//////////Begin OpenComputers integration
	
	@Optional.Method(modid = "opencomputers")
	@Override
	public String getComponentName() {
		return "supplierPipe";
	}
	
    @Optional.Method(modid = "opencomputers")
    @Callback(doc = "function():...; Returns the supply configuration. Usage: `supply = {getSinkConfig()}`")
    public Object[] getSupplyConfig(Context context, Arguments args) throws Exception {
		List<Object> returns = new ArrayList<Object>();
		for (FilterConfig filter : this.supplyConfig.computerFilters) {
			Map<String,Object> entry = new HashMap<String,Object>();
			entry.put("item", filter.stack.getItem().getRegistryName());
			entry.put("filterType", filter.filterType.toString());
			entry.put("priority", filter.priority);
			returns.add(entry);
		}
		return returns.toArray();
	}
    
    @Optional.Method(modid = "opencomputers")
    @Callback(doc = "function(string:item, [integer:quantity=1, [string:filterType=\"DEFAULT\", [integer:meta=0, [string:nbtString=\"\"]]]]):integer; Add a supply filter, returns filter ID.")
    public Object[] addSupplyFilter(Context context, Arguments args) throws Exception {
    	String item = args.checkString(0);
		ItemStack stack = GameRegistry.makeItemStack(item, args.optInteger(3, 0), args.optInteger(1, 1), args.optString(4, ""));
		FilterConfig filter = new FilterConfig(stack, FilterType.fromString(args.optString(2, "DEFAULT")));
		this.supplyConfig.computerFilters.add(filter);
    	return new Object[] {this.supplyConfig.computerFilters.size()};
    }
    
    @Optional.Method(modid = "opencomputers")
    @Callback(doc = "function(integer:id); Removes a supply filter using its ID.")
    public Object[] removeSupplyFilter(Context context, Arguments args) throws Exception {
    	int id = args.checkInteger(0);
    	this.supplyConfig.computerFilters.remove(id-1); // Lua index is 1-based
    	return null;
    }
    
    @Optional.Method(modid = "opencomputers")
    @Callback(doc = "function(); Completely clear the supply configuration.")
    public Object[] clearSupplyConfig(Context context, Arguments args) throws Exception {
    	this.supplyConfig.computerFilters.clear();
    	return null;
    }
}
