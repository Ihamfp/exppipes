package ihamfp.exppipes.tileentities.pipeconfig;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

public class ConfigRoutingPipe implements INBTSerializable<NBTTagCompound> {
	public List<FilterConfig> filters = new ArrayList<FilterConfig>();
	
	public List<FilterConfig> computerFilters = new ArrayList<FilterConfig>(); // used with opencomputers to separate player and computer filters
	
	public boolean doesMatchAllFilters(ItemStack stack) {
		if (stack.isEmpty()) return false;
		for (FilterConfig filter : this.filters) {
			if (!filter.doesMatch(stack)) return false;
		}
		for (FilterConfig filter : this.computerFilters) {
			if (!filter.doesMatch(stack)) return false;
		}
		return true;
	}
	
	public boolean doesMatchAnyFilter(ItemStack stack) {
		if (stack.isEmpty()) return false;
		for (FilterConfig filter : this.filters) {
			if (filter.doesMatch(stack)) return true;
		}
		for (FilterConfig filter : this.computerFilters) {
			if (filter.doesMatch(stack)) return true;
		}
		return false;
	}

	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagList filterList = new NBTTagList();
		for (FilterConfig filter : this.filters) {
			NBTTagCompound entry = filter.serializeNBT();
			entry.setBoolean("computer", false);
			
			filterList.appendTag(entry);
		}
		for (FilterConfig filter : this.computerFilters) {
			NBTTagCompound entry = filter.serializeNBT();
			entry.setBoolean("computer", true);
			
			filterList.appendTag(entry);
		}
		
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setTag("filters", filterList);
		return nbt;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		NBTTagList filterList = nbt.getTagList("filters", Constants.NBT.TAG_COMPOUND);
		
		this.filters.clear();
		for (int i=0; i<filterList.tagCount();i++) {
			NBTTagCompound entry = filterList.getCompoundTagAt(i);
			if (entry.hasKey("computer") && entry.getBoolean("computer")) {
				this.computerFilters.add(new FilterConfig(entry));
			} else {
				this.filters.add(new FilterConfig(entry));
			}
		}
	}
}
