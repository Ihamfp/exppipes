package ihamfp.exppipes.pipenetwork;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.common.collect.Iterables;

import ihamfp.exppipes.ExppipesMod;
import ihamfp.exppipes.items.ItemCraftingPattern;
import ihamfp.exppipes.tileentities.TileEntityCraftingPipe;
import ihamfp.exppipes.tileentities.TileEntityProviderPipe;
import ihamfp.exppipes.tileentities.TileEntityRoutingPipe;
import ihamfp.exppipes.tileentities.pipeconfig.FilterConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

public class PipeNetwork {
	public List<TileEntityRoutingPipe> nodes = new ArrayList<TileEntityRoutingPipe>();
	public TileEntityRoutingPipe defaultRoute = null;
	public List<TileEntityProviderPipe> providers = new ArrayList<TileEntityProviderPipe>();
	public List<TileEntityCraftingPipe> crafters = new ArrayList<TileEntityCraftingPipe>();
	
	public CopyOnWriteArrayList<Request> requests = new CopyOnWriteArrayList<Request>();
	
	/***
	 * Get the pipe to send an item with no route set to.
	 * @param stack the stack to get the destination for
	 * @return the destination
	 */
	public TileEntityRoutingPipe getDefaultRoute(ItemStack stack) {
		int priority = Integer.MIN_VALUE; // get the highest priority, start with the lowest
		List<TileEntityRoutingPipe> candidates = new ArrayList<TileEntityRoutingPipe>();
		
		// Check for requests first
		for (Request req : this.requests) {
			if (req.filter.doesMatch(stack) && req.processedCount < req.requestedCount) { // we got one !
				if (req.filter.priority > priority) {
					candidates.clear();
					candidates.add(req.requester);
					priority = req.filter.priority;
				} else if (req.filter.priority == priority) {
					candidates.add(req.requester);
				}
			}
		}
		if (candidates.size() > 0) {
			Collections.shuffle(candidates);
			return candidates.get(0);
		}
		
		if (this.defaultRoute != null && !this.defaultRoute.isInvalid()) candidates.add(this.defaultRoute); 
		priority = Integer.MIN_VALUE; // start again !
		List<TileEntityRoutingPipe> notBlacklisted = new ArrayList<TileEntityRoutingPipe>(); // un-prioritized pipe where the stack isn't blacklisted
		for (TileEntityRoutingPipe pipe : this.nodes) {
			boolean valid = false;
			boolean blacklisted = false;
			int maxPriority = priority;
			
			for (FilterConfig filter : Iterables.concat(pipe.sinkConfig.filters, pipe.sinkConfig.computerFilters)) {
				boolean didMatch = filter.doesMatch(stack);
				if (didMatch && !filter.blacklist && pipe.canInsert(stack)) {
					if (filter.priority > maxPriority) {
						maxPriority = filter.priority;
					}
					valid = true;
				} else if (!didMatch && filter.blacklist && filter.priority >= maxPriority) {
					valid = false;
					blacklisted = true;
					maxPriority = filter.priority;
				}
			}
			
			if (valid) {
				if (maxPriority > priority) {
					priority = maxPriority;
					candidates.clear();
				}
				candidates.add(pipe);
			}
			if (!blacklisted && pipe.canInsert(stack)) {
				notBlacklisted.add(pipe);
			}
		}
		
		if (candidates.size() == 0) {
			if (notBlacklisted.size() > 0) {
				Collections.shuffle(notBlacklisted);
				return notBlacklisted.get(0);
			}
			return null;
		}
		
		Collections.shuffle(candidates);
		return candidates.get(0);
	}
	
	// Could have been done with Thorup algorithm, but I both don't understand shit about it and couldn't find any free article.
	// ~~So I used Dijkstra~~
	// TODO: shortest path, the current algorithm only checks for lowest amount of nodes
	public EnumFacing getShortestFace(TileEntityRoutingPipe source, TileEntityRoutingPipe dest) {
		if (dest == null || source == null) return null;
		if (source == dest) return null;
		if (source.network != dest.network) return null;
		
		// How it works:
		//  * Start from destination
		//  * Try to propagate to source, one level at a time
		//  * When source is found, return the side to the node who found it
		List<TileEntityRoutingPipe> visitedNodes = new ArrayList<TileEntityRoutingPipe>();
		visitedNodes.add(dest);
		List<TileEntityRoutingPipe> buff = new ArrayList<TileEntityRoutingPipe>();
		while (!visitedNodes.contains(source)) {
			buff.clear();
			for (TileEntityRoutingPipe v : visitedNodes) {
				for (EnumFacing f : v.connectedNodes.keySet()) {
					TileEntityRoutingPipe c = v.connectedNodes.get(f);
					
					if (c == source) { // Found the source ! Now get which side to send the item to
						for (EnumFacing fSource : source.connectedNodes.keySet()) {
							if (source.connectedNodes.get(fSource) == v) {
								return fSource;
							}
						}
					}
					
					if (!visitedNodes.contains(c) && !buff.contains(c)) {
						buff.add(c);
					}
				}
			}
			visitedNodes.addAll(buff);
			if (buff.size() == 0 && !visitedNodes.contains(source)) {
				ExppipesMod.logger.error("Node is in network but not connected to it");
				return null;
			}
		}
		
		ExppipesMod.logger.error("Found source but no path from " + source.getPos().toString() + "to " + dest.getPos().toString());
		
		return null;
	}
	
	public void removeNode(TileEntityRoutingPipe node) {
		ExppipesMod.logger.trace("Removing " + node.getPos().toString() + " from network");
		nodes.remove(node);
		providers.remove(node);
		for (TileEntityRoutingPipe pipe : node.connectedNodes.values()) { // for all the nodes connected to he one to remove...
			List<EnumFacing> foundFace = new ArrayList<EnumFacing>();
			for (EnumFacing f : pipe.connectedNodes.keySet()) { // search the face connected to the node to remove
				if (pipe.connectedNodes.get(f) == node) { // and if it's this one,
					foundFace.add(f); // remove it
				}
			}
			for (EnumFacing f : foundFace) {
				pipe.connectedNodes.remove(f);
			}
		}
		
		List<Request> toRemove = new ArrayList<Request>();
		for (Request r : this.requests) { // remove all not-handled requests made by the node
			toRemove.add(r);
		}
		this.requests.removeAll(toRemove);
	}
	
	/*
	 * Use advanced routing techniques such as "nuke everything and rebuild" to split a network.
	 * Should be called before nodes update.
	 * Handles the special cases with 1 node, no network, etc.
	 */
	public static void split(List<TileEntityRoutingPipe> src) {
		if (src.size() < 2) return; // wtf bro, nothing ?
		// check if all networks are the same and non-null
		for (TileEntityRoutingPipe src1 : src) {
			if (src1.network == null) return; // nothing to do
			for (TileEntityRoutingPipe src2 : src) {
				if (src1.network != src2.network) return; // nothing to do either
			}
		}
		// create new networks
		PipeNetwork original = src.get(0).network;
		for (TileEntityRoutingPipe pipe : original.nodes) {
			pipe.network = null; // nuke the network
		}
		for (TileEntityRoutingPipe pipe : src) {
			PipeNetwork newNetwork = new PipeNetwork(); // partially rebuild the network
			newNetwork.nodes.add(pipe);
			newNetwork.requests.addAll(original.requests);
			pipe.network = newNetwork;
		}
	}
	
	public List<ItemStack> globalInventory() {
		List<ItemStack> itemStacks = new ArrayList<ItemStack>();
		for (TileEntityProviderPipe p : this.providers) {
			itemStacks.addAll(p.getInventories().keySet());
		}
		return itemStacks;
	}
	
	/**
	 * For use in request GUIs or such.
	 * A value of 0 means "available to craft"
	 * @return map with ItemStack as a key and the available amount as value
	 */
	public Map<ItemStack,Integer> condensedInventory() {
		List<ItemStack> storedStacks = this.globalInventory();
		Map<ItemStack,Integer> condInv = new HashMap<ItemStack,Integer>();
		for (ItemStack storedStack : storedStacks) {
			ItemStack matched = null;
			for (ItemStack keyStack : condInv.keySet()) {
				if (ItemStack.areItemsEqual(storedStack, keyStack) && ItemStack.areItemStackTagsEqual(storedStack, keyStack)) {
					matched = keyStack;
				}
			}
			
			if (matched == null) {
				ItemStack keyStack = storedStack.copy();
				keyStack.setCount(1);
				condInv.put(keyStack, storedStack.getCount());
			} else {
				condInv.put(matched, condInv.get(matched) + storedStack.getCount());
			}
		}
		for (TileEntityCraftingPipe pipe : this.crafters) {
			for (int i=0; i<pipe.patternStorage.getSlots();i++) {
				List<ItemStack> patternResults = ItemCraftingPattern.getPatternResults(pipe.patternStorage.getStackInSlot(i));
				for (ItemStack patternResult : patternResults) {
					if (patternResult.isEmpty()) continue;
					boolean added = false;
					for (ItemStack keyStack : condInv.keySet()) {
						if (ItemStack.areItemsEqual(patternResult, keyStack) && ItemStack.areItemStackTagsEqual(patternResult, keyStack)) {
							added = true;
						}
					}
					if (!added) condInv.put(patternResult, 0);
				}
			}
		}
		return condInv;
	}
	
	public Request request(TileEntityRoutingPipe requester, FilterConfig filter, int count) {
		Request req = new Request(requester, filter, count);
		
		if (this.requests.size() == 0) { // no request, just add one
			this.requests.add(req);
			return req;
		}
		
		// insert before all request with inferior priority, after all requests with superior priority
		boolean added = false;
		for (int i=0; i<this.requests.size()-1; i++) {
			if (this.requests.get(i).filter.priority > filter.priority && this.requests.get(i+1).filter.priority <= filter.priority) {
				this.requests.add(i, req);
				added = true;
			}
		}
		if (!added) this.requests.add(req);
		return req;
	}
	
	public void endRequest(Request req) {
		this.requests.remove(req);
	}
}
