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
	
	public static String formatNumber(long number) {
		if (number > 1000000000000L) {
			return Float.toString(number/1000000000000.0f).substring(0, 3) + "T";
		} else if (number > 1000000000L) {
			return Float.toString(number/1000000000.0f).substring(0, 3) + "G";
		} else if (number > 1000000L) {
			return Float.toString(number/1000000.0f).substring(0, 3) + "M";
		} else if (number > 1000L) {
			return Float.toString(number/1000.0f).substring(0, 3) + "k";
		}
		return Long.toString(number);
	}
}
