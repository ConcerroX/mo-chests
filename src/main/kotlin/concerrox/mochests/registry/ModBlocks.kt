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
            mapColor(MapColor.WOOD)
            instrument(NoteBlockInstrument.BASS)
            strength(2.5f)
            sound(SoundType.WOOD)
            ignitedByLava()
        }) { ModBlockEntityTypes.CHEST.get() }
    }

}