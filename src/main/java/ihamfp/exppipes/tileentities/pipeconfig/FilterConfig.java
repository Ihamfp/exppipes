package ihamfp.exppipes.tileentities.pipeconfig;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

public class FilterConfig implements INBTSerializable<NBTTagCompound> {
	/***
	 * The item to filter/provide/supply/etc.
	 */
	public ItemStack reference;
	/***
	 * The filter to apply to the insertion/requesting/providing/etc.
	 */
	public int filterId;
	/***
	 * Whether or not the filter is a blacklist
	 */
	public boolean blacklist;
	/***
	 * The priority of the default destination/request/etc.
	 * The higher the value the higher the priority. Set to {@link Integer.MAX_VALUE} for absolute priority
	 */
	public int priority = 0;
	
	public FilterConfig(ItemStack stack, int filterId, boolean blacklist) {
		this(stack, filterId, blacklist, 0);
	}
	
	public FilterConfig(ItemStack stack, int filterId, boolean blacklist, int priority) {
		if (stack.getCount() == 0) stack.setCount(1); // prevents replacement with minecraft:air on save/reload
		this.reference = stack;
		this.filterId = filterId;
		this.blacklist = blacklist;
		this.priority = priority;
	}
	
	public FilterConfig(NBTTagCompound nbt) {
		this(new ItemStack(nbt), nbt.getInteger("filterType"), nbt.getBoolean("blacklist"), nbt.getInteger("priority"));
	}
	
	/***
	 * True if the stack passes the filter.
	 * {@link Items.AIR} means "everything"
	 */
	public boolean doesMatch(ItemStack stack) {
		if (this.reference.isEmpty() || this.reference.getItem() == Items.AIR) {
			return !this.blacklist;
		}
		if (this.filterId >= 0 && Filters.filters.size() > this.filterId) {
			Filter f = Filters.filters.get(this.filterId);
			return this.blacklist?!f.doesMatch(this.reference, stack):f.doesMatch(this.reference, stack);
		}
		return false;
	}

	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound tag = new NBTTagCompound();
		this.reference.writeToNBT(tag);
		tag.setInteger("filterType", this.filterId);
		tag.setInteger("priority", this.priority);
		tag.setBoolean("blacklist", this.blacklist);
		return tag;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		this.reference = new ItemStack(nbt);
		this.filterId = nbt.getInteger("filterType");
		if (this.filterId >= Filters.filters.size()) this.filterId = 0;
		this.priority = nbt.getInteger("priority");
		this.blacklist = nbt.getBoolean("blacklist");
	}
	
	@Override
	public String toString() {
		return (Filters.filters.get(this.filterId).getLongName() + (this.blacklist?" (blacklist)":"") + ": " + this.reference.toString());
	}
}