package de.maxhenkel.camera;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;

@Mod.EventBusSubscriber(modid = Main.MODID)
@OnlyIn(Dist.CLIENT)
public class ClientEvents {

    private static final ResourceLocation VIEWFINDER = new ResourceLocation(Main.MODID, "textures/gui/viewfinder_overlay.png");

    private Minecraft mc;
    private boolean inCameraMode;

    public ClientEvents() {
        mc = Minecraft.getInstance();
    }

    @SubscribeEvent
    public void renderOverlay(RenderGameOverlayEvent.Pre event) {
        if (event.getType().equals(RenderGameOverlayEvent.ElementType.ALL)) {
            return;
        }

        inCameraMode = isInCameraMode();

        if (!inCameraMode) {
            return;
        }

        event.setCanceled(true);

        if (!event.getType().equals(RenderGameOverlayEvent.ElementType.EXPERIENCE)) {
            return;
        }

        GlStateManager.pushMatrix();

        mc.getTextureManager().bindTexture(VIEWFINDER);
        float imageWidth = 192F;
        float imageHeight = 100F;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        float ws = (float) mc.mainWindow.getScaledWidth();
        float hs = (float) mc.mainWindow.getScaledHeight();

        float rs = ws / hs;
        float ri = imageWidth / imageHeight;

        float hnew;
        float wnew;

        if (rs > ri) {
            wnew = imageWidth * hs / imageHeight;
            hnew = hs;
        } else {
            wnew = ws;
            hnew = imageHeight * ws / imageWidth;
        }

        float top = (hs - hnew) / 2F;
        float left = (ws - wnew) / 2F;

        buffer.pos(left, top, 0D).tex(0D, 0D).endVertex();
        buffer.pos(left, top + hnew, 0D).tex(0D, 1D).endVertex();
        buffer.pos(left + wnew, top + hnew, 0D).tex(1D, 1D).endVertex();
        buffer.pos(left + wnew, top, 0D).tex(1D, 0D).endVertex();

        tessellator.draw();

        GlStateManager.popMatrix();

    }

    @SubscribeEvent
    public void renderHand(RenderHandEvent event) {
        if(inCameraMode){
            event.setCanceled(true);
        }
    }

    private boolean isInCameraMode() {
        ItemStack stack = mc.player.getHeldItemMainhand();
        if (!stack.getItem().equals(Main.CAMERA)) {
            return false;
        }

        return Main.CAMERA.isActive(stack);
    }
}

/*
        //System.out.println("RENDER");
        GlStateManager.pushMatrix();

        mc.getTextureManager().bindTexture(VIEWFINDER);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        int width=mc.mainWindow.getScaledWidth();
        int height=mc.mainWindow.getScaledHeight();


        //mc.ingameGUI.drawTexturedModalRect(0, 0, 0, 0, mc.mainWindow.getScaledWidth(), mc.mainWindow.getScaledHeight());

        buffer.pos(0D, height, zLevel).tex(0D, 1D).endVertex();
        buffer.pos(width, height, zLevel).tex(1D, 1D).endVertex();
        buffer.pos(width, 0D, zLevel).tex(1D, 0D).endVertex();
        buffer.pos(0D, 0D, zLevel).tex(0D, 0D).endVertex();
        tessellator.draw();

        GlStateManager.popMatrix();*/