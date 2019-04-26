package ihamfp.exppipes.common.network;

import ihamfp.exppipes.pipenetwork.BlockDimPos;
import ihamfp.exppipes.pipenetwork.Request;
import ihamfp.exppipes.tileentities.TileEntityRequestPipe;
import ihamfp.exppipes.tileentities.TileEntityRoutingPipe;
import ihamfp.exppipes.tileentities.pipeconfig.FilterConfig;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketItemRequest implements IMessage {
	BlockDimPos pos;
	FilterConfig filter;
	int count;
	
	public PacketItemRequest() {}
	
	public PacketItemRequest(BlockDimPos pos, FilterConfig filter, int count) {
		this.pos = pos;
		this.filter = filter;
		this.count = count;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.pos = new BlockDimPos(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt());
		ItemStack stack = ByteBufUtils.readItemStack(buf);
		this.filter = new FilterConfig(stack, buf.readInt(), buf.readBoolean());
		this.count = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(this.pos.getX());
		buf.writeInt(this.pos.getY());
		buf.writeInt(this.pos.getZ());
		buf.writeInt(this.pos.dimension);
		ByteBufUtils.writeItemStack(buf, this.filter.reference);
		buf.writeInt(filter.filterId);
		buf.writeBoolean(filter.blacklist);
		buf.writeInt(this.count);
	}
	
	public static class Handler implements IMessageHandler<PacketItemRequest,IMessage> {

		@Override
		public IMessage onMessage(PacketItemRequest message, MessageContext ctx) {
			EntityPlayerMP serverPlayer = ctx.getServerHandler().player;
			if (message.pos == null) return null;
			serverPlayer.getServerWorld().addScheduledTask(() -> {
				if (!serverPlayer.getServerWorld().isBlockLoaded(message.pos)) return;
				TileEntity te = serverPlayer.getServerWorld().getTileEntity(message.pos);
				if (te == null || !(te instanceof TileEntityRoutingPipe)) return; // I'll be kind and send back even if the TE isn't right
				TileEntityRoutingPipe terp = (TileEntityRoutingPipe)te; // just a cast, really
				if (terp.network == null) return;
				
				Request req = terp.network.request(message.pos, message.filter, message.count);
				if (terp instanceof TileEntityRequestPipe) {
					((TileEntityRequestPipe)terp).requests.add(req);
				}
			});
			return null;
		}
	}

}
