package ihamfp.exppipes.common.network;

import java.util.ArrayList;
import java.util.List;

import ihamfp.exppipes.items.ItemCraftingPattern;
import ihamfp.exppipes.tileentities.pipeconfig.FilterConfig;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketCraftingPatternData implements IMessage {
	List<ItemStack> results;
	List<FilterConfig> ingredients;
	
	public PacketCraftingPatternData() {}
	
	public PacketCraftingPatternData(List<ItemStack> results, List<FilterConfig> ingredients) {
		this.results = results;
		this.ingredients = ingredients;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		int count = buf.readByte();
		this.results = new ArrayList<ItemStack>();
		for (int i=0; i<count; i++) {
			this.results.add(ByteBufUtils.readItemStack(buf));
		}
		
		count = buf.readByte();
		this.ingredients = new ArrayList<FilterConfig>();
		for (int i=0; i<count; i++) {
			int filterId = buf.readInt();
			if (filterId == -1) {
				this.ingredients.add(null);
				continue;
			}
			int priority = buf.readInt();
			boolean blacklist = buf.readBoolean();
			ItemStack reference = ByteBufUtils.readItemStack(buf);
			this.ingredients.add(new FilterConfig(reference, filterId, blacklist, priority));
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		if (this.results == null) {
			buf.writeByte(0);
		} else {
			buf.writeByte(this.results.size());
			for (int i=0; i<this.results.size(); i++) {
				ByteBufUtils.writeItemStack(buf, this.results.get(i));
			}
		}
		
		if (this.ingredients == null) {
			buf.writeByte(0);
		} else {
			buf.writeByte(this.ingredients.size());
			for (int i=0; i<this.ingredients.size(); i++) {
				FilterConfig ingredient = ingredients.get(i);
				if (ingredient == null) {
					buf.writeInt(-1);
					continue;
				}
				buf.writeInt(ingredient.filterId);
				buf.writeInt(ingredient.priority);
				buf.writeBoolean(ingredient.blacklist);
				ByteBufUtils.writeItemStack(buf, ingredient.reference);
			}
		}
	}
	
	public static class Handler implements IMessageHandler<PacketCraftingPatternData,IMessage> {

		@Override
		public IMessage onMessage(PacketCraftingPatternData message, MessageContext ctx) {
			EntityPlayerMP serverPlayer = ctx.getServerHandler().player;
			
			serverPlayer.getServerWorld().addScheduledTask(() -> {
				ItemStack currentStack = serverPlayer.inventory.getCurrentItem();
				ItemCraftingPattern.setPatternResults(currentStack, message.results);
				ItemCraftingPattern.setPatternIngredients(currentStack, message.ingredients);
			});
			return null;
		}
		
	}

}
