package ihamfp.exppipes.tileentities;

import ihamfp.exppipes.items.ModItems;
import ihamfp.exppipes.pipenetwork.BlockDimPos;
import ihamfp.exppipes.tileentities.pipeconfig.FilterConfig;
import net.minecraft.item.ItemStack;

public class TileEntityRetrieverPipe extends TileEntitySupplierPipe {
	
	static final FilterConfig everything = new FilterConfig(new ItemStack(ModItems.pipeDebug, 1), 0, true, 0); // Nobody ain't ever use debug
	
	@Override
	public void serverUpdate() {
		super.serverUpdate();
		
		if (this.requests.size() == 0 && this.supplyConfig.filters.size() == 0) {
			FilterConfig reqFilter = this.insertableMatchingFilter(everything);
			if (reqFilter != null) this.requests.put(everything, this.network.request(new BlockDimPos(this), reqFilter, 1));
		}
	}
}
