package de.maxhenkel.camera;

import de.maxhenkel.camera.proxy.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Main.MODID, version = Main.VERSION, acceptedMinecraftVersions=Main.MC_VERSION, updateJSON=Main.UPDATE_JSON, dependencies=Main.DEPENDENCIES)
public class Main {
	
    public static final String MODID = "camera";
    public static final String VERSION = "1.0.0";
    public static final String MC_VERSION = "[1.12.2]";
	public static final String UPDATE_JSON = "http://maxhenkel.de/update/camera.json";
	public static final String DEPENDENCIES = "required-after:forge@[14.23.2.2611,);";

	@Instance
    private static Main instance;

	@SidedProxy(clientSide="de.maxhenkel.camera.proxy.ClientProxy", serverSide="de.maxhenkel.camera.proxy.CommonProxy")
    public static CommonProxy proxy;
    
	public Main() {
		instance=this;
	}
	
    @EventHandler
    public void preinit(FMLPreInitializationEvent event){
		proxy.preinit(event);
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event){
    	 proxy.init(event);
    }
    
    @EventHandler
    public void postinit(FMLPostInitializationEvent event){
		proxy.postinit(event);
    }
    
	public static Main instance() {
		return instance;
	}
	
}
