package ihamfp.exppipes.common.network;

import java.util.ArrayList;
import java.util.List;

import ihamfp.exppipes.ExppipesMod;
import ihamfp.exppipes.Utils;
import ihamfp.exppipes.tileentities.InvCacheEntry;
import ihamfp.exppipes.tileentities.TileEntityRequestPipe;
import ihamfp.exppipes.tileentities.TileEntityRequestStation;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

// Actually doesn't send the whole map at once, but only one entry
public class PacketInventoryMap implements IMessage {
	ItemStack stack; // Stack of the entry
	int count; // available count,<=0 if craftable
	BlockPos pos; // where to send the entry
	int entries; // how many entries in total.
	
	public PacketInventoryMap() {}
	
	public PacketInventoryMap(ItemStack stack, int count, BlockPos pos, int entries) {
		this.stack = stack;
		this.count = count;
		this.pos = pos;
		this.entries = entries;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.stack = ByteBufUtils.readItemStack(buf);
		this.pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
		this.count = buf.readInt();
		this.entries = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeItemStack(buf, this.stack);
		buf.writeInt(this.pos.getX());
		buf.writeInt(this.pos.getY());
		buf.writeInt(this.pos.getZ());
		buf.writeInt(this.count);
		buf.writeInt(this.entries);
	}
	
	public static class Handler implements IMessageHandler<PacketInventoryMap,IMessage> {
		@Override
		public IMessage onMessage(PacketInventoryMap message, MessageContext ctx) {
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
				World clientWorld = ExppipesMod.proxy.getClientWorld();
				if (clientWorld == null) return;
				TileEntity tile = clientWorld.getTileEntity(message.pos);
				
				List<InvCacheEntry> invCacheBuffer = null;
				
				if (tile instanceof TileEntityRequestPipe) {
					invCacheBuffer = ((TileEntityRequestPipe)tile).invCacheBuffer;
					if (invCacheBuffer == null) {
						invCacheBuffer = new ArrayList<InvCacheEntry>();
						((TileEntityRequestPipe)tile).invCacheBuffer = invCacheBuffer;
					}
				} else if (tile instanceof TileEntityRequestStation) {
					invCacheBuffer = ((TileEntityRequestStation)tile).invCacheBuffer;
					if (invCacheBuffer == null) {
						invCacheBuffer = new ArrayList<InvCacheEntry>();
						((TileEntityRequestStation)tile).invCacheBuffer = invCacheBuffer;
					}
				}
				
				invCacheBuffer.add(new InvCacheEntry(message.stack, message.count<0?-message.count:message.count, message.count<=0));
				
				Utils.invCacheSort(invCacheBuffer);
				
				if (message.entries == invCacheBuffer.size()) { // buffer -> actual thing
					if (tile instanceof TileEntityRequestPipe) {
						if (((TileEntityRequestPipe)tile).invCache == null) ((TileEntityRequestPipe)tile).invCache = new ArrayList<InvCacheEntry>();
						((TileEntityRequestPipe)tile).invCache.clear();
						((TileEntityRequestPipe)tile).invCache.addAll(invCacheBuffer);
					} else if (tile instanceof TileEntityRequestStation) {
						if (((TileEntityRequestStation)tile).invCache == null) ((TileEntityRequestStation)tile).invCache = new ArrayList<InvCacheEntry>();
						((TileEntityRequestStation)tile).invCache.clear();
						((TileEntityRequestStation)tile).invCache.addAll(invCacheBuffer);
					}
					invCacheBuffer.clear();
				}
			});
			return null;
		}
	}
}