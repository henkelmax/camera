package de.maxhenkel.camera.items;

import de.maxhenkel.camera.blocks.BlockImageFrame;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.EnumActionResult;

public class ItemImageFrame extends ItemBlock {

    public ItemImageFrame(Block block) {
        super(block, new Item.Properties().group(ItemGroup.DECORATIONS));
    }

    @Override
    public EnumActionResult onItemUse(ItemUseContext ctx) {
        if (BlockImageFrame.canStay(ctx.getFace(), ctx.getWorld(), ctx.getPos().offset(ctx.getFace()))) {
            return super.onItemUse(ctx);
        }
        return EnumActionResult.FAIL;
    }
}
