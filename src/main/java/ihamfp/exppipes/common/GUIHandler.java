package ihamfp.exppipes.common;

import ihamfp.exppipes.common.network.PacketFilterChange.FilterFunction;
import ihamfp.exppipes.containers.ContainerCraftingPipe;
import ihamfp.exppipes.containers.ContainerPatternSettings;
import ihamfp.exppipes.containers.ContainerPipeConfig;
import ihamfp.exppipes.containers.ContainerPipeRequest;
import ihamfp.exppipes.containers.GuiContainerCraftingPipe;
import ihamfp.exppipes.containers.GuiContainerPatternSettings;
import ihamfp.exppipes.containers.GuiContainerPipeConfig;
import ihamfp.exppipes.containers.GuiContainerPipeRequest;
import ihamfp.exppipes.tileentities.TileEntityCraftingPipe;
import ihamfp.exppipes.tileentities.TileEntityExtractionPipe;
import ihamfp.exppipes.tileentities.TileEntityRequestPipe;
import ihamfp.exppipes.tileentities.TileEntityRoutingPipe;
import ihamfp.exppipes.tileentities.TileEntityStockKeeperPipe;
import ihamfp.exppipes.tileentities.TileEntitySupplierPipe;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GUIHandler implements IGuiHandler {
	
	/* Gui IDs:
	 *  0: nothing
	 *  1-999: pipe GUI
	 *  1000-x: item GUI
	 */

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (ID < 1000) {
			TileEntity te = world.getTileEntity(new BlockPos(x,y,z));
			
			switch (ID) {
			case 1: // sink config
				return new ContainerPipeConfig(player.inventory, (TileEntityRoutingPipe) te, ((TileEntityRoutingPipe) te).sinkConfig);
			case 2: // supply config
				return new ContainerPipeConfig(player.inventory, (TileEntityRoutingPipe) te, ((TileEntitySupplierPipe) te).supplyConfig);
			case 3: // request pipe
				return new ContainerPipeRequest(player.inventory, (TileEntityRequestPipe)te);
			case 4: // crafting pipe
				return new ContainerCraftingPipe(player.inventory, (TileEntityCraftingPipe)te);
			case 5: // extraction pipe
				return new ContainerPipeConfig(player.inventory, (TileEntityRoutingPipe)te, ((TileEntityExtractionPipe)te).extractConfig);
			case 6: // stock keeper pipe
				return new ContainerPipeConfig(player.inventory, (TileEntityRoutingPipe) te, ((TileEntityStockKeeperPipe)te).stockConfig);
			default:
				return null;
			}
		} else {
			switch (ID) {
			case 1001:
				return new ContainerPatternSettings(player.inventory);
			default:
				return null;
			}
		}
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (ID < 1000) {
			TileEntity te = world.getTileEntity(new BlockPos(x,y,z));
			
			switch (ID)	{
			case 1: // sink config
				return new GuiContainerPipeConfig(new ContainerPipeConfig(player.inventory, (TileEntityRoutingPipe) te, ((TileEntityRoutingPipe) te).sinkConfig), ((TileEntityRoutingPipe) te), FilterFunction.FILTER_SINK);
			case 2:
				return new GuiContainerPipeConfig(new ContainerPipeConfig(player.inventory, (TileEntityRoutingPipe) te, ((TileEntitySupplierPipe) te).supplyConfig), ((TileEntitySupplierPipe) te), FilterFunction.FILTER_SUPPLY);
			case 3:
				return new GuiContainerPipeRequest(new ContainerPipeRequest(player.inventory, (TileEntityRequestPipe)te), (TileEntityRequestPipe)te);
			case 4:
				return new GuiContainerCraftingPipe(new ContainerCraftingPipe(player.inventory, (TileEntityCraftingPipe)te));
			case 5:
				return new GuiContainerPipeConfig(new ContainerPipeConfig(player.inventory, (TileEntityRoutingPipe)te, ((TileEntityExtractionPipe)te).extractConfig), ((TileEntityExtractionPipe)te), FilterFunction.FILTER_EXTRACT);
			case 6:
				return new GuiContainerPipeConfig(new ContainerPipeConfig(player.inventory, (TileEntityRoutingPipe) te, ((TileEntityStockKeeperPipe)te).stockConfig), ((TileEntityStockKeeperPipe)te), FilterFunction.FILTER_STOCK);
			default:
				return null;	
			}
		} else {
			switch (ID) {
			case 1001:
				return new GuiContainerPatternSettings(new ContainerPatternSettings(player.inventory), x);
			default:
				return null;
			}
		}
	}

}
