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
	public EnumFacing from; // where the stack is coming from
	public EnumFacing to;   // where the stack will be sent when time is up
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
		this(itemStack, from, null, dest, insertTime);
	}
	
	public ItemDirection(ItemStack itemStack, EnumFacing from, EnumFacing to, TileEntityRoutingPipe dest, long insertTime) {
		this.itemStack = itemStack;
		this.from = from;
		this.to = to; // will be set in pipe update
		this.destination = dest;
		this.insertTime = insertTime;
	}
}