package de.maxhenkel.camera;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import de.maxhenkel.camera.items.CameraItem;
import de.maxhenkel.camera.net.MessageDisableCameraMode;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.CoreShaders;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Matrix4f;

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
        RenderSystem.setShader(CoreShaders.POSITION_TEX);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShaderTexture(0, VIEWFINDER);
        float imageWidth = 192F;
        float imageHeight = 100F;

        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        float ws = (float) mc.getWindow().getGuiScaledWidth();
        float hs = (float) mc.getWindow().getGuiScaledHeight();

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

        Matrix4f matrix = guiGraphics.pose().last().pose();
        buffer.addVertex(matrix, left, top, 0F).setUv(0F, 0F);
        buffer.addVertex(matrix, left, top + hnew, 0F).setUv(0F, 100F / 256F);
        buffer.addVertex(matrix, left + wnew, top + hnew, 0F).setUv(192F / 256F, 100F / 256F);
        buffer.addVertex(matrix, left + wnew, top, 0F).setUv(192F / 256F, 0F);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private void drawZoom(GuiGraphics guiGraphics, float percent) {
        RenderSystem.setShader(CoreShaders.POSITION_TEX);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShaderTexture(0, ZOOM);

        int zoomWidth = 112;
        int zoomHeight = 20;

        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();

        int left = (width - zoomWidth) / 2;
        int top = height / 40;

        Matrix4f matrix = guiGraphics.pose().last().pose();
        buffer.addVertex(matrix, left, top, 0F).setUv(0F, 0F);
        buffer.addVertex(matrix, left, (float) (top + zoomHeight / 2), 0F).setUv(0F, 10F / 128F);
        buffer.addVertex(matrix, left + zoomWidth, (float) (top + zoomHeight / 2), 0F).setUv(112F / 128F, 10F / 128F);
        buffer.addVertex(matrix, left + zoomWidth, top, 0F).setUv(112F / 128F, 0F);

        int percWidth = (int) (Math.max(Math.min(percent, 1D), 0F) * (float) zoomWidth);

        buffer.addVertex(matrix, left, top, 0F).setUv(0F, 10F / 128F);
        buffer.addVertex(matrix, left, (float) (top + zoomHeight / 2), 0F).setUv(0F, 20F / 128F);
        buffer.addVertex(matrix, left + percWidth, (float) (top + zoomHeight / 2), 0F).setUv((112F / 128F) * percent, 20F / 128F);
        buffer.addVertex(matrix, left + percWidth, top, 0F).setUv((112F / 128F) * percent, 10F / 128F);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
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
                PacketDistributor.sendToServer(new MessageDisableCameraMode());
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