package ihamfp.exppipes.integration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ihamfp.exppipes.Utils;
import ihamfp.exppipes.common.network.PacketCraftingPatternData;
import ihamfp.exppipes.common.network.PacketHandler;
import ihamfp.exppipes.containers.ContainerPatternSettings;
import ihamfp.exppipes.tileentities.pipeconfig.FilterConfig;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class PatternTransferHandler implements IRecipeTransferHandler<ContainerPatternSettings> {
	@Override
	public Class<ContainerPatternSettings> getContainerClass() {
		return ContainerPatternSettings.class;
	}

	@Override
	public IRecipeTransferError transferRecipe(ContainerPatternSettings container, IRecipeLayout recipeLayout, EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
		if (doTransfer) {
			Map<Integer, ? extends IGuiIngredient<ItemStack>> recipeItems = recipeLayout.getItemStacks().getGuiIngredients();
			List<ItemStack> results = new ArrayList<ItemStack>();
			List<FilterConfig> ingredients = new ArrayList<FilterConfig>();
			for (Integer i : recipeItems.keySet()) {
				IGuiIngredient<ItemStack> ingredient = recipeItems.get(i);
				if (ingredient == null) continue;
				if (ingredient.isInput()) {
					if (ingredient.getDisplayedIngredient() != null) {
						ingredients.add(new FilterConfig(ingredient.getDisplayedIngredient(), (Utils.commonOredict(ingredient.getAllIngredients())!=null)?3:0, false));
					} else {
						ingredients.add(null);
					}
				} else if (ingredient.getDisplayedIngredient() != null) {
					results.add(ingredient.getDisplayedIngredient());
				}
			}
			player.closeScreen();
			PacketHandler.INSTANCE.sendToServer(new PacketCraftingPatternData(results, ingredients));
		}
		return null;
	}
}
