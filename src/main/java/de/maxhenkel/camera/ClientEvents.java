package de.maxhenkel.camera;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxhenkel.camera.items.CameraItem;
import de.maxhenkel.camera.net.MessageDisableCameraMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.opengl.GL11;

@OnlyIn(Dist.CLIENT)
public class ClientEvents {

    private static final ResourceLocation VIEWFINDER = new ResourceLocation(Main.MODID, "textures/gui/viewfinder_overlay.png");
    private static final ResourceLocation ZOOM = new ResourceLocation(Main.MODID, "textures/gui/zoom.png");

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

        if (!event.getType().equals(RenderGameOverlayEvent.ElementType.HOTBAR)) {
            return;
        }

        mc.options.setCameraType(PointOfView.FIRST_PERSON);

        setShader(getShader(mc.player));
        drawViewFinder(event.getMatrixStack());
        drawZoom(event.getMatrixStack(), getFOVPercentage());
    }

    private void drawViewFinder(MatrixStack matrixStack) {
        mc.getTextureManager().bind(VIEWFINDER);
        float imageWidth = 192F;
        float imageHeight = 100F;

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuilder();
        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

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

        Matrix4f matrix = matrixStack.last().pose();
        bufferBuilder.vertex(matrix, left, top, 0F).uv(0F, 0F).endVertex();
        bufferBuilder.vertex(matrix, left, top + hnew, 0F).uv(0F, 100F / 256F).endVertex();
        bufferBuilder.vertex(matrix, left + wnew, top + hnew, 0F).uv(192F / 256F, 100F / 256F).endVertex();
        bufferBuilder.vertex(matrix, left + wnew, top, 0F).uv(192F / 256F, 0F).endVertex();

        bufferBuilder.end();
        WorldVertexBufferUploader.end(bufferBuilder);
    }

    private void drawZoom(MatrixStack matrixStack, float percent) {
        mc.getTextureManager().bind(ZOOM);

        int zoomWidth = 112;
        int zoomHeight = 20;

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuilder();
        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();

        int left = (width - zoomWidth) / 2;
        int top = height / 40;

        Matrix4f matrix = matrixStack.last().pose();
        bufferBuilder.vertex(matrix, left, top, 0F).uv(0F, 0F).endVertex();
        bufferBuilder.vertex(matrix, left, (float) (top + zoomHeight / 2), 0F).uv(0F, 10F / 128F).endVertex();
        bufferBuilder.vertex(matrix, left + zoomWidth, (float) (top + zoomHeight / 2), 0F).uv(112F / 128F, 10F / 128F).endVertex();
        bufferBuilder.vertex(matrix, left + zoomWidth, top, 0F).uv(112F / 128F, 0F).endVertex();

        int percWidth = (int) (Math.max(Math.min(percent, 1D), 0F) * (float) zoomWidth);

        bufferBuilder.vertex(matrix, left, top, 0F).uv(0F, 10F / 128F).endVertex();
        bufferBuilder.vertex(matrix, left, (float) (top + zoomHeight / 2), 0F).uv(0F, 20F / 128F).endVertex();
        bufferBuilder.vertex(matrix, left + percWidth, (float) (top + zoomHeight / 2), 0F).uv((112F / 128F) * percent, 20F / 128F).endVertex();
        bufferBuilder.vertex(matrix, left + percWidth, top, 0F).uv((112F / 128F) * percent, 10F / 128F).endVertex();

        bufferBuilder.end();
        WorldVertexBufferUploader.end(bufferBuilder);
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
        for (Hand hand : Hand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (!stack.getItem().equals(Main.CAMERA)) {
                continue;
            }
            return Shaders.getShader(Main.CAMERA.getShader(stack));
        }
        return null;
    }

    private void setShader(ResourceLocation shader) {
        if (shader == null) {
            if (currentShader != null) {
                mc.gameRenderer.shutdownEffect();
            }
        } else if (!shader.equals(currentShader)) {
            try {
                mc.gameRenderer.loadEffect(shader);
            } catch (Exception e) {
            }
        }
        currentShader = shader;
    }

    @SubscribeEvent
    public void renderPlayer(RenderPlayerEvent.Pre event) {
        PlayerEntity player = event.getPlayer();
        if (player == mc.player) {
            return;
        }
        for (Hand hand : Hand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof CameraItem && Main.CAMERA.isActive(stack)) {
                player.startUsingItem(hand);
            }
        }

    }

    @SubscribeEvent
    public void renderPlayer(RenderPlayerEvent.Post event) {
        PlayerEntity player = event.getPlayer();
        if (player == mc.player) {
            return;
        }
        if (!inCameraMode) {
            return;
        }

        event.getPlayer().stopUsingItem();
    }

    @SubscribeEvent
    public void onMouseEvent(InputEvent.MouseScrollEvent event) {
        if (event.getScrollDelta() == 0D) {
            return;
        }
        if (!inCameraMode) {
            return;
        }

        if (event.getScrollDelta() < 0D) {
            fov = Math.min(fov + 5F, MAX_FOV);
        } else {
            fov = Math.max(fov - 5F, MIN_FOV);
        }
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onFOVModifierEvent(EntityViewRenderEvent.FOVModifier event) {
        if (!inCameraMode) {
            fov = (float) event.getFOV();
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
        for (Hand hand : Hand.values()) {
            ItemStack stack = mc.player.getItemInHand(hand);
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