package de.maxhenkel.camera;

import de.maxhenkel.camera.proxy.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = Main.MODID, version = "1.0.10", acceptedMinecraftVersions = "[1.12.2]", updateJSON = "https://maxhenkel.de/update/camera.json", dependencies = "")
public class Main {

    public static final String MODID = "camera";

    @Mod.Instance
    private static Main instance;

    @SidedProxy(clientSide = "de.maxhenkel.camera.proxy.ClientProxy", serverSide = "de.maxhenkel.camera.proxy.CommonProxy")
    public static CommonProxy proxy;

    public Main() {
        instance = this;
    }

    @Mod.EventHandler
    public void preinit(FMLPreInitializationEvent event) {
        proxy.preinit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postinit(FMLPostInitializationEvent event) {
        proxy.postinit(event);
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        proxy.serverStarting(event);
    }


    public static Main instance() {
        return instance;
    }

}
