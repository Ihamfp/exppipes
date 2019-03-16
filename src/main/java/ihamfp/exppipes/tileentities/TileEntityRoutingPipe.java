package ihamfp.exppipes.tileentities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ihamfp.exppipes.ExppipesMod;
import ihamfp.exppipes.pipenetwork.ItemDirection;
import ihamfp.exppipes.pipenetwork.PipeNetwork;
import ihamfp.exppipes.pipenetwork.PipeNetwork.Request;
import ihamfp.exppipes.tileentities.pipeconfig.ConfigRoutingPipe;
import ihamfp.exppipes.tileentities.pipeconfig.FilterConfig;
import ihamfp.exppipes.tileentities.pipeconfig.FilterConfig.FilterType;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/***
 * Each routing pipe is a node in the network.
 */
@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")
public class TileEntityRoutingPipe extends TileEntityPipe implements SimpleComponent {
	public static int updateInterval = 60; // In ticks, interval between searchNodes() calls
	private int nextUpdate = 0; // decrement on each tick, update when 0 and reset to *updateInterval*
	
	// Used to store a list of nodes, providers, etc. and provide some network-wide functions
	public PipeNetwork network = null;
	
	// Purely cosmetic, used to set the block state
	@SideOnly(Side.CLIENT)
	private static final PipeNetwork fakeNetwork = new PipeNetwork();
	
	// Other nodes by direction
	public Map<EnumFacing, TileEntityRoutingPipe> connectedNodes = new HashMap<EnumFacing, TileEntityRoutingPipe>();
	public Map<EnumFacing, Integer> nodeDist = new HashMap<EnumFacing, Integer>();
	
	// this pipe's config
	public ConfigRoutingPipe sinkConfig = new ConfigRoutingPipe(); // for accepting items
	private String oldSinkConfig = "";
	// sourceConfig defined only for ProviderPipes. Crafting pipes are provider pipes too btw, they just report craftable things
	
	/// Methods
	
	/**
	 * Returns a map of the surrounding available inventories.
	 */
	public Map<ItemStack,TileEntity> getInventories() {
		Map<ItemStack,TileEntity> invs = new HashMap<ItemStack,TileEntity>(); // ItemStacks are all different objects (hopefully), but a TileEntity can hold many ItemStacks
		
		for (EnumFacing e : EnumFacing.VALUES) {
			TileEntity te = this.world.getTileEntity(this.pos.offset(e));
			if (te == null) continue;
			if (!te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, e.getOpposite())) continue;
			IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, e.getOpposite());
			if (handler instanceof PipeItemHandler || handler instanceof WrappedItemHandler) continue; // do not check pipes, prevents a lot of duplication
			
			for (int i=0; i<handler.getSlots();i++) {
				ItemStack inSlot = handler.extractItem(i, handler.getSlotLimit(i), true); // only check what can be extracted
				if (inSlot.isEmpty()) continue;
				if (invs.containsKey(inSlot)) break; // do not keep checking, it's probably a multiblock.
				invs.put(inSlot, te);
			}
		}
		
		return invs;
	}
	
	/***
	 * Get all connected nodes, on each side.
	 */
	public void searchNodes() {
		//ExppipesMod.logger.info("Searching other nodes from " + this.pos.toString());
		for (EnumFacing e : EnumFacing.VALUES) {
			List<BlockPos> otherPipes = new ArrayList<BlockPos>();
			BlockPos searchingAt = this.pos.offset(e);
			TileEntityRoutingPipe foundNode = null;
			EnumFacing foundFace = e.getOpposite(); // to mutually add nodes on face
			
			while (this.world.getTileEntity(searchingAt) instanceof TileEntityPipe) { // TODO integration with buildcraft, etc.
				TileEntity te = this.world.getTileEntity(searchingAt);
				if (te instanceof TileEntityRoutingPipe) { // found another node !
					foundNode = (TileEntityRoutingPipe) te;
					break;
				}
				
				boolean found = false;
				for (EnumFacing f : EnumFacing.VALUES) {
					BlockPos nextSearch = searchingAt.offset(f);
					if (otherPipes.contains(nextSearch)) continue; // prevent loops
					
					TileEntity nextTileEntity = this.world.getTileEntity(nextSearch);
					if (nextTileEntity instanceof TileEntityPipe && nextTileEntity != this) { // found another pipe !
						otherPipes.add(nextSearch);
						searchingAt = nextSearch;
						foundFace = f.getOpposite();
						found = true;
						break;
					}
				}
				if (!found) break; // ain't found nothing here
			}
			
			if (foundNode != null) {
				this.connectedNodes.put(e, foundNode);
				this.nodeDist.put(e, otherPipes.size());
				foundNode.connectedNodes.put(foundFace, this);
				foundNode.nodeDist.put(foundFace, otherPipes.size());
				
				if (this.network == null && foundNode.network != null) { // append this node to foundNode's network
					this.network = foundNode.network;
					this.network.nodes.add(this);
				} else if (this.network != null && foundNode.network == null) { // append foundNode to this node's network
					foundNode.network = this.network;
					this.network.nodes.add(foundNode);
				} else if (this.network == null && foundNode.network == null) { // create network
					this.network = new PipeNetwork();
					this.network.nodes.add(this);
					this.network.nodes.add(foundNode);
				} else if (this.network != null && foundNode.network != null && this.network != foundNode.network) { // merge networks
					this.network.nodes.addAll(foundNode.network.nodes);
					this.network.providers.addAll(foundNode.network.providers);
					this.network.requests.addAll(foundNode.network.requests);
					for (TileEntityRoutingPipe networkTE : this.network.nodes) {
						networkTE.network = this.network;
					}
					foundNode.network = this.network;
				}
			}
		}
		if (this.network != null) {
			ExppipesMod.logger.trace(this.pos.toString() + " now in network " + this.network.toString());
		} else {
			ExppipesMod.logger.trace(this.pos.toString() + " has no network");
		}
	}
	
	@Override
	public void serverUpdate() {
		IBlockState currentState = this.world.getBlockState(this.pos);
		if (this.nextUpdate <= 0) {
			this.searchNodes();
			this.nextUpdate = updateInterval;

			this.world.notifyBlockUpdate(this.pos, currentState, currentState, 2);
		}
		if (!this.sinkConfig.toString().equals(this.oldSinkConfig)) {
			this.oldSinkConfig = this.sinkConfig.toString();
			this.world.notifyBlockUpdate(this.pos, currentState, currentState, 2);
		}
		this.nextUpdate--;
		
		this.itemHandler.tick(this.world.getTotalWorldTime());
		if (this.network != null && this.network.nodes != null && this.network.nodes.get(0) == this) { // here: things to do once per tick on the network
			List<Request> toRemove = new ArrayList<Request>();
			for (Request req : this.network.requests) {
				if (req.completed || req.filter.stack.isEmpty()) {
					toRemove.add(req);
				}
			}
			this.network.requests.removeAll(toRemove);
		}
		
		for (ItemDirection i : this.itemHandler.storedItems) {
			if (i.itemStack == null) continue;
			ExppipesMod.logger.trace(" - " + i.itemStack.toString() + ", dest: " + ((i.destination != null)?(i.destination.getPos().toString()):"null") + ", now at " + this.pos.toString());
			// First, check if the item is already on its destination node
			if (i.destination == this && i.to == null) {
				ExppipesMod.logger.trace(i.itemStack.toString() + " arrived at destination @ " + this.getPos().toString());
				// check all sides for non-pipes inventories
				for (EnumFacing e : EnumFacing.VALUES) {
					BlockPos checking = this.pos.offset(e);
					if (this.world.getTileEntity(checking) == null) continue; // nothing here
					if (this.world.getTileEntity(checking) instanceof TileEntityPipe) continue; // pipe here
					if (!this.world.getTileEntity(checking).hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, e.getOpposite())) continue; // no inventory here
					
					i.to = e;
					break;
				}
			// Then, check if it should be redirected to the default route
			} else if (i.destination == null && this.network != null) {
				ExppipesMod.logger.trace(i.itemStack.toString() + " has no destination: sending to default route");
				i.destination = this.network.getDefaultRoute(i.itemStack);
				ExppipesMod.logger.trace("Destination found: " + ((i.destination!=null)?i.destination.getPos().toString():"null"));
			}
			
			// Finally, try to route the item properly
			if (this.network != null && i.destination != null && i.to == null) {
				EnumFacing path = this.network.getShortestFace(this, i.destination);
				if (path == null) {
					ExppipesMod.logger.trace("no route to " + i.destination.getPos().toString() + " from " + this.getPos().toString());
					i.destination = null; // destination either doesn't exist anymore or isn't reachable. Aborting.
					continue;
				}
				i.to = path;
			}
		}
		super.serverUpdate();
	}
	
	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
		if (oldState.getBlock() != newState.getBlock() && this.network != null) {
			// remove self from network
			this.network.removeNode(this);
			return true;
		}
		return super.shouldRefresh(world, pos, oldState, newState);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		this.sinkConfig.deserializeNBT(compound.getCompoundTag("config"));
		if (this.network != null && this.network.nodes.size() > 0 && this.network.nodes.get(0) == this) {
			this.network.deserializeNBT(compound.getCompoundTag("network"), getWorld());
		}
		super.readFromNBT(compound);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setTag("config", this.sinkConfig.serializeNBT());
		if (this.network != null && this.network.nodes.size() > 0 && this.network.nodes.get(0) == this) {
			compound.setTag("network", this.network.serializeNBT());
		}
		return super.writeToNBT(compound);
	}
	
	// What's sent to the client: network, Config
	@Override
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound nbtTag = super.getUpdateTag();
		if (!this.world.isRemote) { // sending from the server
			nbtTag.setBoolean("hasNetwork", this.network != null);
			nbtTag.setTag("config", this.sinkConfig.serializeNBT());
		}
		return nbtTag;
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		NBTTagCompound nbtTag = pkt.getNbtCompound();
		if (this.world.isRemote) { // receiving from the client
			this.sinkConfig.deserializeNBT(nbtTag.getCompoundTag("config"));
			if (nbtTag.getBoolean("hasNetwork")) {
				this.network = fakeNetwork;
			} else {
				this.network = null;
			}
		}
		super.onDataPacket(net, pkt);
	}
	
	////////// Begin OpenComputers integration

	@Optional.Method(modid = "opencomputers")
	@Override
	public String getComponentName() {
		return "routingPipe";
	}
	
	@Optional.Method(modid = "opencomputers")
	@Callback(doc = "function():boolean; True when the pipe is connected to a network")
    public Object[] hasNetwork(Context context, Arguments args) throws Exception {
		return new Object[] {this.network != null};
	}

    @Optional.Method(modid = "opencomputers")
    @Callback(doc = "function():...; Returns the sink configuration. Usage: `sink = {getSinkConfig()}`")
    public Object[] getSinkConfig(Context context, Arguments args) throws Exception {
		List<Object> returns = new ArrayList<Object>();
		for (FilterConfig filter : this.sinkConfig.computerFilters) {
			Map<String,Object> entry = new HashMap<String,Object>();
			entry.put("item", filter.stack.getItem().getRegistryName());
			entry.put("filterType", filter.filterType.toString());
			entry.put("priority", filter.priority);
			returns.add(entry);
			//returns.add(new Object[] {filter.stack.getItem().getRegistryName(), filter.stack.getMetadata(), filter.filterType.toString(), filter.priority});
		}
		return returns.toArray();
	}
    
    @Optional.Method(modid = "opencomputers")
    @Callback(doc = "function(string:item, [integer:quantity=1, [string:filterType=\"DEFAULT\", [integer:meta=0, [string:nbtString=\"\"]]]]):integer; Add a sink filter, returns filter ID.")
    public Object[] addSinkFilter(Context context, Arguments args) throws Exception {
    	String item = args.checkString(0);
		ItemStack stack = GameRegistry.makeItemStack(item, args.optInteger(3, 0), args.optInteger(1, 1), args.optString(4, ""));
		FilterConfig filter = new FilterConfig(stack, FilterType.fromString(args.optString(2, "DEFAULT")));
		this.sinkConfig.computerFilters.add(filter);
    	return new Object[] {this.sinkConfig.computerFilters.size()};
    }
    
    @Optional.Method(modid = "opencomputers")
    @Callback(doc = "function(integer:id); Removes a sink filter using its ID.")
    public Object[] removeSinkFilter(Context context, Arguments args) throws Exception {
    	int id = args.checkInteger(0);
    	this.sinkConfig.computerFilters.remove(id-1); // Lua index is 1-based
    	return null;
    }
    
    @Optional.Method(modid = "opencomputers")
    @Callback(doc = "function(); Completely clear the sink configuration.")
    public Object[] clearSinkConfig(Context context, Arguments args) throws Exception {
    	this.sinkConfig.computerFilters.clear();
    	return null;
    }
}
