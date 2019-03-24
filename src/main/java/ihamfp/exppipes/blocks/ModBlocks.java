package ihamfp.exppipes.blocks;

import java.util.ArrayList;

import ihamfp.exppipes.tileentities.ModTileEntities;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ModBlocks {
	public static BlockPipe pipe = new BlockPipe("blockPipe");
	public static BlockRoutingPipe routingPipe = new BlockRoutingPipe("blockRoutingPipe");
	public static BlockProviderPipe providerPipe = new BlockProviderPipe("blockProviderPipe");
	public static BlockSupplierPipe supplierPipe = new BlockSupplierPipe("blockSupplierPipe");
	public static BlockRequestPipe requestPipe = new BlockRequestPipe("blockRequestPipe");
	public static BlockNoInsertionPipe noInsertionPipe = new BlockNoInsertionPipe("blockNoInsertionPipe");
	
	public static ArrayList<Block> modBlocks = new ArrayList<Block>();
	public static ArrayList<Item> modItemBlocks = new ArrayList<Item>();
	
	public static void preInit() {
		modBlocks.add(pipe);
		modBlocks.add(routingPipe);
		modBlocks.add(providerPipe);
		modBlocks.add(supplierPipe);
		modBlocks.add(requestPipe);
		modBlocks.add(noInsertionPipe);
		
		MinecraftForge.EVENT_BUS.register(new ModBlocks());
	}
	
	@SubscribeEvent
	public void registerBlocks(RegistryEvent.Register<Block> event) {
		for (Block b : modBlocks) {
			b.setTranslationKey(b.getRegistryName().toString());
			event.getRegistry().register(b);
		}
		ModTileEntities.registerTileEntities();
	}
	
	@SubscribeEvent
	public void registerItemBlocks(RegistryEvent.Register<Item> event) {
		for (Block b : modBlocks) {
			Item ib = new ItemBlock(b);
			ib.setRegistryName(b.getRegistryName());
			modItemBlocks.add(ib);
			event.getRegistry().register(ib);
		}
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void registerItemBlockModels(ModelRegistryEvent event) {
		for (Block b : modBlocks) {
			ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(b), 0, new ModelResourceLocation(b.getRegistryName(), "gui"));
		}
	}
}
