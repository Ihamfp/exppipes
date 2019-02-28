package ihamfp.exppipes.pipenetwork;

import ihamfp.exppipes.pipenetwork.PipeNetwork.Request;
import ihamfp.exppipes.tileentities.TileEntityRoutingPipe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

/***
 * Packs all the details for a traveling item
 */
public class ItemDirection {
	public ItemStack itemStack;
	public EnumFacing from;
	public long insertTime;
	
	/***
	 * Stores the final destination node
	 * If null, the stack will be routed to the default destination.
	 */
	public TileEntityRoutingPipe destination;
	
	/**
	 * The request this item is associated to. May be null for a freely-traveling item
	 */
	public Request request;
	
	public ItemDirection(ItemStack itemStack, EnumFacing from, TileEntityRoutingPipe dest, long insertTime) {
		this.itemStack = itemStack;
		this.from = from;
		this.destination = dest;
		this.insertTime = insertTime;
	}
}