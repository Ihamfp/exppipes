package ihamfp.exppipes.tileentities.pipeconfig;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.oredict.OreDictionary;

public class FilterConfig implements INBTSerializable<NBTTagCompound> {
	/***
	 * DEFAULT: match on ItemStack.areItemsEqual (Item and meta ==)
	 * FUZZY: match on Item == only
	 * STRICT: match on NBT tags as well
	 */
	public enum FilterType {
		DEFAULT("D"),
		FUZZY("F"),
		STRICT("S"),
		OREDICT("O"),
		OREDICT_STRICT("OS"),
		
		BLACKLIST("B"),
		FUZZY_BLACKLIST("FB"),
		STRICT_BLACKLIST("SB"),
		OREDICT_BLACKLIST("OB"),
		OREDICT_STRICT_BLACKLIST("OSB");
		
		private String shortName;
		
		private FilterType(String shortName) {
			this.shortName = shortName;
		}
		
		public static FilterType fromString(String s) {
			for (FilterType f : FilterType.values()) {
				if (f.toString().equals(s)) return f;
			}
			return DEFAULT;
		}
		
		public static FilterType fromShortString(String s) {
			for (FilterType f : FilterType.values()) {
				if (f.shortName.equals(s)) return f;
			}
			return DEFAULT;
		}
		
		public String getShortName() {
			return this.shortName;
		}
	}
	
	/***
	 * The item to filter/provide/supply/etc.
	 */
	public ItemStack stack;
	/***
	 * The filter to apply to the insertion/requesting/providing/etc.
	 */
	public FilterType filterType;
	/***
	 * The priority of the default destination/request/etc.
	 * The higher the value the higher the priority. Set to {@link Integer.MAX_VALUE} for absolute priority
	 */
	public int priority = 0;
	
	public FilterConfig(ItemStack stack, FilterType filterType) {
		this.stack = stack;
		if (stack.getCount() == 0) stack.setCount(1); // prevents replacement with minecraft:air on save/reload
		this.filterType = filterType;
	}
	
	public FilterConfig(ItemStack stack, FilterType filterType, int priority) {
		this(stack, filterType); // "Learn java. Just the basics, 5 minutes of learning java and you will understand how horribly wrong that is on every level" -- Cadiboo, not about my code, on the forge forum (post 323626)
		this.priority = priority;
	}
	
	public FilterConfig(NBTTagCompound nbt) {
		this(new ItemStack(nbt), FilterType.values()[nbt.getByte("filterType")], nbt.getInteger("priority"));
	}
	
	/***
	 * True if the stack passes the filter.
	 * {@link Items.AIR} means "everything"
	 * TODO possibility for filter API
	 */
	public boolean doesMatch(ItemStack stack) {
		if (this.stack.isEmpty() || this.stack.getItem() == Items.AIR) {
			switch(this.filterType) {
			case DEFAULT:
			case FUZZY:
			case STRICT:
			case OREDICT:
			case OREDICT_STRICT:
				return true;
			case BLACKLIST:
			case FUZZY_BLACKLIST:
			case STRICT_BLACKLIST:
			case OREDICT_BLACKLIST:
			case OREDICT_STRICT_BLACKLIST:
				return false;
			}
		}
		switch (this.filterType) {
		case DEFAULT:
			return (this.stack.isItemEqual(stack));
		case FUZZY:
			return (this.stack.getItem() == stack.getItem());
		case STRICT:
			return (this.stack.isItemEqual(stack) && ItemStack.areItemStackTagsEqual(this.stack, stack));
		case OREDICT:
			return OreDictionary.itemMatches(this.stack, stack, false);
		case OREDICT_STRICT:
			return OreDictionary.itemMatches(this.stack, stack, true);
		case BLACKLIST:
			return !(this.stack.isItemEqual(stack));
		case FUZZY_BLACKLIST:
			return !(this.stack.getItem() == stack.getItem());
		case STRICT_BLACKLIST:
			return !(this.stack.isItemEqual(stack) && ItemStack.areItemStackTagsEqual(this.stack, stack));
		case OREDICT_BLACKLIST:
			return !OreDictionary.itemMatches(this.stack, stack, false);
		case OREDICT_STRICT_BLACKLIST:
			return OreDictionary.itemMatches(this.stack, stack, true);
		default:
			return false;
		}
	}

	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound tag = new NBTTagCompound();
		this.stack.writeToNBT(tag);
		tag.setByte("filterType", (byte)this.filterType.ordinal());
		tag.setInteger("priority", this.priority);
		return tag;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		this.stack = new ItemStack(nbt);
		this.filterType = FilterType.values()[nbt.getByte("filterType")];
		this.priority = nbt.getInteger("priority");
	}
}