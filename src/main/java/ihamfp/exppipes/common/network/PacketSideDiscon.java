package ihamfp.exppipes.common.network;

import java.util.Map;

import ihamfp.exppipes.pipenetwork.BlockDimPos;
import ihamfp.exppipes.pipenetwork.PipeNetwork;
import ihamfp.exppipes.tileentities.TileEntityNetworkBlock;
import ihamfp.exppipes.tileentities.TileEntityPipe;
import ihamfp.exppipes.tileentities.TileEntityRoutingPipe;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketSideDiscon implements IMessage {
	BlockDimPos pos;
	EnumFacing face;
	boolean disconn;
	
	public PacketSideDiscon() {}
	
	public PacketSideDiscon(BlockDimPos pos, EnumFacing face, boolean disconn) {
		this.pos = pos;
		this.face = face;
		this.disconn = disconn;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.pos = new BlockDimPos(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt());
		this.face = EnumFacing.byIndex(buf.readByte());
		this.disconn = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(this.pos.getX());
		buf.writeInt(this.pos.getY());
		buf.writeInt(this.pos.getZ());
		buf.writeInt(this.pos.dimension);
		buf.writeByte(this.face.getIndex());
		buf.writeBoolean(this.disconn);
	}
	
	public static class Handler implements IMessageHandler<PacketSideDiscon,IMessage> {

		@Override
		public IMessage onMessage(PacketSideDiscon message, MessageContext ctx) {
			if (message.pos == null) return null;
			Minecraft.getMinecraft().addScheduledTask(() -> {
				World world = Minecraft.getMinecraft().world;
				if (!world.isBlockLoaded(message.pos)) return null;
				TileEntity te = world.getTileEntity(message.pos);
				if (!(te instanceof TileEntityPipe)) return null;
				
				Map<EnumFacing,Boolean> dic = ((TileEntityPipe)te).disableConnection;
				if (dic.getOrDefault(message.face, false) != message.disconn) {
					if (message.disconn) dic.put(message.face, true);
					else dic.remove(message.face);
				}
				return null;
			});
			return null;
		}
	}
}
