package ihamfp.exppipes.tileentities;

import ihamfp.exppipes.Utils;
import ihamfp.exppipes.pipenetwork.ItemDirection;
import ihamfp.exppipes.pipenetwork.Request;
import ihamfp.exppipes.tileentities.pipeconfig.FilterConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.CapabilityItemHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("unused")
public class TileEntityPolyProviderPipe extends TileEntityProviderPipe {

	///WIP --- need more work on this, not fully keeping the filter set updated yet. Too Tired.

	protected List<FilterConfig> tempFilters;

	protected void ensureFilter(ItemStack itemStack) {
		if(!this.sinkConfig.filters.stream().anyMatch(f -> f.reference.isItemEqual(itemStack))){
			this.sinkConfig.filters.add( new FilterConfig(itemStack.copy(),0,false));
		}
	}

	protected void makeRemoveMap(FilterConfig filter,Map<ItemStack,TileEntity> inventories){
		AtomicReference<Boolean> found = new AtomicReference<>(false);
		inventories.forEach( (k,v)-> {
			if (filter.reference.isItemEqual(k)){
				found.set(true);
				return;
			}
		});
		if (!found.get()) tempFilters.add(filter);
	}

	protected void removeUnneededFilters(){
		tempFilters.forEach(f->this.sinkConfig.filters.remove(f));
	}

	@Override
	public void serverUpdate() {


		if (this.network != null && !this.network.providers.contains(this) && this.network.nodes.contains(this)) {
			this.network.providers.add(this);
		}

		Map<ItemStack,TileEntity> inventories = this.getInventories();
		inventories.forEach((stack,tile)->ensureFilter(stack));
		tempFilters = new ArrayList<>();
		this.sinkConfig.filters.forEach(filter->makeRemoveMap(filter,inventories));
		removeUnneededFilters();

		if (this.network != null && this.network.requests.size() > 0) {
			for (Request req : this.network.requests) {
				if (req.processedCount + req.processingCount.get() < req.requestedCount) { // if request not completed...
					int neededCount = req.requestedCount-(req.processedCount+req.processingCount.get());
					for (ItemStack stack : inventories.keySet()) {
						if (!stack.isEmpty() && req.filter.doesMatch(stack)) {
							TileEntity invTE = inventories.get(stack);
							EnumFacing extractFace = Utils.faceFromPos(this.pos, invTE.getPos());
							int maxExtract = stack.getCount()-(this.leaveOneItem()?1:0);
							ItemStack exStack = this.extractFrom(invTE.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, extractFace.getOpposite()), req.filter, Integer.min(neededCount, maxExtract));
							req.processingCount.addAndGet(exStack.getCount());
							neededCount -= exStack.getCount();
							this.itemHandler.insertedItems.add(new ItemDirection(exStack, extractFace, req.requester, this.world.getTotalWorldTime()));
							if (neededCount <= 0) break;
						}
					}
				}
			}
		}
		
		super.serverUpdate();
	}
}
