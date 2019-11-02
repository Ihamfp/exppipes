package ihamfp.exppipes.common.network;

import java.util.Map;

import ihamfp.exppipes.pipenetwork.BlockDimPos;
import ihamfp.exppipes.pipenetwork.PipeNetwork;
import ihamfp.exppipes.tileentities.TileEntityNetworkBlock;
import ihamfp.exppipes.tileentities.TileEntityRoutingPipe;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

// Received by server
public class PacketInventoryRequest implements IMessage {
	BlockDimPos pos;
	
	public PacketInventoryRequest() {}
	
	public PacketInventoryRequest(BlockDimPos pos) {
		this.pos = pos;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.pos = new BlockDimPos(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt());
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(this.pos.getX());
		buf.writeInt(this.pos.getY());
		buf.writeInt(this.pos.getZ());
		buf.writeInt(this.pos.dimension);
	}
	
	public static class Handler implements IMessageHandler<PacketInventoryRequest,IMessage> {

		@Override
		public IMessage onMessage(PacketInventoryRequest message, MessageContext ctx) {
			EntityPlayerMP serverPlayer = ctx.getServerHandler().player;
			if (message.pos == null) return null;
			serverPlayer.getServerWorld().addScheduledTask(() -> {
				if (serverPlayer.getServerWorld().provider.getDimension() != message.pos.dimension) return;
				if (!serverPlayer.getServerWorld().isBlockLoaded(message.pos)) return;
				TileEntity te = serverPlayer.getServerWorld().getTileEntity(message.pos);
				if (te == null) return;
				PipeNetwork network = null;
				if (te instanceof TileEntityRoutingPipe) {
					network = ((TileEntityRoutingPipe) te).network;
				} else if (te instanceof TileEntityNetworkBlock) {
					network = ((TileEntityNetworkBlock) te).searchNetwork();
				}
				if (network == null) return;
				
				Map<ItemStack,Integer> condInv = network.condensedInventory();
				int entries = condInv.size();
				for (ItemStack stack : condInv.keySet()) {
					PacketInventoryMap toSend = new PacketInventoryMap(stack, condInv.get(stack), message.pos, entries);
					PacketHandler.INSTANCE.sendTo(toSend, serverPlayer);
				}
			});
			return null;
		}
	}

}
