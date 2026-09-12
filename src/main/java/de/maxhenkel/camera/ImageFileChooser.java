package de.maxhenkel.camera;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.sdl.SDLDialog;
import org.lwjgl.sdl.SDLError;
import org.lwjgl.sdl.SDLProperties;
import org.lwjgl.sdl.SDL_DialogFileCallback;
import org.lwjgl.sdl.SDL_DialogFileFilter;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

import static org.lwjgl.system.MemoryUtil.*;

public final class ImageFileChooser {

    private static final SDL_DialogFileCallback CALLBACK = SDL_DialogFileCallback.create(ImageFileChooser::onResult);

    private static ImageFileChooser activeChooser;

    private final Consumer<File> onResult;
    private SDL_DialogFileFilter.Buffer filters;
    private ByteBuffer filterName;
    private ByteBuffer filterPattern;
    private int properties;

    private ImageFileChooser(Consumer<File> onResult) {
        this.onResult = onResult;
    }

    public static void open(File initialDirectory, Consumer<File> onResult) {
        if (activeChooser != null) {
            return;
        }

        ImageFileChooser chooser = new ImageFileChooser(onResult);
        activeChooser = chooser;
        chooser.open(initialDirectory);
    }

    public static boolean isOpen() {
        return activeChooser != null;
    }

    private void open(File initialDirectory) {
        try {
            filterName = memUTF8(Component.translatable("filetype.images").getString());
            filterPattern = memUTF8("png;jpg;jpeg");
            filters = SDL_DialogFileFilter.malloc(1);
            filters.get(0).set(filterName, filterPattern);

            properties = SDLProperties.SDL_CreateProperties();
            if (properties == 0
                    || !SDLProperties.SDL_SetPointerProperty(properties, SDLDialog.SDL_PROP_FILE_DIALOG_FILTERS_POINTER, filters.address())
                    || !SDLProperties.SDL_SetNumberProperty(properties, SDLDialog.SDL_PROP_FILE_DIALOG_NFILTERS_NUMBER, filters.remaining())
                    || !SDLProperties.SDL_SetPointerProperty(properties, SDLDialog.SDL_PROP_FILE_DIALOG_WINDOW_POINTER, Minecraft.getInstance().getWindow().handle())
                    || !SDLProperties.SDL_SetStringProperty(properties, SDLDialog.SDL_PROP_FILE_DIALOG_LOCATION_STRING, initialDirectory.getAbsolutePath())
                    || !SDLProperties.SDL_SetStringProperty(properties, SDLDialog.SDL_PROP_FILE_DIALOG_TITLE_STRING, Component.translatable("title.choose_image").getString())) {
                throw new IllegalStateException(SDLError.SDL_GetError());
            }

            SDLDialog.SDL_ShowFileDialogWithProperties(SDLDialog.SDL_FILEDIALOG_OPENFILE, CALLBACK, NULL, properties);
        } catch (RuntimeException e) {
            close();
            activeChooser = null;
            CameraMod.LOGGER.error("Failed to open image file dialog", e);
        }
    }

    private static void onResult(long userdata, long fileList, int filter) {
        String path = null;
        if (fileList == NULL) {
            CameraMod.LOGGER.error("Failed to open image file dialog: {}", SDLError.SDL_GetError());
        } else {
            long firstFile = memGetAddress(fileList);
            if (firstFile != NULL) {
                path = memUTF8(firstFile);
            }
        }

        String selectedPath = path;
        ImageFileChooser chooser = activeChooser;
        Minecraft.getInstance().schedule(() -> chooser.finish(selectedPath));
    }

    private void finish(String path) {
        close();
        activeChooser = null;

        if (path != null) {
            onResult.accept(new File(path));
        }
    }

    private void close() {
        if (properties != 0) {
            SDLProperties.SDL_DestroyProperties(properties);
            properties = 0;
        }
        if (filters != null) {
            filters.free();
            filters = null;
        }
        if (filterName != null) {
            memFree(filterName);
            filterName = null;
        }
        if (filterPattern != null) {
            memFree(filterPattern);
            filterPattern = null;
        }
    }

}
