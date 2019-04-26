package ihamfp.exppipes.pipenetwork;

import java.util.concurrent.atomic.AtomicInteger;

import ihamfp.exppipes.tileentities.pipeconfig.FilterConfig;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

public class Request implements INBTSerializable<NBTTagCompound> {
	/**
	 * Used in crafting trees, can (and will, most likely) be null
	 */
	public Request parent = null;
	/**
	 * The position of the {@link TileEntityRoutingPipe} that requested the itemstack.
	 * If null, set the {@link ItemDirection.destination} to null so that the item is sent to the default route
	 */
	public BlockDimPos requester;
	
	/**
	 * The requested ItemStack filter
	 */
	public FilterConfig filter;
	/**
	 * Constant, the total requested item count
	 */
	public int requestedCount;
	/**
	 * The currently processing item count
	 * Increased when a crafting pipe starts crafting or when a provider inserts items on the network.
	 * Decreased when requested items are received by the requester.
	 */
	public AtomicInteger processingCount = new AtomicInteger(0);
	/**
	 * The processed count, increased by the requester on items reception.
	 */
	public int processedCount = 0;
	
	public Request(BlockDimPos requester, FilterConfig filter, int requestedCount) {
		this.requester = requester;
		this.filter = filter;
		this.requestedCount = requestedCount;
	}
	
	public Request(BlockDimPos requester, FilterConfig filter, int requestedCount, Request parent) {
		this(requester, filter, requestedCount);
		this.parent = parent;
	}
	
	public Request(NBTTagCompound nbt) {
		this.filter = new FilterConfig(nbt.getCompoundTag("filter"));
		this.requestedCount = nbt.getInteger("requested");
		this.processingCount.set(nbt.getInteger("processing"));
		this.processedCount = nbt.getInteger("processed");
		int[] pos = nbt.getIntArray("requester");
		if (pos.length == 4) this.requester = new BlockDimPos(pos[0], pos[1], pos[2], pos[3]);
	}

	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setTag("filter", this.filter.serializeNBT());
		tag.setInteger("requested", this.requestedCount);
		tag.setInteger("processing", this.processingCount.get());
		tag.setInteger("processed", this.processedCount);
		tag.setIntArray("requester", new int[] {requester.getX(), requester.getY(), requester.getZ(), requester.dimension});
		return tag;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		this.filter = new FilterConfig(nbt.getCompoundTag("filter"));
		this.requestedCount = nbt.getInteger("requested");
		this.processingCount.set(nbt.getInteger("processing"));
		this.processedCount = nbt.getInteger("processed");
		int[] pos = nbt.getIntArray("requester");
		if (pos.length == 4) this.requester = new BlockDimPos(pos[0], pos[1], pos[2], pos[3]);
	}
}