package ihamfp.exppipes.common.network;

import ihamfp.exppipes.tileentities.TileEntityRoutingPipe;
import ihamfp.exppipes.tileentities.TileEntitySupplierPipe;
import ihamfp.exppipes.tileentities.pipeconfig.FilterConfig;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketFilterChange implements IMessage {
	public enum FilterFunction {
		FILTER_SINK,
		FILTER_SUPPLY;
	}
	
	BlockPos pos;
	int filterId;
	int newFilter;
	boolean blacklist;
	int priority;
	FilterFunction filterFunction;
	
	public PacketFilterChange() {}
	
	public PacketFilterChange(BlockPos pos, int filterId, int newFilter, boolean blacklist, int priority, FilterFunction filterFunction) {
		this.pos = pos;
		this.filterId = filterId;
		this.newFilter = newFilter;
		this.blacklist = blacklist;
		this.priority = priority;
		this.filterFunction = filterFunction;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
		this.filterId = buf.readInt();
		this.newFilter = buf.readInt();
		this.blacklist = buf.readBoolean();
		this.priority = buf.readInt();
		this.filterFunction = FilterFunction.values()[buf.readByte()];
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(this.pos.getX());
		buf.writeInt(this.pos.getY());
		buf.writeInt(this.pos.getZ());
		buf.writeInt(this.filterId);
		buf.writeInt(this.newFilter);
		buf.writeBoolean(blacklist);
		buf.writeInt(this.priority);
		buf.writeByte(this.filterFunction.ordinal());
	}
	
	public static class Handler implements IMessageHandler<PacketFilterChange,IMessage> {

		@Override
		public IMessage onMessage(PacketFilterChange message, MessageContext ctx) {
			EntityPlayerMP serverPlayer = ctx.getServerHandler().player;
			if (message.pos == null) return null;
			serverPlayer.getServerWorld().addScheduledTask(() -> {
				if (!serverPlayer.getServerWorld().isBlockLoaded(message.pos)) return;
				TileEntity te = serverPlayer.getServerWorld().getTileEntity(message.pos);
				if (te == null || !(te instanceof TileEntityRoutingPipe)) return; // I'll be kind and send back even if the TE isn't right
				TileEntityRoutingPipe terp = (TileEntityRoutingPipe)te; // just a cast, really
				
				FilterConfig filter = null;
				switch (message.filterFunction) {
				case FILTER_SINK:
					if (terp.sinkConfig.filters.size() < message.filterId) break;
					filter = terp.sinkConfig.filters.get(message.filterId);
					break;
				case FILTER_SUPPLY:
					if (!(terp instanceof TileEntitySupplierPipe)) return;
					if (((TileEntitySupplierPipe)terp).supplyConfig.filters.size() < message.filterId) return;
					filter = ((TileEntitySupplierPipe)terp).supplyConfig.filters.get(message.filterId);
					break;
				default:
					return;
				}
				
				if (filter != null) { // Yes, I know it shouldn't be null at this point.
					filter.filterId = message.newFilter;
					filter.blacklist = message.blacklist;
					filter.priority = message.priority;
				}
			});
			return null;
		}
	}

}
