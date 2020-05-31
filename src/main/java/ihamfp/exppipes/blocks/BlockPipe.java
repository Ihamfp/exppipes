package ihamfp.exppipes.blocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ihamfp.exppipes.ExppipesMod;
import ihamfp.exppipes.ModCreativeTabs;
import ihamfp.exppipes.Utils;
import ihamfp.exppipes.common.network.PacketHandler;
import ihamfp.exppipes.common.network.PacketSideDiscon;
import ihamfp.exppipes.pipenetwork.BlockDimPos;
import ihamfp.exppipes.pipenetwork.ItemDirection;
import ihamfp.exppipes.pipenetwork.PipeNetwork;
import ihamfp.exppipes.tileentities.TileEntityPipe;
import ihamfp.exppipes.tileentities.TileEntityRoutingPipe;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
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
	
	public static final PropertyBool opaque = PropertyBool.create("opaque");
	public static final PropertyEnum<EnumDyeColor> pipeColor = PropertyEnum.create("color", EnumDyeColor.class);
	
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
				.withProperty(connSOUTH, false)
				.withProperty(opaque, false)
				.withProperty(pipeColor, EnumDyeColor.BLACK));
	}
	
	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		TileEntityPipe newTE = new TileEntityPipe();
		//if (world != null) ExppipesMod.logger.info("Created TE in " + (world.isRemote?"remote":"server") + " world");
		return newTE;
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
	
	public int getGuiID() {
		return -1;
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		//if (!worldIn.isRemote) ExppipesMod.logger.info(hand.name() + "Click !");
		// Opacity/color
		if (worldIn.getTileEntity(pos) instanceof TileEntityPipe && !worldIn.isRemote) {
			TileEntityPipe te = (TileEntityPipe) worldIn.getTileEntity(pos);
			ItemStack heldItem = playerIn.getHeldItem(hand);
			if (heldItem.getItem() instanceof ItemDye) { // TODO support oreDict dye
				EnumDyeColor dyeColor = EnumDyeColor.byDyeDamage(heldItem.getMetadata());
				//ExppipesMod.logger.info("New color " + dyeColor.getName() + ", was " + (te.opaque?state.getValue(pipeColor).getName():"clear"));
				te.opaque = true;
				te.dyeColor = dyeColor;
				te.markDirty();
				worldIn.setBlockState(pos, state.withProperty(opaque, true).withProperty(pipeColor, dyeColor), 3);
				return true;
			} else if (te.opaque && heldItem.getItem() == Items.WATER_BUCKET) {
				te.opaque = false;
				te.markDirty();
				worldIn.setBlockState(pos, state.withProperty(opaque, false));
				return true;
			}
		}
		if (worldIn.isRemote && playerIn.getHeldItem(hand).getItem() == Items.WATER_BUCKET && state.getActualState(worldIn, pos).getValue(opaque)) {
			return true;
		}
		// Connection/Disconnection
		if (hand == EnumHand.MAIN_HAND && worldIn.getTileEntity(pos) instanceof TileEntityPipe) {
			if (playerIn.isSneaking()) {
				// get face to connect/disconnect
				EnumFacing f = null;
				if (Utils.bbContainsEq(bbCENTER, hitX, hitY, hitZ)) { // clicked on center/unconnected side
					f = facing;
				} else {
					for (EnumFacing g : EnumFacing.VALUES) {
						if (Utils.bbContainsEq(bbMap.get(g), hitX, hitY, hitZ)) {
							f = g;
						}
					}
				}
				// connect/disconnect
				if (f == null) return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
				TileEntityPipe te = (TileEntityPipe) worldIn.getTileEntity(pos);
				if (!worldIn.isRemote) {
					if (te.disableConnection.getOrDefault(f, false)) {
						te.disableConnection.remove(f);
					}
					else {
						te.disableConnection.put(f, true);
					}
					PacketHandler.INSTANCE.sendToAllTracking(new PacketSideDiscon(new BlockDimPos(te), f, te.disableConnection.getOrDefault(f, false)), new TargetPoint(worldIn.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 0.0));
					worldIn.setBlockState(pos, state.withProperty(propertyMap.get(f), te.disableConnection.getOrDefault(f,false)), 2);
				}
				// connect/disconnect other pipe
				if (!worldIn.isRemote && worldIn.getTileEntity(pos.offset(f)) instanceof TileEntityPipe) {
					BlockPos offsetPos = pos.offset(f);
					TileEntityPipe other = (TileEntityPipe) worldIn.getTileEntity(offsetPos);
					if (!te.disableConnection.getOrDefault(f, false) && other.disableConnection.getOrDefault(f.getOpposite(), false)) { // reconnect to the other pipe
						other.disableConnection.remove(f.getOpposite());
						//return true;
					} else if (te.disableConnection.getOrDefault(f, false) && !other.disableConnection.getOrDefault(f.getOpposite(), false)) { // disconnect the other pipe
						other.disableConnection.put(f.getOpposite(), true);
					}
					PacketHandler.INSTANCE.sendToAllTracking(new PacketSideDiscon(new BlockDimPos(other), f.getOpposite(), other.disableConnection.getOrDefault(f.getOpposite(), false)), new TargetPoint(worldIn.provider.getDimension(), offsetPos.getX(), offsetPos.getY(), offsetPos.getZ(), 0.0));
					worldIn.notifyBlockUpdate(other.getPos(), worldIn.getBlockState(other.getPos()), worldIn.getBlockState(other.getPos()).withProperty(propertyMap.get(f.getOpposite()), true), 2);
					other.markDirty();
				}
				te.markDirty();
				return true;
			} else if (this.getGuiID() > 0) {
				if (!worldIn.isRemote) playerIn.openGui(ExppipesMod.instance, this.getGuiID(), worldIn, pos.getX(), pos.getY(), pos.getZ());
				return true;
			}
		}
		
		return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
	}

	@Override
	public boolean canHarvestBlock(IBlockAccess world, BlockPos pos, EntityPlayer player) {
		return true;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, connUP, connDOWN, connEAST, connWEST, connNORTH, connSOUTH, opaque, pipeColor);
	}
	
	private boolean canConnectTo(BlockPos pos, EnumFacing onFace, IBlockAccess worldIn) {
		TileEntity teAtPos = worldIn.getTileEntity(pos);
		if (teAtPos == null) {
			Block blockAtPos = worldIn.getBlockState(pos).getBlock();
			if (blockAtPos instanceof BlockPipe) return true;
		} else {
			if (teAtPos.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, onFace)) return true;
		}
		return false;
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		TileEntity te = worldIn instanceof ChunkCache ? ((ChunkCache)worldIn).getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK) : worldIn.getTileEntity(pos);;
		if (te instanceof TileEntityPipe) {
			for (EnumFacing e : EnumFacing.VALUES) {
				boolean connect = canConnectTo(pos.offset(e), e.getOpposite(), worldIn);
				if (te != null) connect = (connect && !((TileEntityPipe)te).disableConnection.getOrDefault(e, false));
				state = state.withProperty(propertyMap.get(e), connect);
			}
			if (te != null) {
				state = state.withProperty(opaque, ((TileEntityPipe)te).opaque);
				if (((TileEntityPipe)te).dyeColor != null) state = state.withProperty(pipeColor, ((TileEntityPipe)te).dyeColor);
			}
		} else {
			//ExppipesMod.logger.info("Mismatched TE at " + pos.toString() + ", was " + (te==null?"null":te.toString()));
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
