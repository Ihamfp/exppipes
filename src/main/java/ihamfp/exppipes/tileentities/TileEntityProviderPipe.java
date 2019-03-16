package ihamfp.exppipes.tileentities;

import java.util.Map;

import ihamfp.exppipes.ExppipesMod;
import ihamfp.exppipes.Utils;
import ihamfp.exppipes.pipenetwork.ItemDirection;
import ihamfp.exppipes.pipenetwork.PipeNetwork.Request;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.CapabilityItemHandler;

public class TileEntityProviderPipe extends TileEntityRoutingPipe {
	@Override
	public void serverUpdate() {
		if (this.network != null && !this.network.providers.contains(this) && this.network.nodes.contains(this)) {
			//ExppipesMod.logger.info("Attached provider @ " + this.pos.toString() + " to network " + this.network.toString());
			this.network.providers.add(this);
		}
		// TODO handle partial requests
		if (this.network != null && this.network.requests.size() > 0) {
			Map<ItemStack,TileEntity> inventories = this.getInventories(); // get this one only once, to speed it up
			for (Request r : this.network.requests) { // scan through all the current requests
				if (r.handled.get() == false) { // check if it has not been handled already
					for (ItemStack stack : inventories.keySet()) { // scan through all available items
						if (r.filter.doesMatch(stack) && r.filter.stack.getCount() <= stack.getCount() && inventories.get(stack) != r.requester) { // possible candidate
							if (r.handled.getAndSet(true)) break; // someone else was faster... abort
							
							EnumFacing dir = Utils.faceFromPos(this.pos, inventories.get(stack).getPos());
							ItemStack exStack;
							if (dir == null) {
								ExppipesMod.logger.info("[" + this.toString() + "]Extracting from a weird place: " + inventories.get(stack));
								exStack = this.extractFrom(inventories.get(stack).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null), r.filter);
							} else {
								exStack = this.extractFrom(inventories.get(stack).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir.getOpposite()), r.filter);
							}
							ItemDirection itemDir = new ItemDirection(exStack, dir, r.requester, this.world.getTotalWorldTime());
							itemDir.request = r;
							this.itemHandler.insertedItems.add(itemDir);
							r.completed = true;
						}
					}
				}
			}
		}
		super.serverUpdate();
	}
}
