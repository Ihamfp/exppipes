package ihamfp.exppipes.tileentities;

import java.util.Map;

import ihamfp.exppipes.Utils;
import ihamfp.exppipes.items.ModItems;
import ihamfp.exppipes.pipenetwork.ItemDirection;
import ihamfp.exppipes.pipenetwork.Request;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.CapabilityItemHandler;

public class TileEntityProviderPipe extends TileEntityRoutingPipe {
	public boolean leaveOneItem() {
		for (int i=0; i<this.upgradesItemHandler.getSlots(); i++) {
			if (this.upgradesItemHandler.getStackInSlot(i).getItem() == ModItems.itemLockUpgrade) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void serverUpdate() {
		if (this.network != null && !this.network.providers.contains(this) && this.network.nodes.contains(this)) {
			this.network.providers.add(this);
		}
		if (this.network != null && this.network.requests.size() > 0) {
			Map<ItemStack,TileEntity> inventories = this.getInventories();
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
