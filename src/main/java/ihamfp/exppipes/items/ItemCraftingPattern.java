package ihamfp.exppipes.items;

import java.util.ArrayList;
import java.util.List;

import ihamfp.exppipes.ExppipesMod;
import ihamfp.exppipes.ModCreativeTabs;
import ihamfp.exppipes.tileentities.pipeconfig.FilterConfig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

public class ItemCraftingPattern extends Item {
	public ItemCraftingPattern(String id) {
		this.setRegistryName(ExppipesMod.MODID, id);
		this.setMaxStackSize(1);
		this.setCreativeTab(ModCreativeTabs.PIPES);
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		ItemStack heldStack = playerIn.getHeldItem(handIn);
		if (playerIn.world.isRemote) return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, heldStack);
			
		if (playerIn.isSneaking()) { // erase pattern
			heldStack.setTagCompound(new NBTTagCompound());
		} else {
			playerIn.openGui(ExppipesMod.instance, 1001, playerIn.world, playerIn.inventory.currentItem, 0, 0);
		}
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, heldStack);
	}
	
	public static void setPatternResults(ItemStack pattern, List<ItemStack> results) {
		NBTTagList resultTags = new NBTTagList();
		for (ItemStack result : results) {
			resultTags.appendTag(result.serializeNBT());
		}
		pattern.setTagInfo("results", resultTags);
	}
	
	public static void setPatternIngredients(ItemStack pattern, List<FilterConfig> ingredients) {
		NBTTagList ingredientsTags = new NBTTagList();
		NBTTagCompound emptyIngredient = new NBTTagCompound();
		emptyIngredient.setBoolean("empty", true);
		for (FilterConfig ingredient : ingredients) {
			if (ingredient == null) {
				ingredientsTags.appendTag(emptyIngredient);
			} else {
				ingredientsTags.appendTag(ingredient.serializeNBT());
			}
		}
		pattern.setTagInfo("ingredients", ingredientsTags);
	}
	
	public static List<ItemStack> getPatternResults(ItemStack pattern) {
		if (!(pattern.getItem() instanceof ItemCraftingPattern)) return new ArrayList<ItemStack>();
		
		NBTTagCompound patternTags = pattern.getTagCompound();
		if (patternTags == null) return new ArrayList<ItemStack>();
		
		NBTTagList resultTags = patternTags.getTagList("results", NBT.TAG_COMPOUND);
		
		List<ItemStack> resultList = new ArrayList<ItemStack>();
		for (int i=0; i<resultTags.tagCount(); i++) {
			resultList.add(new ItemStack(resultTags.getCompoundTagAt(i)));
		}
		
		return resultList;//resultList.toArray(new ItemStack[0]);
	}
	
	public static List<FilterConfig> getPatternIngredients(ItemStack pattern) {
		if (!(pattern.getItem() instanceof ItemCraftingPattern)) return new ArrayList<FilterConfig>(); // also this
		
		NBTTagCompound patternTags = pattern.getTagCompound();
		if (patternTags == null) return new ArrayList<FilterConfig>();
		NBTTagList ingredientTags = patternTags.getTagList("ingredients", NBT.TAG_COMPOUND);
		
		List<FilterConfig> ingredientList = new ArrayList<FilterConfig>();
		for (int i=0; i<ingredientTags.tagCount(); i++) {
			NBTTagCompound ingTag = ingredientTags.getCompoundTagAt(i);
			if (ingTag.hasKey("empty") && ingTag.getBoolean("empty")) {
				ingredientList.add(null);
			} else {
				ingredientList.add(new FilterConfig(ingTag));
			}
		}
		
		return ingredientList;
	}
}
