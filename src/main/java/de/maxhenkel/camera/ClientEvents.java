package de.maxhenkel.camera;

import de.maxhenkel.camera.items.ItemCamera;
import de.maxhenkel.camera.net.MessageDisableCameraMode;
import de.maxhenkel.camera.proxy.CommonProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = Main.MODID, value = Side.CLIENT)
public class ClientEvents {

    private static final ResourceLocation VIEWFINDER = new ResourceLocation(Main.MODID, "textures/gui/viewfinder_overlay.png");

    private Minecraft mc;
    private boolean inCameraMode;
    private ResourceLocation currentShader;

    public ClientEvents() {
        mc = Minecraft.getMinecraft();
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

        GlStateManager.pushMatrix();

        mc.getTextureManager().bindTexture(VIEWFINDER);
        float imageWidth = 192F;
        float imageHeight = 100F;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        ScaledResolution scaledresolution = new ScaledResolution(mc);
        float ws = (float) scaledresolution.getScaledWidth();
        float hs = (float) scaledresolution.getScaledHeight();

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
            if (event.getGui() instanceof GuiIngameMenu) {
                CommonProxy.simpleNetworkWrapper.sendToServer(new MessageDisableCameraMode());
                event.setCanceled(true);
            }
        }
    }

    private boolean isInCameraMode() {
        ItemStack stack = mc.player.getHeldItemMainhand();
        if (!stack.getItem().equals(ModItems.CAMERA)) {
            return false;
        }

        return ModItems.CAMERA.isActive(stack);
    }

    private ResourceLocation getShader(EntityPlayer player) {
        ItemStack stack = mc.player.getHeldItemMainhand();
        if (!stack.getItem().equals(ModItems.CAMERA)) {
            return null;
        }

        return Shaders.getShader(ModItems.CAMERA.getShader(stack));
    }

    private void setShader(ResourceLocation shader) {
        if (shader == null) {
            mc.entityRenderer.stopUseShader();
        } else if (!shader.equals(currentShader)) {
            try {
                mc.entityRenderer.loadShader(shader);
            } catch (Exception e) {
            }
        }
        currentShader = shader;
    }

    @SubscribeEvent
    public void renderPlayer(RenderPlayerEvent.Pre event) {
        EntityPlayer player = event.getEntityPlayer();
        if (player == mc.player) {
            return;
        }
        for (EnumHand hand : EnumHand.values()) {
            ItemStack stack = player.getHeldItem(hand);
            if (stack.getItem() instanceof ItemCamera && ModItems.CAMERA.isActive(stack)) {
                player.setActiveHand(hand);
            }
        }

    }

    @SubscribeEvent
    public void renderPlayer(RenderPlayerEvent.Post event) {
        EntityPlayer player = event.getEntityPlayer();
        if (player == mc.player) {
            return;
        }
        for (EnumHand hand : EnumHand.values()) {
            ItemStack stack = player.getHeldItem(hand);
            if (stack.getItem() instanceof ItemCamera && ModItems.CAMERA.isActive(stack)) {
                event.getEntityPlayer().resetActiveHand();
            }
        }
    }
}