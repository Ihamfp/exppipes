package ihamfp.exppipes.common.network;

import ihamfp.exppipes.tileentities.TileEntityRequestPipe;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

// Actually doesn't send the whole map at once, but only one entry
public class PacketInventoryMap implements IMessage {
	ItemStack stack;
	int count;
	BlockPos pos;
	
	public PacketInventoryMap() {}
	
	public PacketInventoryMap(ItemStack stack, int count, BlockPos pos) {
		this.stack = stack;
		this.count = count;
		this.pos = pos;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.stack = ByteBufUtils.readItemStack(buf);
		this.pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
		this.count = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeItemStack(buf, this.stack);
		buf.writeInt(this.pos.getX());
		buf.writeInt(this.pos.getY());
		buf.writeInt(this.pos.getZ());
		buf.writeInt(count);
	}
	
	public static class Handler implements IMessageHandler<PacketInventoryMap,IMessage> {
		@Override
		public IMessage onMessage(PacketInventoryMap message, MessageContext ctx) {
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
				EntityPlayer clientPlayer = Minecraft.getMinecraft().player;
				if (clientPlayer == null || clientPlayer.world == null) return;
				TileEntity tile = clientPlayer.world.getTileEntity(message.pos);
				if (!(tile instanceof TileEntityRequestPipe)) return;
				TileEntityRequestPipe terp = (TileEntityRequestPipe)tile;
				
				terp.invCache.put(message.stack, message.count);
			});
			return null;
		}
	}
}