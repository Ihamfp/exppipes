package ihamfp.exppipes.common.network;

import ihamfp.exppipes.pipenetwork.BlockDimPos;
import ihamfp.exppipes.tileentities.TileEntityExtractionPipe;
import ihamfp.exppipes.tileentities.TileEntityRoutingPipe;
import ihamfp.exppipes.tileentities.TileEntityStockKeeperPipe;
import ihamfp.exppipes.tileentities.TileEntitySupplierPipe;
import ihamfp.exppipes.tileentities.pipeconfig.FilterConfig;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketFilterChange implements IMessage {
	public enum FilterFunction {
		FILTER_SINK,
		FILTER_SUPPLY,
		FILTER_EXTRACT,
		FILTER_STOCK;
	}
	
	BlockDimPos pos;
	int filterId;
	int newFilter;
	boolean blacklist;
	int priority;
	FilterFunction filterFunction;
	ItemStack reference;
	
	public PacketFilterChange() {}
	
	public PacketFilterChange(BlockDimPos pos, int filterId, int newFilter, boolean blacklist, int priority, ItemStack reference, FilterFunction filterFunction) {
		this.pos = pos;
		this.filterId = filterId;
		this.newFilter = newFilter;
		this.blacklist = blacklist;
		this.priority = priority;
		this.filterFunction = filterFunction;
		this.reference = reference;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.pos = new BlockDimPos(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt());
		this.filterId = buf.readInt();
		this.newFilter = buf.readInt();
		this.blacklist = buf.readBoolean();
		this.priority = buf.readInt();
		this.filterFunction = FilterFunction.values()[buf.readByte()];
		this.reference = ByteBufUtils.readItemStack(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(this.pos.getX());
		buf.writeInt(this.pos.getY());
		buf.writeInt(this.pos.getZ());
		buf.writeInt(this.pos.dimension);
		buf.writeInt(this.filterId);
		buf.writeInt(this.newFilter);
		buf.writeBoolean(blacklist);
		buf.writeInt(this.priority);
		buf.writeByte(this.filterFunction.ordinal());
		ByteBufUtils.writeItemStack(buf, this.reference);
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
					if (message.reference.isEmpty() && message.filterId < terp.sinkConfig.filters.size()) {
						terp.sinkConfig.filters.remove(message.filterId);
					} else if (terp.sinkConfig.filters.size() <= message.filterId) {
						filter = new FilterConfig(new ItemStack(Blocks.BEDROCK, 1), 0, false);
						terp.sinkConfig.filters.add(filter);
					} else {
						filter = terp.sinkConfig.filters.get(message.filterId);
					}
					break;
				case FILTER_SUPPLY:
					if (!(terp instanceof TileEntitySupplierPipe)) return;
					if (message.reference.isEmpty() && message.filterId < ((TileEntitySupplierPipe)terp).supplyConfig.filters.size()) {
						((TileEntitySupplierPipe)terp).supplyConfig.filters.remove(message.filterId);
					} else if (((TileEntitySupplierPipe)terp).supplyConfig.filters.size() <= message.filterId) {
						filter = new FilterConfig(new ItemStack(Blocks.BEDROCK, 1), 0, false);
						((TileEntitySupplierPipe)terp).supplyConfig.filters.add(filter);
					} else {
						filter = ((TileEntitySupplierPipe)terp).supplyConfig.filters.get(message.filterId);
					}
					break;
				case FILTER_EXTRACT:
					if (!(terp instanceof TileEntityExtractionPipe)) return;
					if (message.reference.isEmpty() && message.filterId < ((TileEntityExtractionPipe)terp).extractConfig.filters.size()) {
						((TileEntityExtractionPipe)terp).extractConfig.filters.remove(message.filterId);
					} else if (((TileEntityExtractionPipe)terp).extractConfig.filters.size() <= message.filterId) {
						filter = new FilterConfig(new ItemStack(Blocks.BEDROCK, 1), 0, false);
						((TileEntityExtractionPipe)terp).extractConfig.filters.add(filter);
					} else {
						filter = ((TileEntityExtractionPipe)terp).extractConfig.filters.get(message.filterId);
					}
					break;
				case FILTER_STOCK:
					if (!(terp instanceof TileEntityStockKeeperPipe)) return;
					if (message.reference.isEmpty() && message.filterId < ((TileEntityStockKeeperPipe)terp).stockConfig.filters.size()) {
						((TileEntityStockKeeperPipe)terp).stockConfig.filters.remove(message.filterId);
					} else if (((TileEntityStockKeeperPipe)terp).stockConfig.filters.size() <= message.filterId) {
						filter = new FilterConfig(new ItemStack(Blocks.BEDROCK, 1), 0, false);
						((TileEntityStockKeeperPipe)terp).stockConfig.filters.add(filter);
					} else {
						filter = ((TileEntityStockKeeperPipe)terp).stockConfig.filters.get(message.filterId);
					}
					break;
				default:
					return;
				}
				
				if (filter != null) { // Yes, I know it shouldn't be null at this point.
					filter.reference = message.reference;
					filter.filterId = message.newFilter;
					filter.blacklist = message.blacklist;
					filter.priority = message.priority;
				}
			});
			return null;
		}
	}

}
