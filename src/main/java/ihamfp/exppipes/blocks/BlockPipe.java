package ihamfp.exppipes.blocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ihamfp.exppipes.ExppipesMod;
import ihamfp.exppipes.ModCreativeTabs;
import ihamfp.exppipes.pipenetwork.ItemDirection;
import ihamfp.exppipes.pipenetwork.PipeNetwork;
import ihamfp.exppipes.tileentities.TileEntityPipe;
import ihamfp.exppipes.tileentities.TileEntityRoutingPipe;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;

public class BlockPipe extends Block {
	static final PropertyBool connUP    = PropertyBool.create("connup");
	static final PropertyBool connDOWN  = PropertyBool.create("conndown");
	static final PropertyBool connEAST  = PropertyBool.create("conneast");
	static final PropertyBool connWEST  = PropertyBool.create("connwest");
	static final PropertyBool connNORTH = PropertyBool.create("connnorth");
	static final PropertyBool connSOUTH = PropertyBool.create("connsouth");
	
	@SuppressWarnings("serial")
	public static final Map<EnumFacing, PropertyBool> propertyMap = new HashMap<EnumFacing,PropertyBool>() {{
			put(EnumFacing.UP, connUP);
			put(EnumFacing.DOWN, connDOWN);
			put(EnumFacing.EAST, connEAST);
			put(EnumFacing.WEST, connWEST);
			put(EnumFacing.NORTH, connNORTH);
			put(EnumFacing.SOUTH, connSOUTH);
	}};
	
	public static final AxisAlignedBB bbCENTER = new AxisAlignedBB(5/16D, 5/16D,  5/16D, 11/16D, 11/16D, 11/16D);
	static final AxisAlignedBB bbUP     = new AxisAlignedBB(5/16D, 11/16D, 5/16D, 11/16D, 16/16D, 11/16D);
	static final AxisAlignedBB bbDOWN   = new AxisAlignedBB(5/16D, 0,      5/16D, 11/16D, 5/16D, 11/16D);
	
	// Just in case directions change
	// (I am REALLY lazy and I don't want to learn directions)
	@SuppressWarnings("serial")
	public static final Map<EnumFacing,AxisAlignedBB> bbMap = new HashMap<EnumFacing,AxisAlignedBB>() {{
		put(EnumFacing.UP, bbUP);
		put(EnumFacing.DOWN, bbDOWN);
		for (EnumFacing f : EnumFacing.HORIZONTALS) {
			if (f.getXOffset() == +1) {
				put(f, new AxisAlignedBB(11/16D, 5/16D, 5/16D,  16/16D, 11/16D, 11/16D));
			} else if (f.getXOffset() == -1) {
				put(f, new AxisAlignedBB(0/16D,  5/16D, 5/16D,  5/16D,  11/16D, 11/16D));
			} else if (f.getZOffset() == +1) {
				put(f, new AxisAlignedBB(5/16D,  5/16D, 11/16D, 11/16D, 11/16D, 16/16D));
			} else if (f.getZOffset() == -1) {
				put(f, new AxisAlignedBB(5/16D,  5/16D, 0/16D,  11/16D, 11/16D, 5/16D ));
			}
		}
	}};
	
	public BlockPipe(String registryID) {
		super(Material.BARRIER);
		this.setRegistryName(ExppipesMod.MODID, registryID);
		this.setCreativeTab(ModCreativeTabs.PIPES);
		this.setLightOpacity(0);
		this.setHardness(0.5f);
		this.setDefaultState(this.getDefaultState()
				.withProperty(connUP,    false)
				.withProperty(connDOWN,  false)
				.withProperty(connEAST,  false)
				.withProperty(connWEST,  false)
				.withProperty(connNORTH, false)
				.withProperty(connSOUTH, false));
	}
	
	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileEntityPipe();
	}

	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		if (!worldIn.isRemote) {
			TileEntity te = worldIn.getTileEntity(pos);
			if (te == null || te.isInvalid() || !(te instanceof TileEntityPipe) || state.getBlock() != this) { // TE wasn't valid or something.
				super.breakBlock(worldIn, pos, state);
				return;
			}
			// spawn items
			if (te instanceof TileEntityPipe) {
				((TileEntityPipe)te).itemHandler.tick(worldIn.getTotalWorldTime());
				for (ItemDirection itemDir : ((TileEntityPipe)te).itemHandler.storedItems) {
					worldIn.spawnEntity(new EntityItem(worldIn, pos.getX(), pos.getY(), pos.getZ(), itemDir.itemStack));
				}
			}
			
			// search for routing nodes, rebuild/split corresponding networks
			List<BlockPos> foundPipes = new ArrayList<BlockPos>();
			foundPipes.add(te.getPos());
			List<BlockPos> newPipes = new ArrayList<BlockPos>(); // still can't edit a list while iterating...
			List<TileEntityRoutingPipe> foundNodes = new ArrayList<TileEntityRoutingPipe>();
			do {
				newPipes.clear();
				for (BlockPos p : foundPipes) {
					for (EnumFacing f : EnumFacing.VALUES) {
						TileEntity teScan = worldIn.getTileEntity(p.offset(f));
						if (teScan instanceof TileEntityRoutingPipe) {
							foundNodes.add((TileEntityRoutingPipe)teScan);
						} else if (teScan instanceof TileEntityPipe && !foundPipes.contains(teScan.getPos()) && !newPipes.contains(teScan.getPos())) {
							newPipes.add(teScan.getPos());
						}
					}
				}
				foundPipes.addAll(newPipes);
			} while (newPipes.size() > 0);
			PipeNetwork.split(foundNodes);
		}
		super.breakBlock(worldIn, pos, state);
	}
	
	

	@Override
	public boolean canHarvestBlock(IBlockAccess world, BlockPos pos, EntityPlayer player) {
		return true;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, connUP, connDOWN, connEAST, connWEST, connNORTH, connSOUTH);
	}
	
	private boolean canConnectTo(BlockPos pos, EnumFacing onFace, IBlockAccess worldIn) {
		TileEntity teAtPos = worldIn.getTileEntity(pos);
		if (teAtPos == null) return false;
		if (teAtPos.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, onFace)) return true;
		return false;
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		for (EnumFacing e : EnumFacing.VALUES) {
			state = state.withProperty(propertyMap.get(e), canConnectTo(pos.offset(e), e.getOpposite(), worldIn));
		}
		return state;
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState();
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return 0;
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		state = state.getActualState(source, pos);
		AxisAlignedBB stateBB = bbCENTER;
		for (EnumFacing f : EnumFacing.VALUES) {
			if (state.getValue(propertyMap.get(f)).booleanValue()) {
				stateBB = stateBB.union(bbMap.get(f));
			}
		}
		return stateBB;
	}

	/*@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
		return this.getBoundingBox(blockState, worldIn, pos);
	}*/

	@SuppressWarnings({ "deprecation" })
	@Override
	public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entityIn, boolean isActualState) {
		if (!isActualState) state = state.getActualState(worldIn, pos);
		addCollisionBoxToList(pos, entityBox, collidingBoxes, bbCENTER); // deprecated my ass
		for (EnumFacing f : EnumFacing.VALUES) {
			if (state.getValue(propertyMap.get(f)).booleanValue()) {
				addCollisionBoxToList(pos, entityBox, collidingBoxes, bbMap.get(f));
			}
		}
	}
	
	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	// Rendering stuff
	@Override
	@SideOnly(Side.CLIENT)
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT;
	}
}
