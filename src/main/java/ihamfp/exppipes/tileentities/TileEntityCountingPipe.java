package ihamfp.exppipes.tileentities;

import java.util.ArrayList;
import java.util.List;

import ihamfp.exppipes.pipenetwork.ItemDirection;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.Optional;

@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")
public class TileEntityCountingPipe extends TileEntityPipe implements SimpleComponent {
	public int count = 0;
	
	public void countInsertedItems() {
		List<ItemDirection> insertedItems = new ArrayList<ItemDirection>();
		insertedItems.addAll(this.itemHandler.insertedItems);
		for (ItemDirection itemDir : insertedItems) {
			this.count += itemDir.itemStack.getCount();
		}
	}
	
	@Override
	public void serverUpdate() {
		this.countInsertedItems();
		super.serverUpdate();
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		this.count = compound.getInteger("count");
		super.readFromNBT(compound);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		this.countInsertedItems();
		compound.setInteger("count", count);
		return super.writeToNBT(compound);
	}
	
	//////////Begin OpenComputers integration
	
	@Optional.Method(modid = "opencomputers")
	@Override
	public String getComponentName() {
		return "countingPipe";
	}
	
	@Optional.Method(modid = "opencomputers")
	@Callback(doc = "function():integer; The number of items that traveled through the pipe")
    public Object[] count(Context context, Arguments args) throws Exception {
		return new Object[] {this.count};
	}
	
	@Optional.Method(modid = "opencomputers")
	@Callback(doc = "function():nil; Reset the counter")
    public Object[] reset(Context context, Arguments args) throws Exception {
		this.count = 0;
		return new Object[] {null};
	}
}
