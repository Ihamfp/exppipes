package ihamfp.exppipes;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class Utils {
	public static EnumFacing faceFromPos(BlockPos from, BlockPos to) {
		for (EnumFacing f : EnumFacing.VALUES) {
			if (from.offset(f).equals(to)) return f;
		}
		return null;
	}
}
