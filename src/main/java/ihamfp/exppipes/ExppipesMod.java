package ihamfp.exppipes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ihamfp.exppipes.interfaces.IProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = ExppipesMod.MODID, name = ExppipesMod.NAME, version = ExppipesMod.VERSION)
public class ExppipesMod {
	public static final String MODID = "exppipes";
    public static final String NAME = "Exponentialistics pipes";
    public static final String VERSION = "0.1";
    
    @Mod.Instance(MODID)
    public static ExppipesMod instance;
    
    @SidedProxy(serverSide = "ihamfp.exppipes.common.CommonProxy", clientSide = "ihamfp.exppipes.client.ClientProxy")
    public static IProxy proxy;
    
    public static final Logger logger = LogManager.getLogger(MODID);
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
    	proxy.preInit(event);
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event) {
    	proxy.init(event);
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
    	proxy.postInit(event);
    }
}
