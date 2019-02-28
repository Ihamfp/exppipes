package ihamfp.exppipes.tileentities;

import java.util.ArrayList;

import ihamfp.exppipes.ExppipesMod;
import ihamfp.exppipes.blocks.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModTileEntities {
	public static ArrayList<Class<? extends TileEntity>> modTileEntities = new ArrayList<Class<? extends TileEntity>>();
	
	public static void registerTileEntities() {
		// for when you forget to register a TileEntity
		for (Block b : ModBlocks.modBlocks) {
			for (int meta=0; meta<16;meta++) { // check all 16 possible meta values
				@SuppressWarnings("deprecation")
				IBlockState metaState = b.getStateFromMeta(meta); // TODO cycle through all possible states, not just from meta
				if (!b.hasTileEntity(metaState)) continue;
				
				TileEntity blockTE = b.createTileEntity(null, metaState);
				if (blockTE == null) continue;
				Class<? extends TileEntity> teClass = blockTE.getClass();
				if (!modTileEntities.contains(teClass)) {
					modTileEntities.add(teClass);
				}
			}
		}
		
		for (Class<? extends TileEntity> te : modTileEntities) {
			GameRegistry.registerTileEntity(te, new ResourceLocation(ExppipesMod.MODID, te.getSimpleName()));
		}
	}
}
