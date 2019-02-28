package ihamfp.exppipes.common;

import ihamfp.exppipes.containers.ContainerPipeConfig;
import ihamfp.exppipes.containers.ContainerPipeRequest;
import ihamfp.exppipes.containers.GuiContainerPipeConfig;
import ihamfp.exppipes.containers.GuiContainerPipeRequest;
import ihamfp.exppipes.tileentities.TileEntityRequestPipe;
import ihamfp.exppipes.tileentities.TileEntityRoutingPipe;
import ihamfp.exppipes.tileentities.TileEntitySupplierPipe;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GUIHandler implements IGuiHandler {

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		TileEntity te = world.getTileEntity(new BlockPos(x,y,z));
		
		switch (ID) {
		case 1: // sink config
			return new ContainerPipeConfig(player.inventory, (TileEntityRoutingPipe) te, ((TileEntityRoutingPipe) te).sinkConfig);
		case 2: // supply config
			return new ContainerPipeConfig(player.inventory, (TileEntityRoutingPipe) te, ((TileEntitySupplierPipe) te).supplyConfig);
		case 3: // request pipe
			return new ContainerPipeRequest(player.inventory, (TileEntityRequestPipe)te);
		default:
			return null;
		}
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		TileEntity te = world.getTileEntity(new BlockPos(x,y,z));
		
		// btw, pipe configuration should be without wrench, but not on the main screen (if a main screen exists)
		
		switch (ID)	{
		case 1: // sink config
			return new GuiContainerPipeConfig(new ContainerPipeConfig(player.inventory, (TileEntityRoutingPipe) te, ((TileEntityRoutingPipe) te).sinkConfig), ((TileEntityRoutingPipe) te).sinkConfig);
		case 2:
			return new GuiContainerPipeConfig(new ContainerPipeConfig(player.inventory, (TileEntityRoutingPipe) te, ((TileEntitySupplierPipe) te).supplyConfig), ((TileEntitySupplierPipe) te).supplyConfig);
		case 3:
			return new GuiContainerPipeRequest(new ContainerPipeRequest(player.inventory, (TileEntityRequestPipe)te), (TileEntityRequestPipe)te);
		default:
			return null;	
		}
	}

}
