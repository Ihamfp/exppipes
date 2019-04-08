package ihamfp.exppipes.client;

import ihamfp.exppipes.client.render.TESRPipe;
import ihamfp.exppipes.common.CommonProxy;
import ihamfp.exppipes.tileentities.TileEntityPipe;
import ihamfp.exppipes.tileentities.TileEntityProviderPipe;
import ihamfp.exppipes.tileentities.TileEntityRequestPipe;
import ihamfp.exppipes.tileentities.TileEntityRoutingPipe;
import ihamfp.exppipes.tileentities.TileEntitySupplierPipe;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

	@Override
	public void preInit(FMLPreInitializationEvent event) {
		super.preInit(event);
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPipe.class, new TESRPipe());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRoutingPipe.class, new TESRPipe());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRequestPipe.class, new TESRPipe());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityProviderPipe.class, new TESRPipe());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySupplierPipe.class, new TESRPipe());
	}

	@Override
	public void init(FMLInitializationEvent event) {
		super.init(event);
	}

	@Override
	public void postInit(FMLPostInitializationEvent event) {
		super.postInit(event);
	}
	
	@Override
	public World getClientWorld() {
		return Minecraft.getMinecraft().world;
	}

}
