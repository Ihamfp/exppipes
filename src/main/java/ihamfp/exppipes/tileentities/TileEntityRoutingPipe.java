package ihamfp.exppipes.tileentities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ihamfp.exppipes.ExppipesMod;
import ihamfp.exppipes.common.Configs;
import ihamfp.exppipes.pipenetwork.BlockDimPos;
import ihamfp.exppipes.pipenetwork.ItemDirection;
import ihamfp.exppipes.pipenetwork.PipeNetwork;
import ihamfp.exppipes.tileentities.pipeconfig.ConfigRoutingPipe;
import ihamfp.exppipes.tileentities.pipeconfig.FilterConfig;
import ihamfp.exppipes.tileentities.pipeconfig.Filters;
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
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

/***
 * Each routing pipe is a node in the network.
 */
@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")
public class TileEntityRoutingPipe extends TileEntityPipe implements SimpleComponent {
	private int nextUpdate = 0; // decrement on each tick, update when 0 and reset to *updateInterval*
	
	// Used to store a list of nodes, providers, etc. and provide some network-wide functions
	public PipeNetwork network = null;

	public boolean isDefaultRoute = false;
	
	// Purely cosmetic, used to set the block state
	private static final PipeNetwork fakeNetwork = new PipeNetwork();
	
	// Other nodes by direction
	//@Deprecated
	//public Map<EnumFacing, TileEntityRoutingPipe> connectedNodes = new HashMap<EnumFacing, TileEntityRoutingPipe>();
	public Map<EnumFacing, BlockDimPos> connectedNodesPos = new HashMap<EnumFacing, BlockDimPos>();
	public Map<EnumFacing, Integer> nodeDist = new HashMap<EnumFacing, Integer>(); // unused for now
	
	// this pipe's config
	public ConfigRoutingPipe sinkConfig = new ConfigRoutingPipe(); // for accepting items
	private String oldSinkConfig = "";
	
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
				//ItemStack inSlot = handler.extractItem(i, handler.getSlotLimit(i), true); // only check what can be extracted
				ItemStack inSlot = handler.getStackInSlot(i);
				if (inSlot.isEmpty()) continue;
				if (invs.containsKey(inSlot)) break; // do not keep checking, it's probably a multiblock.
				invs.put(inSlot, te);
			}
		}
		
		return invs;
	}
	
	public boolean isPipe(TileEntity te) {
		return (te instanceof TileEntityPipe);
	}
	
	public boolean isPipe(BlockPos pos) {
		return isPipe(this.world.getTileEntity(pos));
	}
	
	/***
	 * Get all connected nodes, on each side.
	 */
	public void searchNodes() {
		this.connectedNodesPos.clear();
		
		List<BlockPos> checkedPipes = new ArrayList<BlockPos>();
		checkedPipes.add(this.pos);
		for (EnumFacing e : EnumFacing.VALUES) { // check this pipe's faces
			int jumpCount = 0;
			BlockPos currentPos = this.pos.offset(e);
			
			if (checkedPipes.contains(currentPos)) continue; // loop
			if (!isPipe(currentPos)) continue; // nothing on this face
			checkedPipes.add(currentPos);
			
			TileEntityRoutingPipe foundNode = null;
			EnumFacing foundFace = null;
			boolean foundNext = true;
			
			if (this.world.getTileEntity(currentPos) instanceof TileEntityRoutingPipe) {
				foundNode = (TileEntityRoutingPipe) this.world.getTileEntity(currentPos);
				foundFace = e.getOpposite();
			} else {
				while (foundNext && foundNode == null) { // follow the conduit
					foundNext = false;
					for (EnumFacing f : EnumFacing.VALUES) { // search for pipes connected to the currently checked pipe
						BlockPos nextPos = currentPos.offset(f);
						if (checkedPipes.contains(nextPos)) continue; // going backward
						TileEntity nextTE = this.world.getTileEntity(nextPos);
						if (nextTE != null && nextTE instanceof TileEntityRoutingPipe) { // found a routing pipe !
							foundNode = (TileEntityRoutingPipe) nextTE;
							foundFace = f.getOpposite();
							break;
						} else if (isPipe(nextTE)) { // found a normal pipe
							checkedPipes.add(nextPos);
							currentPos = nextPos;
							foundNext = true;
							jumpCount++;
							break;
						}
					}
				}
			}
			
			if (foundNode != null) {
				this.connectedNodesPos.put(e, new BlockDimPos(foundNode));
				this.nodeDist.put(e, jumpCount);
				foundNode.connectedNodesPos.put(foundFace, new BlockDimPos(this));
				foundNode.nodeDist.put(foundFace, jumpCount);
				
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
				} else if (this.network != null && foundNode.network != null && !this.network.equals(foundNode.network)) { // merge networks
					this.network.nodes.addAll(foundNode.network.nodes);
					this.network.providers.addAll(foundNode.network.providers);
					this.network.requests.addAll(foundNode.network.requests);
					for (TileEntityRoutingPipe networkTE : this.network.nodes) {
						networkTE.network = this.network;
					}
					foundNode.network = this.network;
				} else if (this.network != foundNode.network) { // should never happen
					ExppipesMod.logger.error("Unsupported merging");
				}
			}
		}
	}
	
	@Override
	public void serverUpdate() {
		IBlockState currentState = this.world.getBlockState(this.pos);
		if (this.nextUpdate <= 0) {
			this.searchNodes();
			this.nextUpdate = Configs.updateInterval;

			this.world.notifyBlockUpdate(this.pos, currentState, currentState, 2);
		}
		if (!this.sinkConfig.toString().equals(this.oldSinkConfig)) {
			this.oldSinkConfig = this.sinkConfig.toString();
			this.world.notifyBlockUpdate(this.pos, currentState, currentState, 2);
		}
		this.nextUpdate--;
		
		if (this.isDefaultRoute && this.network != null && this.network.defaultRoute != this) {
			if (this.network.defaultRoute == null) {
				this.network.defaultRoute = this;
			} else {
				this.isDefaultRoute = false;
			}
		}
		
		this.itemHandler.tick(this.world.getTotalWorldTime());
		
		for (ItemDirection i : this.itemHandler.storedItems) {
			if (i.itemStack == null) continue;
			// First, check if the item is already on its destination node
			if (i.destinationPos != null && i.destinationPos.isHere(this) && i.to == null) {
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
			} else if (i.destinationPos == null && this.network != null) {
				i.destinationPos = new BlockDimPos(this.network.getDefaultRoute(i.itemStack));
			}
			
			// Finally, try to route the item properly
			if (this.network != null && i.destinationPos != null && i.to == null) {
				EnumFacing path = this.network.getShortestFace(this, i.destinationPos.getTE());
				if (path == null) {
					i.destinationPos = null; // destination either doesn't exist anymore or isn't reachable. Aborting.
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
		this.isDefaultRoute = compound.getBoolean("isdefaultroute");
		super.readFromNBT(compound);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setTag("config", this.sinkConfig.serializeNBT());
		compound.setBoolean("isdefaultroute", this.isDefaultRoute);
		return super.writeToNBT(compound);
	}
	
	// What's sent to the client: network, Config
	@Override
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound nbtTag = super.getUpdateTag();
		if (!this.world.isRemote) { // sending from the server
			nbtTag.setBoolean("hasNetwork", this.network != null);
			nbtTag.setTag("config", this.sinkConfig.serializeNBT());
			nbtTag.setBoolean("isdefaultroute", this.isDefaultRoute);
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
			this.isDefaultRoute = nbtTag.getBoolean("isdefaultroute");
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
			entry.put("item", filter.reference.getItem().getRegistryName());
			entry.put("filterType", Filters.filters.get(filter.filterId).getShortName());
			entry.put("priority", filter.priority);
			returns.add(entry);
			//returns.add(new Object[] {filter.stack.getItem().getRegistryName(), filter.stack.getMetadata(), filter.filterType.toString(), filter.priority});
		}
		return returns.toArray();
	}
    
    @Optional.Method(modid = "opencomputers")
    @Callback(doc = "function(string:item, [string:filterType=\"D\", [boolean:blacklist=false, [integer:meta=0, [string:nbtString=\"\"]]]]):integer; Add a sink filter, returns filter ID.")
    public Object[] addSinkFilter(Context context, Arguments args) throws Exception {
    	String item = args.checkString(0);
		ItemStack stack = GameRegistry.makeItemStack(item, args.optInteger(3, 0), 1, args.optString(4, ""));
		FilterConfig filter = new FilterConfig(stack, Filters.idFromShortString(args.optString(1, "D")), args.optBoolean(2, false));
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
