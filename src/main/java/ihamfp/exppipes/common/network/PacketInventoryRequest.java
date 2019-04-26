package ihamfp.exppipes.common.network;

import java.util.Map;

import ihamfp.exppipes.pipenetwork.BlockDimPos;
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
				if (!serverPlayer.getServerWorld().isBlockLoaded(message.pos)) return;
				TileEntity te = serverPlayer.getServerWorld().getTileEntity(message.pos);
				if (te == null || !(te instanceof TileEntityRoutingPipe)) return; // I'll be kind and send back even if the TE isn't right
				TileEntityRoutingPipe terp = (TileEntityRoutingPipe)te; // just a cast, really
				if (terp.network == null) return;
				
				Map<ItemStack,Integer> condInv = terp.network.condensedInventory();
				for (ItemStack stack : condInv.keySet()) {
					PacketInventoryMap toSend = new PacketInventoryMap(stack, condInv.get(stack), message.pos);
					PacketHandler.INSTANCE.sendTo(toSend, serverPlayer);
				}
			});
			return null;
		}
	}

}
