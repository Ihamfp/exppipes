package ihamfp.exppipes.blocks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BlockPipeColor implements IBlockColor {

	@Override
	public int colorMultiplier(IBlockState state, IBlockAccess worldIn, BlockPos pos, int tintIndex) {
		if (tintIndex == 0) {
			EnumDyeColor dyeColor = state.getValue(BlockPipe.pipeColor);
			return dyeColor.getColorValue();
		}
		return 0xFFFFFF;
	}

}
