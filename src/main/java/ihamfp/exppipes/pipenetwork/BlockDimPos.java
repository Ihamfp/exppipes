package ihamfp.exppipes.pipenetwork;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.DimensionManager;

public class BlockDimPos extends BlockPos {
	public int dimension = 0;
	
	public BlockDimPos(int x, int y, int z, int dim) {
		super(x, y, z);
		this.dimension = dim;
	}
	
	public BlockDimPos(BlockPos pos, int dim) {
		super(pos.getX(), pos.getY(), pos.getZ());
		this.dimension = dim;
	}
	
	public BlockDimPos(TileEntity te) {
		this(te.getPos(), (te.getWorld()!=null)?te.getWorld().provider.getDimension():0);
	}
	
	public boolean isHere(TileEntity te) {
		BlockPos tePos = te.getPos();
		int dim = te.getWorld().provider.getDimension();
		
		return (tePos.getX() == this.getX() && tePos.getY() == this.getY() && tePos.getZ() == this.getZ() && dim == this.dimension);
	}
	
	public boolean hasTE() {
		TileEntity te = DimensionManager.getWorld(this.dimension).getTileEntity(this);
		return (te != null && !te.isInvalid());
	}
	
	public TileEntity getTE() {
		return DimensionManager.getWorld(this.dimension).getTileEntity(this);
	}
}
