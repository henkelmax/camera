package de.maxhenkel.camera;

import de.maxhenkel.camera.net.MessageDisableCameraMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiOpenEvent;
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
    private ResourceLocation currentShader;

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
            setShader(null);
            return;
        }

        event.setCanceled(true);

        if (!event.getType().equals(RenderGameOverlayEvent.ElementType.EXPERIENCE)) {
            return;
        }

        setShader(getShader(mc.player));

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
        if (inCameraMode) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (inCameraMode) {
            if(event.getGui() instanceof GuiIngameMenu){
                Main.SIMPLE_CHANNEL.sendToServer(new MessageDisableCameraMode());
                event.setCanceled(true);
            }
        }
    }

    private boolean isInCameraMode() {
        ItemStack stack = mc.player.getHeldItemMainhand();
        if (!stack.getItem().equals(Main.CAMERA)) {
            return false;
        }

        return Main.CAMERA.isActive(stack);
    }

    private ResourceLocation getShader(EntityPlayer player){
        ItemStack stack = mc.player.getHeldItemMainhand();
        if (!stack.getItem().equals(Main.CAMERA)) {
            return null;
        }

        return Shaders.getShader(Main.CAMERA.getShader(stack));
    }

    private void setShader(ResourceLocation shader) {
        if (shader == null) {
            mc.entityRenderer.stopUseShader();
        } else if (!shader.equals(currentShader)) {
            try {
                mc.entityRenderer.loadShader(shader);
            } catch (Exception e) {}
        }
        currentShader = shader;
    }
}