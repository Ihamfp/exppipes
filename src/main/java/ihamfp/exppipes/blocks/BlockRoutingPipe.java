package ihamfp.exppipes.blocks;

import ihamfp.exppipes.ExppipesMod;
import ihamfp.exppipes.tileentities.TileEntityRoutingPipe;
import ihamfp.exppipes.tileentities.pipeconfig.ConfigRoutingPipe;
import ihamfp.exppipes.tileentities.pipeconfig.FilterConfig;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

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
		return new BlockStateContainer(this, connUP, connDOWN, connEAST, connWEST, connNORTH, connSOUTH, hasNetwork);
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
		super.breakBlock(worldIn, pos, state);
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		//ExppipesMod.logger.info("Clicked on " + this.getLocalizedName() + " at " + pos.toString());
		if (worldIn.isRemote) {
			return true;
		} else if (!(worldIn.getTileEntity(pos) instanceof TileEntityRoutingPipe)) {
			return false;
		}
		
		if (!playerIn.isSneaking()) {
			playerIn.openGui(ExppipesMod.instance, 1, worldIn, pos.getX(), pos.getY(), pos.getZ());
			return true;
		}
		
		return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
	}
	
	@Override
	public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		super.getDrops(drops, world, pos, state, fortune);
		TileEntity tileEntity = world instanceof ChunkCache ? ((ChunkCache)world).getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK) : world.getTileEntity(pos);
		if (tileEntity instanceof TileEntityRoutingPipe && ((TileEntityRoutingPipe)tileEntity).sinkConfig != null) {
			ConfigRoutingPipe cfg = ((TileEntityRoutingPipe)tileEntity).sinkConfig;
			for (FilterConfig filter : cfg.filters) {
				drops.add(filter.stack);
			}
		}
	}
}
