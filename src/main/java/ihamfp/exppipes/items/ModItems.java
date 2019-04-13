package ihamfp.exppipes.items;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ModItems {
	public static ItemCraftingPattern craftingPattern = new ItemCraftingPattern("craftingPattern");
	public static ItemPipeDebug pipeDebug = new ItemPipeDebug("pipeDebug");
	
	public static void preInit() {
		MinecraftForge.EVENT_BUS.register(new ModItems());
	}
	
	@SubscribeEvent
	public void registerItems(RegistryEvent.Register<Item> event) {
		craftingPattern.setTranslationKey(craftingPattern.getRegistryName().toString());
		pipeDebug.setTranslationKey(pipeDebug.getRegistryName().toString());
		event.getRegistry().register(craftingPattern);
		event.getRegistry().register(pipeDebug);
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void registerItemBlockModels(ModelRegistryEvent event) {
		ModelLoader.setCustomModelResourceLocation(craftingPattern, 0, new ModelResourceLocation(craftingPattern.getRegistryName(), "gui"));
	}
}
