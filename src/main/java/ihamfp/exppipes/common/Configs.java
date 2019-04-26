package ihamfp.exppipes.common;

import ihamfp.exppipes.ExppipesMod;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.RangeInt;

@Config(modid = ExppipesMod.MODID)
public class Configs {
	@Comment({"Enable help about how to use GUIs"})
	public static boolean showHelpTooltips = true;
	
	@Comment({"Time in ticks for an item to travel 1 pipe block"})
	@RangeInt(min=0)
	public static int travelTime = 4;
	
	@Comment({"Time in ticks between extractions"})
	@RangeInt(min=0)
	public static int extractTime = 4;
	
	@Comment({"Upgraded time in ticks between extractions"})
	@RangeInt(min=0)
	public static int upgradedExtractTime = 1;

	@Comment({"Base maximum stack size to extract"})
	@RangeInt(min = 1,max = 64)
	public static int extractSize = 1;
	
	@Comment({"Upgraded maximum stack size to extract"})
	@RangeInt(min=1, max=64)
	public static int upgradedExtractSize = 16;
	
	@Comment({"Super upgraded maximum stack size to extract"})
	@RangeInt(min=1, max=64)
	public static int superUpgradedExtractSize = 64;
	
	@Comment({"Time in ticks between pipe network scan", "increasing will reduce server load, but also responsiveness"})
	@RangeInt(min=0)
	public static int updateInterval = 60; // In ticks, interval between searchNodes() calls
}
