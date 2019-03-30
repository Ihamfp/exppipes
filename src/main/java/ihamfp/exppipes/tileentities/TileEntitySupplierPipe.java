package ihamfp.exppipes.tileentities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ihamfp.exppipes.common.Configs;
import ihamfp.exppipes.pipenetwork.ItemDirection;
import ihamfp.exppipes.pipenetwork.Request;
import ihamfp.exppipes.tileentities.pipeconfig.ConfigRoutingPipe;
import ihamfp.exppipes.tileentities.pipeconfig.FilterConfig;
import ihamfp.exppipes.tileentities.pipeconfig.Filters;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants.NBT;
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
				if (itemDir.destination == this && (this.world.getTotalWorldTime()-itemDir.insertTime)>=Configs.travelTime && r.filter.doesMatch(itemDir.itemStack)) {
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
				if (this.network != null && !this.requests.containsKey(filter) && this.canInsert(filter.reference)) {
					this.requests.put(filter, this.network.request(this, filter, 1));
					break;
				}
			}
		}
		if (this.requests.size() == 0) {
			for (FilterConfig filter : supplyConfig.computerFilters) {
				if (!invContains(inv, filter) && this.network != null && !this.requests.containsKey(filter) && this.canInsert(filter.reference)) {
					this.requests.put(filter, this.network.request(this, filter, 1));
					break;
				}
			}
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		this.supplyConfig.deserializeNBT(compound.getCompoundTag("supplyConfig"));
		if (this.network != null) this.network.requests.removeAll(this.requests.values());
		this.requests.clear();
		NBTTagList requests = compound.getTagList("requests", NBT.TAG_COMPOUND);
		for (int i=0; i<requests.tagCount();i++) {
			NBTTagCompound extReq = (NBTTagCompound) requests.get(i);
			this.requests.put(this.supplyConfig.filters.get(extReq.getInteger("supplierFilter")), new Request(this, extReq));
		}
		if (this.network != null) this.network.requests.addAll(this.requests.values());
		super.readFromNBT(compound);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setTag("supplyConfig", this.supplyConfig.serializeNBT());
		NBTTagList requests = new NBTTagList();
		for (FilterConfig reqFilter : this.requests.keySet()) {
			Request req = this.requests.get(reqFilter);
			NBTTagCompound extReq = req.serializeNBT();
			int filterID = this.supplyConfig.filters.indexOf(reqFilter);
			if (filterID < 0) continue; // no matching supply filter
			extReq.setInteger("supplierFilter", filterID);
			requests.appendTag(extReq);
		}
		compound.setTag("requests", requests);
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
			entry.put("item", filter.reference.getItem().getRegistryName());
			entry.put("filterType", Filters.filters.get(filter.filterId).getShortName());
			entry.put("priority", filter.priority);
			returns.add(entry);
		}
		return returns.toArray();
	}
    
    @Optional.Method(modid = "opencomputers")
    @Callback(doc = "function(string:item, [integer:quantity=1, [string:filterType=\"D\", [integer:meta=0, [string:nbtString=\"\"]]]]):integer; Add a supply filter, returns filter ID.")
    public Object[] addSupplyFilter(Context context, Arguments args) throws Exception {
    	String item = args.checkString(0);
		ItemStack stack = GameRegistry.makeItemStack(item, args.optInteger(3, 0), args.optInteger(1, 1), args.optString(4, ""));
		FilterConfig filter = new FilterConfig(stack, Filters.idFromShortString(args.optString(2, "D")), false);
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
