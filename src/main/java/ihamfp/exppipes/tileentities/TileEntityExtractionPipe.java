package ihamfp.exppipes.tileentities;

import ihamfp.exppipes.common.Configs;
import ihamfp.exppipes.pipenetwork.ItemDirection;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class TileEntityExtractionPipe extends TileEntityRoutingPipe {
	private long lastExtract = 0;
	
	@Override
	public void serverUpdate() {
		if (this.lastExtract+Configs.extractTime <= this.world.getTotalWorldTime()) {
			this.lastExtract = this.world.getTotalWorldTime();
			for (EnumFacing f : EnumFacing.VALUES) {
				TileEntity te = this.world.getTileEntity(this.pos.offset(f));
				if (te == null || te instanceof TileEntityPipe) continue;
				if (!te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, f.getOpposite())) continue;
				
				IItemHandler itemHandler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, f.getOpposite());
				
				ItemStack extracted = null;
				for (int i=0; i<itemHandler.getSlots(); i++) {
					if (!itemHandler.extractItem(i, 1, true).isEmpty()) {
						extracted = itemHandler.extractItem(i, 1, false);
					}
				}
				
				if (extracted == null || extracted.isEmpty()) continue;
				
				this.itemHandler.insertedItems.add(new ItemDirection(extracted, f, null, this.world.getTotalWorldTime()));
				break;
			}
		}
		super.serverUpdate();
	}
}
