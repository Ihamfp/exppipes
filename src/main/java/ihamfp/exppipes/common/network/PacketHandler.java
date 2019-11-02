package ihamfp.exppipes.common.network;

import ihamfp.exppipes.ExppipesMod;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler {
	public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(ExppipesMod.MODID);
	
	public static void preInit() {
		int id = 0;
		INSTANCE.registerMessage(PacketInventoryRequest.Handler.class, PacketInventoryRequest.class, id++, Side.SERVER);
		INSTANCE.registerMessage(PacketInventoryMap.Handler.class, PacketInventoryMap.class, id++, Side.CLIENT);
		INSTANCE.registerMessage(PacketItemRequest.Handler.class, PacketItemRequest.class, id++, Side.SERVER);
		INSTANCE.registerMessage(PacketFilterChange.Handler.class, PacketFilterChange.class, id++, Side.SERVER);
		INSTANCE.registerMessage(PacketSetDefaultRoute.Handler.class, PacketSetDefaultRoute.class, id++, Side.SERVER);
		INSTANCE.registerMessage(PacketCraftingPatternData.Handler.class, PacketCraftingPatternData.class, id++, Side.SERVER);
		INSTANCE.registerMessage(PacketSideDiscon.Handler.class, PacketSideDiscon.class, id++, Side.CLIENT);
	}
}
