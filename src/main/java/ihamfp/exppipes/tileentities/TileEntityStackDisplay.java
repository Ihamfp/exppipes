package ihamfp.exppipes.tileentities;

import java.util.Map;

import ihamfp.exppipes.Utils;
import ihamfp.exppipes.pipenetwork.PipeNetwork;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")
public class TileEntityStackDisplay extends TileEntityNetworkBlock implements ITickable, SimpleComponent {
	public ItemStack displayedStack = ItemStack.EMPTY;
	public String displayedText = "????";
	public String oldDisplay = "";
	public boolean computerControlled = false;
	
	long updateTimer = 0;
	static int updateTime = 20;
	
	@Override
	public void update() {
		if (this.world.isRemote) return;
		if (!this.computerControlled && this.world.getTotalWorldTime() >= this.updateTimer+updateTime) {
			this.updateTimer = this.world.getTotalWorldTime();
			PipeNetwork network = this.searchNetwork();
			if (network == null && !displayedStack.isEmpty()) {
				this.displayedText = "no network";
				return;
			} else if (network == null) {
				return;
			}
			Map<ItemStack,Integer> invMap = network.condensedInventory();
			boolean found = false;
			for (ItemStack s : invMap.keySet()) {
				if (ItemStack.areItemsEqual(s, this.displayedStack) && ItemStack.areItemStackTagsEqual(s, this.displayedStack)) {
					this.displayedText = Utils.formatNumber(invMap.get(s));
					found = true;
					break;
				}
			}
			if (!found) this.displayedText = "0";
		}
		if (!this.oldDisplay.equals(this.displayedText + ";" + this.displayedStack.toString())) {
			this.oldDisplay = this.displayedText + ";" + this.displayedStack.toString();
			IBlockState currentState = this.world.getBlockState(this.pos);
			this.world.notifyBlockUpdate(this.pos, currentState, currentState, 2);
		}
	}
	
	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
		if (oldState.getBlock() != newState.getBlock()) return true;
		return super.shouldRefresh(world, pos, oldState, newState);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		this.displayedText = compound.getString("dispText");
		this.displayedStack = new ItemStack(compound.getCompoundTag("dispStack"));
		this.computerControlled = compound.getBoolean("computer");
		super.readFromNBT(compound);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setString("dispText", this.displayedText);
		compound.setTag("dispStack", this.displayedStack.serializeNBT());
		compound.setBoolean("computer", this.computerControlled);
		return super.writeToNBT(compound);
	}
	
	@Override
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound tag = super.getUpdateTag();
		if (!this.world.isRemote) {
			tag = this.writeToNBT(tag);
		}
		return tag;
	}
	
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(this.getPos(), 1, this.getUpdateTag());
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		NBTTagCompound tag = pkt.getNbtCompound();
		if (this.world.isRemote) {
			this.readFromNBT(tag);
			while (this.displayedText.length() < 4) {
				this.displayedText = " " + this.displayedText;
			}
		}
		super.onDataPacket(net, pkt);
	}
	
	///////////////

	@Optional.Method(modid = "opencomputers")
	@Override
	public String getComponentName() {
		return "stackDisplay";
	}
	
	@Optional.Method(modid = "opencomputers")
    @Callback(doc = "")
	public Object[] setText(Context context, Arguments args) throws Exception {
		this.displayedText = args.checkString(0);
		this.computerControlled = true;
		return null;
	}
	
	@Optional.Method(modid = "opencomputers")
    @Callback(doc = "function(string:id [, int:meta=0 [, string:tags=\"\"]]): set the displayed stack")
	public Object[] setStack(Context context, Arguments args) throws Exception {
		this.displayedStack = GameRegistry.makeItemStack(args.checkString(0), args.optInteger(1, 0), 1, args.optString(2, ""));
		return null;
	}
}
