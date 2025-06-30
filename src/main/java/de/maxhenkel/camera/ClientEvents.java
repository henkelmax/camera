package de.maxhenkel.camera;

import de.maxhenkel.camera.items.CameraItem;
import de.maxhenkel.camera.net.MessageDisableCameraMode;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ClientEvents {

    private static final ResourceLocation VIEWFINDER = ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/gui/viewfinder_overlay.png");
    private static final ResourceLocation ZOOM = ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/gui/zoom.png");

    public static final float MAX_FOV = 90F;
    public static final float MIN_FOV = 5F;

    private Minecraft mc;
    private boolean inCameraMode;
    private float fov;
    private ResourceLocation currentShader;

    public ClientEvents() {
        mc = Minecraft.getInstance();
        inCameraMode = false;
        fov = 0F;
    }

    @SubscribeEvent
    public void renderOverlay(RenderGuiLayerEvent.Pre event) {
        inCameraMode = isInCameraMode();

        if (!inCameraMode) {
            setShader(null);
            return;
        }

        event.setCanceled(true);

        mc.options.setCameraType(CameraType.FIRST_PERSON);

        setShader(getShader(mc.player));
        drawViewFinder(event.getGuiGraphics());
        drawZoom(event.getGuiGraphics(), getFOVPercentage());
    }

    private void drawViewFinder(GuiGraphics guiGraphics) {
        int imageWidth = 192;
        int imageHeight = 100;

        float ws = (float) mc.getWindow().getGuiScaledWidth();
        float hs = (float) mc.getWindow().getGuiScaledHeight();

        float rs = ws / hs;
        float ri = (float) imageWidth / (float) imageHeight;

        float hnew;
        float wnew;

        if (rs > ri) {
            wnew = (float) imageWidth * hs / (float) imageHeight;
            hnew = hs;
        } else {
            wnew = ws;
            hnew = (float) imageHeight * ws / (float) imageWidth;
        }

        float top = (hs - hnew) / 2F;
        float left = (ws - wnew) / 2F;

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, VIEWFINDER, (int) left, (int) top, 0F, 0F, (int) wnew, (int) hnew, 192, 100, 256, 256);
    }

    private void drawZoom(GuiGraphics guiGraphics, float percent) {
        int zoomWidth = 112;
        int zoomHeight = 10;

        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();

        int left = (width - zoomWidth) / 2;
        int top = height / 40;

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, ZOOM, left, top, 0F, 0F, zoomWidth, zoomHeight, zoomWidth, zoomHeight, 128, 128);
        int percWidth = (int) (Math.max(Math.min(percent, 1D), 0F) * (float) zoomWidth);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, ZOOM, left, top, 0F, zoomHeight, percWidth, zoomHeight, percWidth, zoomHeight, 128, 128);
    }


    @SubscribeEvent
    public void renderHand(RenderHandEvent event) {
        if (inCameraMode) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onGuiOpen(ScreenEvent.Opening event) {
        if (inCameraMode) {
            if (event.getScreen() instanceof PauseScreen) {
                ClientPacketDistributor.sendToServer(new MessageDisableCameraMode());
                event.setCanceled(true);
            }
        }
    }

    private ResourceLocation getShader(Player player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (!stack.getItem().equals(Main.CAMERA.get())) {
                continue;
            }
            return Shaders.getShader(stack.get(Main.SHADER_DATA_COMPONENT));
        }
        return null;
    }

    private void setShader(ResourceLocation shader) {
        if (shader == null) {
            if (currentShader != null) {
                mc.gameRenderer.clearPostEffect();
            }
        } else if (!shader.equals(currentShader)) {
            try {
                mc.gameRenderer.setPostEffect(shader);
            } catch (Exception e) {
            }
        }
        currentShader = shader;
    }

    @SubscribeEvent
    public void renderPlayer(ClientTickEvent.Pre event) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        List<AbstractClientPlayer> players = level.players();
        for (AbstractClientPlayer player : players) {
            if (player == mc.player) {
                continue;
            }
            for (InteractionHand hand : InteractionHand.values()) {
                ItemStack stack = player.getItemInHand(hand);
                if (stack.getItem() instanceof CameraItem) {
                    if (Main.CAMERA.get().isActive(stack)) {
                        player.startUsingItem(hand);
                    } else {
                        player.stopUsingItem();
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onMouseEvent(InputEvent.MouseScrollingEvent event) {
        if (event.getScrollDeltaY() == 0D) {
            return;
        }
        if (!inCameraMode) {
            return;
        }

        if (event.getScrollDeltaY() < 0D) {
            fov = Math.min(fov + 5F, MAX_FOV);
        } else {
            fov = Math.max(fov - 5F, MIN_FOV);
        }
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onFOVModifierEvent(ViewportEvent.ComputeFov event) {
        if (!inCameraMode) {
            fov = event.getFOV();
            return;
        }

        /*
            To trigger the rendering of the chunks that were outside of the FOV
        */
        mc.player.setPos(mc.player.getX(), mc.player.getY() + 0.000000001D, mc.player.getZ());

        event.setFOV(fov);
    }

    public float getFOVPercentage() {
        return 1F - (fov - MIN_FOV) / (MAX_FOV - MIN_FOV);
    }

    private ItemStack getActiveCamera() {
        if (mc.player == null) {
            return null;
        }
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = mc.player.getItemInHand(hand);
            if (stack.getItem().equals(Main.CAMERA.get()) && Main.CAMERA.get().isActive(stack)) {
                return stack;
            }
        }
        return null;
    }

    private boolean isInCameraMode() {
        return getActiveCamera() != null;
    }

}