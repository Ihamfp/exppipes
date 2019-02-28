package ihamfp.exppipes.tileentities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ihamfp.exppipes.tileentities.pipeconfig.FilterConfig;
import ihamfp.exppipes.tileentities.pipeconfig.FilterConfig.FilterType;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.common.Optional;

public class TileEntityRequestPipe extends TileEntityRoutingPipe {
	
	@SideOnly(Side.CLIENT)
	public Map<ItemStack,Integer> invCache = null; // Filled when a packet is received. Used for the request pipe GUI
	
	// opencomputers integration
	
	@Optional.Method(modid = "opencomputers")
	@Override
	public String getComponentName() {
		return "requestPipe";
	}
	
	@Optional.Method(modid = "opencomputers")
    @Callback(doc = "function(string:item, [integer:quantity=1, [string:filterType=\"DEFAULT\", [integer:meta=0, [string:nbtString=\"\"]]]]); Request something on the network")
    public Object[] request(Context context, Arguments args) throws Exception {
		String item = args.checkString(0);
		ItemStack stack = GameRegistry.makeItemStack(item, args.optInteger(3, 0), args.optInteger(1, 1), args.optString(4, ""));
		FilterConfig filter = new FilterConfig(stack, FilterType.fromString(args.optString(2, "DEFAULT")));
		this.network.request(this, filter);
		return null;
	}
	
	@Optional.Method(modid = "opencomputers")
    @Callback
    public Object[] getItemsStored(Context context, Arguments args) throws Exception {
    	List<Object> returns = new ArrayList<Object>();
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
