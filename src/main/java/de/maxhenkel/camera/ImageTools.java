package de.maxhenkel.camera;

import com.sun.javafx.application.PlatformImpl;
import javafx.stage.FileChooser;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.storage.FolderName;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.UUID;
import java.util.function.Consumer;

public class ImageTools {

    public static FolderName CAMERA_IMAGES = new FolderName("camera_images");

    private static final int MAX_IMAGE_SIZE = 1920;

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
        ImageIO.write(image, "jpg", baos);
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

    public static byte[] optimizeImage(BufferedImage image) throws IOException {
        float ratio = ((float) image.getHeight()) / ((float) image.getWidth());
        int newWidth = image.getWidth();
        int newHeight = image.getHeight();

        if (image.getHeight() > MAX_IMAGE_SIZE || image.getWidth() > MAX_IMAGE_SIZE) {
            if (ratio < 1F) {
                newHeight = ((int) (((float) MAX_IMAGE_SIZE) * ratio));
                newWidth = MAX_IMAGE_SIZE;
            } else {
                newWidth = ((int) (((float) MAX_IMAGE_SIZE) * ratio));
                newHeight = MAX_IMAGE_SIZE;
            }
        }

        image = ImageTools.resize(image, newWidth, newHeight);

        float factor = 0.5F;
        byte[] data;

        while ((data = ImageTools.compressToBytes(image, factor)).length > Config.SERVER.MAX_IMAGE_SIZE.get()) {
            Main.LOGGER.debug("Trying to compress image: {}% {} bytes (max {})", Math.round(factor * 100F), data.length, Config.SERVER.MAX_IMAGE_SIZE.get());
            factor -= 0.025F;
            if (factor <= 0F) {
                throw new IOException("Image could not be compressed (too large)");
            }
        }

        Main.LOGGER.debug("Image compressed to {}% ({} bytes)", Math.round(factor * 100F), data.length);

        return data;
    }

    public static BufferedImage resize(BufferedImage img, int newW, int newH) {
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage bufferedImage = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = bufferedImage.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return bufferedImage;
    }

    public static BufferedImage compress(BufferedImage img, float factor) throws IOException {
        return fromBytes(compressToBytes(img, factor));
    }

    public static byte[] compressToBytes(BufferedImage img, float factor) throws IOException {
        ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
        ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
        jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        jpgWriteParam.setCompressionQuality(factor);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MemoryCacheImageOutputStream stream = new MemoryCacheImageOutputStream(baos);
        jpgWriter.setOutput(stream);
        IIOImage outputImage = new IIOImage(img, null, null);
        jpgWriter.write(null, outputImage, jpgWriteParam);
        jpgWriter.dispose();
        baos.flush();
        byte[] data = baos.toByteArray();
        baos.close();
        return data;
    }

    @Deprecated
    public static File getImageFileLegacy(ServerPlayerEntity playerMP, UUID uuid) {
        File imageFolder = playerMP.server.func_240776_a_(CAMERA_IMAGES).toFile();
        File image = new File(imageFolder, uuid.toString() + ".jpg");
        if (!image.exists()) {
            image = new File(imageFolder, uuid.toString() + ".png");
        }
        return image;
    }

    public static File getImageFile(ServerPlayerEntity playerMP, UUID uuid) {
        File imageFolder = playerMP.server.func_240776_a_(CAMERA_IMAGES).toFile();
        return new File(imageFolder, uuid.toString() + ".jpg");
    }

    public static void saveImage(ServerPlayerEntity playerMP, UUID uuid, BufferedImage bufferedImage) throws IOException {
        File image = getImageFile(playerMP, uuid);
        image.mkdirs();
        ImageIO.write(bufferedImage, "jpg", image);
    }

    public static BufferedImage loadImage(ServerPlayerEntity playerMP, UUID uuid) throws IOException {
        return loadImage(ImageTools.getImageFileLegacy(playerMP, uuid));
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
                File last = new File(lastPath);
                if (last.exists()) {
                    chooser.setInitialDirectory(last);
                }
            }
            chooser.setTitle(new TranslationTextComponent("title.choose_image").getString());
            FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter(new TranslationTextComponent("filetype.images").getString(), "*.png", "*.jpg", "*.jpeg");
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
