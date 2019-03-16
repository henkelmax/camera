package de.maxhenkel.camera;

import net.minecraft.entity.player.EntityPlayerMP;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.UUID;

public class ImageTools {

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

    public static File getImageFile(EntityPlayerMP playerMP, UUID uuid) {
        File imageFolder = new File(playerMP.getServerWorld().getSaveHandler().getWorldDirectory(), "camera_images");
        return new File(imageFolder, uuid.toString() + ".png");
    }

    public static void saveImage(EntityPlayerMP playerMP, UUID uuid, BufferedImage bufferedImage) throws IOException {
        File image = getImageFile(playerMP, uuid);
        image.mkdirs();
        ImageIO.write(bufferedImage, "png", image);
    }

    public static BufferedImage loadImage(EntityPlayerMP playerMP, UUID uuid) throws IOException {
        File image = ImageTools.getImageFile(playerMP, uuid);

        FileInputStream fis = new FileInputStream(image);

        BufferedImage bufferedImage = ImageIO.read(fis);

        if (bufferedImage == null) {
            throw new IOException("BufferedImage is null");
        }

        return bufferedImage;
    }

}
