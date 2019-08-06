package ihamfp.exppipes;

import java.util.ArrayList;
import java.util.List;

import ihamfp.exppipes.tileentities.InvCacheEntry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
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
	
	/*
	 * true: sort by ID
	 * false: sort by count
	 */
	public static boolean sortID = false;
	
	public static void invCacheSort(List<InvCacheEntry> l) {
		if (sortID) { // sort by id
			l.sort((a,b) -> Item.getIdFromItem(a.stack.getItem())-Item.getIdFromItem(b.stack.getItem()));
		} else {
			l.sort((a,b) -> b.count - a.count); // reverse count
		}
	}
	
	public static List<InvCacheEntry> invCacheSearch(List<InvCacheEntry> l, String s) {
		List<InvCacheEntry> r = new ArrayList<InvCacheEntry>();
		s = s.toLowerCase();
		for (int i=0; i<l.size(); i++) {
			String whereToSearch = l.get(i).stack.getDisplayName().toLowerCase();
			if (whereToSearch.contains(s)) r.add(l.get(i));
		}
		return r;
	}
	
	public static void smokeCenter(TileEntity te) {
		for (int i=0; i<10;i++) {
			double rx = te.getWorld().rand.nextDouble();
			double ry = te.getWorld().rand.nextDouble();
			double rz = te.getWorld().rand.nextDouble();
			te.getWorld().spawnParticle(EnumParticleTypes.SMOKE_NORMAL, true, te.getPos().getX()+0.5+rx, te.getPos().getY()+0.5+ry, te.getPos().getZ()+0.5+rz, 0.0, 0.0, 0.0);
		}
	}
}
