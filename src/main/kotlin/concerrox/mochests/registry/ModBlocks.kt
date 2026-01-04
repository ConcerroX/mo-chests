package concerrox.mochests.registry

import concerrox.mochests.MoChests
import concerrox.mochests.content.chest.MoChestBlock
import concerrox.mochests.util.new
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument
import net.minecraft.world.level.material.MapColor
import net.neoforged.neoforge.registries.DeferredRegister

object ModBlocks {

    internal val BLOCKS = DeferredRegister.createBlocks(MoChests.MOD_ID)

    val IRON_CHEST = BLOCKS.new("iron_chest") {
        MoChestBlock(BlockBehaviour.Properties.of().apply {
            mapColor(MapColor.METAL)
            instrument(NoteBlockInstrument.IRON_XYLOPHONE)
            strength(5f, 6f)
            sound(SoundType.METAL)
            requiresCorrectToolForDrops()
        }, MoChestBlock.ChestMaterial.IRON)
    }
    val GOLD_CHEST = BLOCKS.new("gold_chest") {
        MoChestBlock(BlockBehaviour.Properties.of().apply {
            mapColor(MapColor.GOLD)
            instrument(NoteBlockInstrument.BELL)
            strength(3f, 6f)
            sound(SoundType.METAL)
            requiresCorrectToolForDrops()
        }, MoChestBlock.ChestMaterial.GOLD)
    }
    val DIAMOND_CHEST = BLOCKS.new("diamond_chest") {
        MoChestBlock(BlockBehaviour.Properties.of().apply {
            mapColor(MapColor.DIAMOND)
            instrument(NoteBlockInstrument.BASS)
            strength(5f, 6f)
            sound(SoundType.METAL)
            requiresCorrectToolForDrops()
        }, MoChestBlock.ChestMaterial.DIAMOND)
    }
    val NETHERITE_CHEST = BLOCKS.new("netherite_chest") {
        MoChestBlock(BlockBehaviour.Properties.of().apply {
            mapColor(MapColor.COLOR_BLACK)
            instrument(NoteBlockInstrument.BASS)
            strength(50f, 1200f)
            sound(SoundType.NETHERITE_BLOCK)
            requiresCorrectToolForDrops()
        }, MoChestBlock.ChestMaterial.NETHERITE)
    }
    val GLASS_CHEST = BLOCKS.new("glass_chest") {
        MoChestBlock(BlockBehaviour.Properties.of().apply {
            instrument(NoteBlockInstrument.HAT)
            strength(0.3f)
            sound(SoundType.GLASS)
        }, MoChestBlock.ChestMaterial.GLASS)
    }

}