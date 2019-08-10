package ihamfp.exppipes.items;

import java.util.ArrayList;

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
	public static ItemUpgrade pipeUpgrade = new ItemUpgrade("pipeUpgrade");
	public static ItemUpgrade superPipeUpgrade = new ItemUpgrade("superPipeUpgrade");
	public static ItemUpgrade extractionSpeedUpgrade = new ItemUpgrade("extractionSpeedUpgrade");
	public static ItemUpgrade itemLockUpgrade = new ItemUpgrade("itemLockUpgrade");
	public static ItemLocator itemLocator = new ItemLocator("itemLocator");
	public static ItemAutoLocator itemAutoLocator = new ItemAutoLocator("itemAutoLocator");
	
	public static ArrayList<Item> modItems = new ArrayList<Item>();
	
	public static void preInit() {
		modItems.add(craftingPattern);
		modItems.add(pipeDebug);
		modItems.add(pipeUpgrade);
		modItems.add(superPipeUpgrade);
		modItems.add(extractionSpeedUpgrade);
		modItems.add(itemLockUpgrade);
		modItems.add(itemLocator);
		modItems.add(itemAutoLocator);
		
		MinecraftForge.EVENT_BUS.register(new ModItems());
	}
	
	@SubscribeEvent
	public void registerItems(RegistryEvent.Register<Item> event) {
		for (Item item : modItems) {
			item.setTranslationKey(item.getRegistryName().toString());
			event.getRegistry().register(item);
		}
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void registerItemBlockModels(ModelRegistryEvent event) {
		for (Item item : modItems) {
			ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "gui"));
		}
	}
}
