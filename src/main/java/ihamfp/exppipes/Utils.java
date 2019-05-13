package ihamfp.exppipes;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.oredict.OreDictionary;

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

	@SuppressWarnings("serial")
	public static final List<EnumFacing> pitches = new ArrayList<EnumFacing>() {{
		add(EnumFacing.UP);
		add(EnumFacing.NORTH);
		add(EnumFacing.DOWN);
	}};
	
	public static float getPitchAngle(EnumFacing f) {
		switch (f) {
		case UP:
			return 90.0f;
		case DOWN:
			return -90.0f;
		default:
			return 0.0f;	
		}
	}
	
	public static boolean bbContainsEq(AxisAlignedBB bb, float x, float y, float z) { // Literally AxisAlignedBB.contains with <=/>= 
		if (x >= bb.minX && x <= bb.maxX) {
			if (y >= bb.minY && y <= bb.maxY) {
				return z >= bb.minZ && z <= bb.maxZ;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	
	public static String commonOredict(List<ItemStack> stacks) {
		if (stacks.size() == 0) return null;
		List<Integer> commonOres = new ArrayList<Integer>();
		for (int i : OreDictionary.getOreIDs(stacks.get(0))) commonOres.add(i);
		if (commonOres.size() == 0) return null;
		for (int i=1; i<stacks.size(); i++) {
			ItemStack stack = stacks.get(i);
			List<Integer> stackOres = new ArrayList<Integer>();
			for (int o : OreDictionary.getOreIDs(stack)) stackOres.add(o);
			commonOres.removeIf(o -> !stackOres.contains(o));
			if (commonOres.size() == 0) return null;
		}
		if (commonOres.size() == 0) return null;
		return OreDictionary.getOreName(commonOres.get(0));
	}
}
