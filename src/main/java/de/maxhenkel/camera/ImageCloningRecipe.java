package de.maxhenkel.camera;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.maxhenkel.camera.items.ImageItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public class ImageCloningRecipe extends CustomRecipe {

    private final ItemStack image;
    private final Ingredient paper;

    public ImageCloningRecipe(ItemStack image, Ingredient paper) {
        super(CraftingBookCategory.MISC);
        this.image = image;
        this.paper = paper;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, Ingredient.of(image), paper);
    }

    @Override
    public boolean matches(CraftingInput inv, Level worldIn) {
        return craft(inv) != null;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput inv) {
        CraftingResult craft = craft(inv);
        if (craft == null) {
            return null;
        }
        return craft.remaining;
    }

    @Override
    public ItemStack assemble(CraftingInput container, HolderLookup.Provider provider) {
        CraftingResult craft = craft(container);
        if (craft == null) {
            return null;
        }
        return craft.result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width > 1 && height > 1;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return image;
    }

    public ItemStack getImage() {
        return image;
    }

    public Ingredient getPaper() {
        return paper;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Main.IMAGE_CLONING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeType.CRAFTING;
    }

    @Override
    public CraftingBookCategory category() {
        return CraftingBookCategory.MISC;
    }

    public static class ImageCloningSerializer implements RecipeSerializer<ImageCloningRecipe> {

        private static final MapCodec<ImageCloningRecipe> CODEC = RecordCodecBuilder.mapCodec((builder) -> builder
                .group(
                        BuiltInRegistries.ITEM.byNameCodec().xmap(ItemStack::new, ItemStack::getItem)
                                .fieldOf("image")
                                .forGetter((recipe) -> recipe.image),
                        Ingredient.CODEC_NONEMPTY
                                .fieldOf("paper")
                                .forGetter((recipe) -> recipe.paper)
                ).apply(builder, ImageCloningRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, ImageCloningRecipe> STREAM_CODEC = StreamCodec.composite(
                ItemStack.STREAM_CODEC,
                ImageCloningRecipe::getImage,
                Ingredient.CONTENTS_STREAM_CODEC,
                ImageCloningRecipe::getPaper,
                ImageCloningRecipe::new
        );

        public ImageCloningSerializer() {

        }

        @Override
        public MapCodec<ImageCloningRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ImageCloningRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }

    protected CraftingResult craft(RecipeInput inv) {
        ItemStack image = null;
        NonNullList<ItemStack> remaining = NonNullList.withSize(inv.size(), ItemStack.EMPTY);
        int paperSlotIndex = -1;

        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getItem(i);

            if (stack.isEmpty()) {
                continue;
            }

            if (stack.getItem() instanceof ImageItem) {
                if (image != null) {
                    return null;
                }
                image = stack;
                remaining.set(i, image.copy());
            } else if (stack.is(Main.IMAGE_PAPER)) {
                if (paperSlotIndex >= 0) {
                    return null;
                }
                paperSlotIndex = i;
            }
        }

        if (image == null) {
            return null;
        }

        if (paperSlotIndex < 0) {
            return null;
        }

        ItemStack imageOut = image.copy();
        imageOut.setCount(1);

        return new CraftingResult(imageOut, remaining);
    }

    private static class CraftingResult {
        public final ItemStack result;
        public final NonNullList<ItemStack> remaining;

        public CraftingResult(ItemStack result, NonNullList<ItemStack> remaining) {
            this.result = result;
            this.remaining = remaining;
        }
    }
}
