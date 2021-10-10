package de.maxhenkel.camera;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.UUID;

interface IStorage {

    void saveImage(File file, UUID uuid, ByteBuffer data);

    Optional<ByteBuffer> loadImage(File file, UUID uuid);
}

