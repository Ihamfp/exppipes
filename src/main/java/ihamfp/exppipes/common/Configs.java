package ihamfp.exppipes.common;

import ihamfp.exppipes.ExppipesMod;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.RangeInt;

@Config(modid = ExppipesMod.MODID)
public class Configs {
	@Comment({"Enable help about how to use GUIs"})
	public static boolean showHelpTooltips = true;
	
	@Comment({"Time in tick for an item to travel 1 pipe block"})
	@RangeInt(min=0)
	public static int travelTime = 4;
}
