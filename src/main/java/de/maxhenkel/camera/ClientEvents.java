package de.maxhenkel.camera;

import com.mojang.blaze3d.platform.GlStateManager;
import de.maxhenkel.camera.items.CameraItem;
import de.maxhenkel.camera.net.MessageDisableCameraMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHelper;
import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.lang.reflect.Method;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Main.MODID, value = Dist.CLIENT)
public class ClientEvents {

    private static final ResourceLocation VIEWFINDER = new ResourceLocation(Main.MODID, "textures/gui/viewfinder_overlay.png");
    private static final ResourceLocation ZOOM = new ResourceLocation(Main.MODID, "textures/gui/zoom.png");

    public static final double MAX_FOV = 90D;
    public static final double MIN_FOV = 5D;

    private Minecraft mc;
    private boolean inCameraMode;
    private double fov;
    private ResourceLocation currentShader;

    public ClientEvents() {
        mc = Minecraft.getInstance();
        inCameraMode = false;
        fov = 0D;

        try {
            Method m;
            try {
                m = ObfuscationReflectionHelper.findMethod(MouseHelper.class, "func_198020_a", long.class, double.class, double.class);
            } catch (Exception e) {
                m = ObfuscationReflectionHelper.findMethod(MouseHelper.class, "scrollCallback", long.class, double.class, double.class);
            }

            Method scrollCallback = m;
            GLFW.glfwSetScrollCallback(mc.mainWindow.getHandle(), (window, xoffset, yoffset) -> {
                if (!scrollCallback(window, xoffset, yoffset)) {
                    try {
                        scrollCallback.invoke(mc.mouseHelper, window, xoffset, yoffset);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
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

        mc.gameSettings.thirdPersonView = 0;
        setShader(getShader(mc.player));

        drawViewFinder();
        drawZoom(getFOVPercentage());
    }

    private void drawViewFinder() {
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

    private void drawZoom(double percent) {

        GlStateManager.pushMatrix();

        mc.getTextureManager().bindTexture(ZOOM);

        int zoomWidth = 112;
        int zoomHeight = 20;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        int width = mc.mainWindow.getScaledWidth();
        int height = mc.mainWindow.getScaledHeight();

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        int left = (width - zoomWidth) / 2;
        int top = height / 40;

        buffer.pos(left, top, 0D).tex(0D, 0D).endVertex();
        buffer.pos(left, top + zoomHeight / 2, 0D).tex(0D, 0.5D).endVertex();
        buffer.pos(left + zoomWidth, top + zoomHeight / 2, 0D).tex(1D, 0.5D).endVertex();
        buffer.pos(left + zoomWidth, top, 0D).tex(1D, 0D).endVertex();

        int percWidth = (int) (Math.max(Math.min(percent, 1D), 0F) * (float) zoomWidth);

        buffer.pos(left, top, 0D).tex(0D, 0.5D).endVertex();
        buffer.pos(left, top + zoomHeight / 2, 0D).tex(0D, 1D).endVertex();
        buffer.pos(left + percWidth, top + zoomHeight / 2, 0D).tex(1D * percent, 1D).endVertex();
        buffer.pos(left + percWidth, top, 0D).tex(1D * percent, 0.5D).endVertex();

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
            if (event.getGui() instanceof IngameMenuScreen) {
                Main.SIMPLE_CHANNEL.sendToServer(new MessageDisableCameraMode());
                event.setCanceled(true);
            }
        }
    }

    private ResourceLocation getShader(PlayerEntity player) {
        ItemStack stack = mc.player.getHeldItemMainhand();
        if (!stack.getItem().equals(Main.CAMERA)) {
            return null;
        }

        return Shaders.getShader(Main.CAMERA.getShader(stack));
    }

    private void setShader(ResourceLocation shader) {
        if (shader == null) {
            mc.gameRenderer.stopUseShader();
        } else if (!shader.equals(currentShader)) {
            try {
                mc.gameRenderer.loadShader(shader);
            } catch (Exception e) {
            }
        }
        currentShader = shader;
    }

    @SubscribeEvent
    public void renderPlayer(RenderPlayerEvent.Pre event) {
        PlayerEntity player = event.getEntityPlayer();
        if (player == mc.player) {
            return;
        }
        for (Hand hand : Hand.values()) {
            ItemStack stack = player.getHeldItem(hand);
            if (stack.getItem() instanceof CameraItem && Main.CAMERA.isActive(stack)) {
                player.setActiveHand(hand);
            }
        }

    }

    @SubscribeEvent
    public void renderPlayer(RenderPlayerEvent.Post event) {
        PlayerEntity player = event.getEntityPlayer();
        if (player == mc.player) {
            return;
        }
        if (!inCameraMode) {
            return;
        }

        event.getEntityPlayer().resetActiveHand();
    }

    private boolean scrollCallback(long handle, double xoffset, double yoffset) {
        if (!inCameraMode) {
            return false;
        }

        if (yoffset < 0) {
            fov = Math.min(fov + 5F, MAX_FOV);
        } else {
            fov = Math.max(fov - 5F, MIN_FOV);
        }
        return true;
    }

    // TODO Implement when the MouseEvent is available
    /*
    @SubscribeEvent
    public void onMouseEvent(MouseEvent event) {
        if (event.getDwheel() == 0) {
            return;
        }
        if (!inCameraMode) {
            return;
        }

        if (event.getDwheel() < 0) {
            fov = Math.min(fov + 5F, MAX_FOV);
        } else {
            fov = Math.max(fov - 5F, MIN_FOV);
        }
        event.setCanceled(true);
    }
    */

    @SubscribeEvent
    public void onFOVModifierEvent(EntityViewRenderEvent.FOVModifier event) {
        if (!inCameraMode) {
            fov = event.getFOV();
            return;
        }

        /*
            To trigger the rendering of the chunks that were outside of the FOV
        */
        mc.player.posY += 0.000000001D;

        event.setFOV(fov);
    }

    public double getFOVPercentage() {
        return 1D - (fov - MIN_FOV) / (MAX_FOV - MIN_FOV);
    }

    private ItemStack getActiveCamera() {
        if (mc.player == null) {
            return null;
        }
        for (Hand hand : Hand.values()) {
            ItemStack stack = mc.player.getHeldItem(hand);
            if (stack.getItem().equals(Main.CAMERA) && Main.CAMERA.isActive(stack)) {
                return stack;
            }
        }
        return null;
    }

    private boolean isInCameraMode() {
        return getActiveCamera() != null;
    }

}