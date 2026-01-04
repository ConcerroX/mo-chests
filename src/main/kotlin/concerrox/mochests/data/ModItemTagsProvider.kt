package concerrox.mochests.data

import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.data.tags.ItemTagsProvider
import net.minecraft.world.level.block.Block
import net.neoforged.neoforge.common.Tags
import java.util.concurrent.CompletableFuture

class ModItemTagsProvider(
    output: PackOutput,
    lookupProvider: CompletableFuture<HolderLookup.Provider>,
    blockTags: CompletableFuture<TagLookup<Block>>
) : ItemTagsProvider(output, lookupProvider, blockTags) {

    override fun addTags(provider: HolderLookup.Provider) {
        copy(Tags.Blocks.CHESTS, Tags.Items.CHESTS)
    }

}