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
	
	public static String formatNumber(int number) {
		if (number > 1000000000) {
			return Float.toString(number/1000000000.0f).substring(0, 3) + "T";
		}
		if (number > 1000000) {
			return Float.toString(number/1000000.0f).substring(0, 3) + "G";
		}
		if (number > 1000) {
			return Float.toString(number/1000.0f).substring(0, 3) + "k";
		}
		return Integer.toString(number);
	}
}
