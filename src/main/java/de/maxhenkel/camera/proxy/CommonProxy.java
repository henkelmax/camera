package de.maxhenkel.camera.proxy;

import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.gui.GuiHandler;
import de.maxhenkel.camera.net.*;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.text.SimpleDateFormat;

public class CommonProxy {

    public static SimpleDateFormat imageDateFormat=new SimpleDateFormat("MM/dd/yyyy HH:mm");
    public static long imageCooldown=5000;

    public static SimpleNetworkWrapper simpleNetworkWrapper;
    public static PacketManager manager;

    public void preinit(FMLPreInitializationEvent event) {

        initConfig(event);

        CommonProxy.simpleNetworkWrapper = NetworkRegistry.INSTANCE.newSimpleChannel(Main.MODID);
        CommonProxy.manager = new PacketManager();
        CommonProxy.simpleNetworkWrapper.registerMessage(MessagePartialImage.class, MessagePartialImage.class, 0, Side.SERVER);
        CommonProxy.simpleNetworkWrapper.registerMessage(MessageTakeImage.class, MessageTakeImage.class, 1, Side.CLIENT);
        CommonProxy.simpleNetworkWrapper.registerMessage(MessageRequestImage.class, MessageRequestImage.class, 2, Side.SERVER);
        CommonProxy.simpleNetworkWrapper.registerMessage(MessageImage.class, MessageImage.class, 3, Side.CLIENT);
        CommonProxy.simpleNetworkWrapper.registerMessage(MessageUpdateImage.class, MessageUpdateImage.class, 4, Side.CLIENT);
        CommonProxy.simpleNetworkWrapper.registerMessage(MessageImageUnavailable.class, MessageImageUnavailable.class, 5, Side.CLIENT);
    }

    public void init(FMLInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(Main.instance(), new GuiHandler());
    }

    public void postinit(FMLPostInitializationEvent event) {

    }

    private void initConfig(FMLPreInitializationEvent event){
        try {
            Configuration config = new Configuration(event.getSuggestedConfigurationFile());
            config.load();

            String format=config.getString("image_date_format", "camera", "MM/dd/yyyy HH:mm", "The format the date will be displayed on the image");
            imageCooldown=config.getInt("image_cooldown", "camera", 5000, 100, Integer.MAX_VALUE, "The time in milliseconds the camera will be on cooldown after taking an image");
            imageDateFormat=new SimpleDateFormat(format);

            config.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
