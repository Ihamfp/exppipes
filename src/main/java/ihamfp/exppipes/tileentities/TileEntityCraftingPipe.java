package ihamfp.exppipes.tileentities;

import java.util.List;

import ihamfp.exppipes.items.ItemCraftingPattern;
import ihamfp.exppipes.pipenetwork.Request;
import ihamfp.exppipes.tileentities.pipeconfig.FilterConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.ItemStackHandler;

public class TileEntityCraftingPipe extends TileEntityRoutingPipe {
	public ItemStackHandler patternStorage = new ItemStackHandler(9) {
		@Override
		protected void onContentsChanged(int slot) {
			TileEntityCraftingPipe.this.markDirty();
		}
	};
	
	@Override
	public void serverUpdate() {
		if (this.network != null && !this.network.crafters.contains(this) && this.network.nodes.contains(this)) {
			this.network.crafters.add(this);
		}
		if (this.network != null && this.network.requests.size() > 0) {
			for (Request req : this.network.requests) {
				if (req.processedCount + req.processingCount.get() < req.requestedCount) { // if request not completed...
					for (int i=0; i<this.patternStorage.getSlots();i++) {
						if (this.patternStorage.getStackInSlot(i).isEmpty()) continue;
						List<ItemStack> results = ItemCraftingPattern.getPatternResults(this.patternStorage.getStackInSlot(i));
						for (ItemStack result : results) {
							if (result.isEmpty()) continue;
							if (req.filter.doesMatch(result)) { // start crafting !
								req.processingCount.addAndGet(result.getCount());
								List<FilterConfig> ingredients = ItemCraftingPattern.getPatternIngredients(this.patternStorage.getStackInSlot(i));
								for (FilterConfig ingFilter : ingredients) {
									if (ingFilter == null) continue;
									this.network.request(this, ingFilter, ingFilter.reference.getCount());
								}
								break; // only request 1
							}
						}
					}
				}
			}
		}
		super.serverUpdate();
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		this.patternStorage.deserializeNBT(compound.getCompoundTag("patternStorage"));
		super.readFromNBT(compound);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setTag("patternStorage", this.patternStorage.serializeNBT());
		return super.writeToNBT(compound);
	}
}
