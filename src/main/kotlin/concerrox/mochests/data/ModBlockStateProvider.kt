package concerrox.mochests.data

import concerrox.mochests.MoChests
import concerrox.mochests.registry.ModBlocks
import net.minecraft.data.PackOutput
import net.neoforged.neoforge.client.model.generators.BlockStateProvider
import net.neoforged.neoforge.common.data.ExistingFileHelper


class ModBlockStateProvider(
    output: PackOutput, existingFileHelper: ExistingFileHelper
) : BlockStateProvider(output, MoChests.MOD_ID, existingFileHelper) {

    override fun registerStatesAndModels() {
        simpleBlock(
            ModBlocks.IRON_CHEST.get(),
            models().getBuilder(ModBlocks.IRON_CHEST.id.toString()).texture("particle", mcLoc("block/iron_block"))
        )
        simpleBlock(
            ModBlocks.GOLD_CHEST.get(),
            models().getBuilder(ModBlocks.GOLD_CHEST.id.toString()).texture("particle", mcLoc("block/gold_block"))
        )
        simpleBlock(
            ModBlocks.DIAMOND_CHEST.get(),
            models().getBuilder(ModBlocks.DIAMOND_CHEST.id.toString()).texture("particle", mcLoc("block/diamond_block"))
        )
        simpleBlock(
            ModBlocks.NETHERITE_CHEST.get(),
            models().getBuilder(ModBlocks.NETHERITE_CHEST.id.toString()).texture("particle", mcLoc("block/netherite_block"))
        )
        simpleBlock(
            ModBlocks.GLASS_CHEST.get(),
            models().getBuilder(ModBlocks.GLASS_CHEST.id.toString()).texture("particle", mcLoc("block/glass"))
        )
    }

}