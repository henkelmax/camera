package de.maxhenkel.camera;

import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.UUID;

public class StorageFile implements IStorage {
    public static File getImageFile(File file, UUID uuid) {
        File imageFolder = new File(file, "camera_images");
        return new File(imageFolder, uuid.toString() + ".png");
    }


    @Override
    public void saveImage(File file, UUID uuid, ByteBuffer data) {
        File image = getImageFile(file, uuid);
        image.mkdirs();
        ImageIO.write((RenderedImage) data, "png", image);
    }


    @Override
    public Optional<ByteBuffer> loadImage(File file, UUID uuid) {
        File image = getImageFile(file, uuid);
        FileInputStream fis = new FileInputStream(image);
        Optional<ByteBuffer> bufferedImage = ImageIO.read(fis);

        if (bufferedImage == null) {
            throw new IOException("BufferedImage is null");
        }
        return bufferedImage;
    }
}
