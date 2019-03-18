package de.maxhenkel.camera;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = Main.MODID)
public class Registry {

    public static void addRenderItem(Item item) {
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }

    public static void addRenderBlock(Block b) {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(b), 0, new ModelResourceLocation(b.getRegistryName(), "inventory"));
    }

    public static void registerItem(IForgeRegistry<Item> registry, Item i) {
        registry.register(i);
    }

    public static void registerBlock(IForgeRegistry<Block> registry, Block b) {
        registry.register(b);
    }

    public static void registerItemBlock(IForgeRegistry<Item> registry, Block b) {
        registerItem(registry, new ItemBlock(b).setRegistryName(b.getRegistryName()));
    }

    public static void regiserRecipe(IForgeRegistry<IRecipe> registry, IRecipe recipe) {
        registry.register(recipe);
    }

    @SubscribeEvent
    public void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        GameRegistry.addShapedRecipe(new ResourceLocation(Main.MODID, "camera"), null, new ItemStack(ModItems.CAMERA, 1), "MRB", "MGM", "MMM",
                'M', new ItemStack(Items.IRON_INGOT), 'R', new ItemStack(Items.REDSTONE), 'B', new ItemStack(Blocks.WOODEN_BUTTON), 'G', new ItemStack(Blocks.GLASS_PANE));

        GameRegistry.addShapedRecipe(new ResourceLocation(Main.MODID, "image_frame"), null, new ItemStack(ModItems.IMAGE_FRAME, 1), "WSW", "WLW", "WWW",
                'W', new ItemStack(Items.STICK), 'S', new ItemStack(Items.STRING), 'L', new ItemStack(Items.LEATHER));

        GameRegistry.addShapelessRecipe(new ResourceLocation(Main.MODID, "album"), null,
                new ItemStack(ModItems.ALBUM),
                Ingredient.fromItem(Items.LEATHER),
                Ingredient.fromItem(Items.STRING),
                Ingredient.fromItem(Items.PAPER),
                Ingredient.fromItem(Items.PAPER),
                Ingredient.fromItem(Items.PAPER));

        regiserRecipe(event.getRegistry(), new RecipeCopyImage().setRegistryName(new ResourceLocation(Main.MODID, "copy_image")));
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {

    }

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        event.getRegistry().register(ModSounds.TAKE_IMAGE);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        registerItem(event.getRegistry(), ModItems.CAMERA);
        registerItem(event.getRegistry(), ModItems.IMAGE);
        registerItem(event.getRegistry(), ModItems.IMAGE_FRAME);
        registerItem(event.getRegistry(), ModItems.ALBUM);
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        addRenderItem(ModItems.CAMERA);
        addRenderItem(ModItems.IMAGE);
        addRenderItem(ModItems.IMAGE_FRAME);
        addRenderItem(ModItems.ALBUM);
    }

}
