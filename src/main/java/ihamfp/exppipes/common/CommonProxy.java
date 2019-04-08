package ihamfp.exppipes.common;

import ihamfp.exppipes.ExppipesMod;
import ihamfp.exppipes.blocks.ModBlocks;
import ihamfp.exppipes.common.network.PacketHandler;
import ihamfp.exppipes.interfaces.IProxy;
import ihamfp.exppipes.items.ModItems;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

public class CommonProxy implements IProxy {

	@Override
	public void preInit(FMLPreInitializationEvent event) {
		ModBlocks.preInit();
		ModItems.preInit();
		PacketHandler.preInit();
	}

	@Override
	public void init(FMLInitializationEvent event) {
		NetworkRegistry.INSTANCE.registerGuiHandler(ExppipesMod.instance, new GUIHandler());
	}

	@Override
	public void postInit(FMLPostInitializationEvent event) {
		
	}
	
	public World getClientWorld() {
		return null; // server here
	}

}
