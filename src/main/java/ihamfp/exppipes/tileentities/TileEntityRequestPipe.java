package ihamfp.exppipes.tileentities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ihamfp.exppipes.pipenetwork.ItemDirection;
import ihamfp.exppipes.pipenetwork.Request;
import ihamfp.exppipes.tileentities.pipeconfig.FilterConfig;
import ihamfp.exppipes.tileentities.pipeconfig.FilterConfig.FilterType;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityRequestPipe extends TileEntityRoutingPipe {
	@SideOnly(Side.CLIENT)
	public List<InvCacheEntry> invCache = null; // Filled when a packet is received. Used for the request pipe GUI
	
	public List<Request> requests = new ArrayList<Request>();
	
	@Override
	public void serverUpdate() {
		// remove all completed requests
		this.itemHandler.tick(this.world.getTotalWorldTime());
		List<Request> rRemove = new ArrayList<Request>();
		for (Request r : this.requests) {
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
		this.requests.removeAll(rRemove);
		if (this.network != null) {
			this.network.requests.removeAll(rRemove);
		}

		super.serverUpdate();
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		if (this.network != null) this.network.requests.removeAll(this.requests);
		NBTTagList requests = compound.getTagList("requests", NBT.TAG_COMPOUND);
		for (int i=0; i<requests.tagCount();i++) {
			this.requests.add(new Request(this, (NBTTagCompound) requests.get(i)));
		}
		if (this.network != null) this.network.requests.addAll(this.requests);
		super.readFromNBT(compound);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagList requests = new NBTTagList();
		for (Request req : this.requests) {
			requests.appendTag(req.serializeNBT());
		}
		compound.setTag("requests", requests);
		return super.writeToNBT(compound);
	}
	
	// opencomputers integration
	
	@Optional.Method(modid = "opencomputers")
	@Override
	public String getComponentName() {
		return "requestPipe";
	}
	
	@Optional.Method(modid = "opencomputers")
    @Callback(doc = "function(string:item, [integer:quantity=1, [string:filterType=\"DEFAULT\", [integer:meta=0, [string:nbtString=\"\"]]]]); Request something on the network")
    public Object[] request(Context context, Arguments args) throws Exception {
		if (this.network == null) return null;
		String item = args.checkString(0);
		ItemStack stack = GameRegistry.makeItemStack(item, args.optInteger(3, 0), 1, args.optString(4, ""));
		FilterConfig filter = new FilterConfig(stack, FilterType.fromString(args.optString(2, "DEFAULT")));
		this.requests.add(this.network.request(this, filter, args.optInteger(1, 1)));
		return null;
	}
	
	@Optional.Method(modid = "opencomputers")
    @Callback
    public Object[] getItemsStored(Context context, Arguments args) throws Exception {
    	List<Object> returns = new ArrayList<Object>();
		if (this.network == null) return returns.toArray();
		for (ItemStack stack : this.network.globalInventory()) {
			Map<String,Object> entry = new HashMap<String,Object>();
			entry.put("item", stack.getItem().getRegistryName());
			entry.put("meta", stack.getMetadata());
			NBTTagCompound stackNbt = stack.serializeNBT();
			if (stackNbt.hasKey("tag")) {
				entry.put("nbt", stackNbt.getTag("tag").toString());
			}
			entry.put("count", stack.getCount());
			returns.add(entry);
			//returns.add(new Object[] {stack.getItem().getRegistryName(), stack.getCount()});
		}
		return returns.toArray();
    }
	
	@Optional.Method(modid = "opencomputers")
    @Callback
    public Object[] getItemsRequestable(Context context, Arguments args) throws Exception {
		List<Object> returns = new ArrayList<Object>();
		if (this.network == null) return returns.toArray();
		Map<ItemStack,Integer> condInv = this.network.condensedInventory();
		for (ItemStack stack : condInv.keySet()) {
			Map<String,Object> entry = new HashMap<String,Object>();
			entry.put("item", stack.getItem().getRegistryName());
			entry.put("meta", stack.getMetadata());
			NBTTagCompound stackNbt = stack.serializeNBT();
			if (stackNbt.hasKey("tag")) {
				entry.put("nbt", stackNbt.getTag("tag").toString());
			}
			entry.put("count", condInv.get(stack));
			returns.add(entry);
			//returns.add(new Object[] {stack.getItem().getRegistryName(), stack.getCount()});
		}
		return returns.toArray();
	}
}
