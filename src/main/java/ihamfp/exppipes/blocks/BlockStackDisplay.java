package ihamfp.exppipes.blocks;

import ihamfp.exppipes.ExppipesMod;
import ihamfp.exppipes.ModCreativeTabs;
import ihamfp.exppipes.Utils;
import ihamfp.exppipes.tileentities.TileEntityStackDisplay;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockStackDisplay extends Block {
	public static final PropertyDirection yaw = PropertyDirection.create("yaw", EnumFacing.Plane.HORIZONTAL);
	public static final PropertyDirection pitch = PropertyDirection.create("pitch", Utils.pitches);
	
	public BlockStackDisplay(String registryID) {
		super(Material.GLASS);
		this.setRegistryName(ExppipesMod.MODID, registryID);
		this.setCreativeTab(ModCreativeTabs.PIPES);
		this.setHardness(0.5f);
		this.setDefaultState(this.getDefaultState()
				.withProperty(yaw, EnumFacing.NORTH)
				.withProperty(pitch, EnumFacing.NORTH));
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (((state.getValue(pitch).equals(EnumFacing.NORTH) && state.getValue(yaw).equals(facing.getOpposite())) || !(state.getValue(pitch).equals(EnumFacing.NORTH) && state.getValue(pitch).equals(facing))) && !playerIn.getHeldItem(hand).isEmpty() && !playerIn.isSneaking()) { // set item if clicked on the displaying face only
			TileEntity te = worldIn.getTileEntity(pos);
			if (te != null && te instanceof TileEntityStackDisplay) {
				TileEntityStackDisplay tesd = (TileEntityStackDisplay)te;
				if (!tesd.computerControlled) {
					tesd.displayedStack = playerIn.getHeldItem(hand).copy();
					return true;
				}
			}
		}
		return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
	}
	
	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileEntityStackDisplay();
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, yaw, pitch);
	}
	
	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
		if (EnumFacing.Plane.HORIZONTAL.test(facing)) {
			return this.getDefaultState().withProperty(yaw, facing.getOpposite());
		} else {
			return this.getDefaultState().withProperty(pitch, facing).withProperty(yaw, placer.getHorizontalFacing());
		}
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(yaw, EnumFacing.HORIZONTALS[meta&3]).withProperty(pitch, Utils.pitches.get(((meta>>2)&3)%3));
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(yaw).getHorizontalIndex() | Utils.pitches.indexOf(state.getValue(pitch))<<2;
	}
	
	public EnumFacing getFrontFace(IBlockState state) {
		return (state.getValue(pitch)==EnumFacing.NORTH?state.getValue(yaw):state.getValue(pitch));
	}
}
