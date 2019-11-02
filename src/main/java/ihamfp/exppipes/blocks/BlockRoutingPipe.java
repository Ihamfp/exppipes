package ihamfp.exppipes.blocks;

import ihamfp.exppipes.tileentities.TileEntityRoutingPipe;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.items.IItemHandler;

public class BlockRoutingPipe extends BlockPipe {
	static final PropertyBool hasNetwork = PropertyBool.create("hasnetwork");
	
	public BlockRoutingPipe(String registryID) {
		super(registryID);
		this.setDefaultState(getDefaultState().withProperty(hasNetwork, false));
	}
	
	@Override
	public TileEntity createTileEntity(World worldIn, IBlockState state) {
		return new TileEntityRoutingPipe();
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, connUP, connDOWN, connEAST, connWEST, connNORTH, connSOUTH, hasNetwork, opaque, pipeColor);
	}
	
	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		TileEntity tileEntity = worldIn instanceof ChunkCache ? ((ChunkCache)worldIn).getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK) : worldIn.getTileEntity(pos);
		boolean hasNet = false;
		if (tileEntity instanceof TileEntityRoutingPipe) {
			hasNet = (((TileEntityRoutingPipe)tileEntity).network != null);
		}
		return super.getActualState(state, worldIn, pos).withProperty(hasNetwork, hasNet);
	}

	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		TileEntity te = worldIn.getTileEntity(pos);
		if (te instanceof TileEntityRoutingPipe && ((TileEntityRoutingPipe)te).network != null) {
			((TileEntityRoutingPipe)te).network.removeNode((TileEntityRoutingPipe)te);
		}
		if (te instanceof TileEntityRoutingPipe) {
			if (((TileEntityRoutingPipe)te).upgradesItemHandler != null) {
				IItemHandler upgrades = ((TileEntityRoutingPipe)te).upgradesItemHandler;
				for (int i=0; i<upgrades.getSlots(); i++) {
					worldIn.spawnEntity(new EntityItem(worldIn, pos.getX(), pos.getY(), pos.getZ(), upgrades.getStackInSlot(i)));
				}
			}
		}
		super.breakBlock(worldIn, pos, state);
	}
	
	@Override
	public int getGuiID() {
		return 1;
	}
}
