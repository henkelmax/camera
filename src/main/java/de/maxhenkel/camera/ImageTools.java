package de.maxhenkel.camera;

import com.sun.javafx.application.PlatformImpl;
import javafx.stage.FileChooser;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.UUID;
import java.util.function.Consumer;

public class ImageTools {

    public static BufferedImage fromNativeImage(NativeImage nativeImage) {
        BufferedImage bufferedImage = new BufferedImage(nativeImage.getWidth(), nativeImage.getHeight(), BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < nativeImage.getWidth(); x++) {
            for (int y = 0; y < nativeImage.getHeight(); y++) {
                int rgba = nativeImage.getPixelRGBA(x, y);
                int alpha = getAlpha(rgba);
                int red = getRed(rgba);
                int green = getGreen(rgba);
                int blue = getBlue(rgba);
                bufferedImage.setRGB(x, y, getArgb(alpha, blue, green, red));
            }
        }

        return bufferedImage;
    }

    private static int getArgb(int a, int red, int green, int blue) {
        return a << 24 | red << 16 | green << 8 | blue;
    }

    private static int getAlpha(int argb) {
        return (argb >> 24) & 0xFF;
    }

    private static int getRed(int argb) {
        return (argb >> 16) & 0xFF;
    }

    private static int getGreen(int argb) {
        return (argb >> 8) & 0xFF;
    }

    private static int getBlue(int argb) {
        return argb & 0xFF;
    }

    public static NativeImage toNativeImage(BufferedImage bufferedImage) {
        NativeImage nativeImage = new NativeImage(bufferedImage.getWidth(), bufferedImage.getHeight(), false);
        for (int x = 0; x < bufferedImage.getWidth(); x++) {
            for (int y = 0; y < bufferedImage.getHeight(); y++) {
                int rgba = bufferedImage.getRGB(x, y);
                int alpha = getAlpha(rgba);
                int red = getRed(rgba);
                int green = getGreen(rgba);
                int blue = getBlue(rgba);
                nativeImage.setPixelRGBA(x, y, getArgb(alpha, blue, green, red));
            }
        }
        return nativeImage;
    }

    public static byte[] toBytes(BufferedImage image) throws IOException {
        ImageIO.setUseCache(false);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        baos.flush();
        byte[] data = baos.toByteArray();
        baos.close();
        return data;
    }

    public static BufferedImage fromBytes(byte[] data) throws IOException {
        ImageIO.setUseCache(false);
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        BufferedImage image = ImageIO.read(bais);
        bais.close();
        return image;
    }

    public static BufferedImage resize(BufferedImage img, int newW, int newH) {
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return dimg;
    }

    public static File getImageFile(ServerPlayerEntity playerMP, UUID uuid) {
        File imageFolder = new File(playerMP.getServerWorld().getSaveHandler().getWorldDirectory(), "camera_images");
        return new File(imageFolder, uuid.toString() + ".png");
    }

    public static void saveImage(ServerPlayerEntity playerMP, UUID uuid, BufferedImage bufferedImage) throws IOException {
        File image = getImageFile(playerMP, uuid);
        image.mkdirs();
        ImageIO.write(bufferedImage, "png", image);
    }

    public static BufferedImage loadImage(ServerPlayerEntity playerMP, UUID uuid) throws IOException {
        return loadImage(ImageTools.getImageFile(playerMP, uuid));
    }

    public static BufferedImage loadImage(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);

        BufferedImage bufferedImage = ImageIO.read(fis);

        if (bufferedImage == null) {
            throw new IOException("BufferedImage is null");
        }

        return bufferedImage;
    }

    public static void chooseImage(Consumer<File> onResult) {
        PlatformImpl.startup(() -> {
            FileChooser chooser = new FileChooser();
            String lastPath = Config.CLIENT.LAST_IMAGE_PATH.get();
            if (!lastPath.isEmpty()) {
                chooser.setInitialDirectory(new File(lastPath));
            }
            chooser.setTitle(new TranslationTextComponent("title.choose_image").getFormattedText());
            FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter(new TranslationTextComponent("filetype.images").getFormattedText(), "*.png", "*.jpg", "*.jpeg");
            chooser.getExtensionFilters().clear();
            chooser.getExtensionFilters().add(filter);
            chooser.setSelectedExtensionFilter(filter);
            File file = chooser.showOpenDialog(null);
            if (file != null && file.exists() && !file.isDirectory()) {
                Config.CLIENT.LAST_IMAGE_PATH.set(file.getParent());
                Config.CLIENT.LAST_IMAGE_PATH.save();
                onResult.accept(file);
            }
        });
    }

}
