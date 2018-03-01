package de.maxhenkel.camera.proxy;

import de.maxhenkel.camera.blocks.tileentity.TileentityImage;
import de.maxhenkel.camera.blocks.tileentity.render.TileentitySpecialRendererImage;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

	public void preinit(FMLPreInitializationEvent event) {
		super.preinit(event);

	}

	public void init(FMLInitializationEvent event) {
		super.init(event);
		ClientRegistry.bindTileEntitySpecialRenderer(TileentityImage.class, new TileentitySpecialRendererImage());
	}

	public void postinit(FMLPostInitializationEvent event) {
		super.postinit(event);
	}

}
