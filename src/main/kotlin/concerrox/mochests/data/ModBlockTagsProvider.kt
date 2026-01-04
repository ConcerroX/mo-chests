package concerrox.mochests.data

import concerrox.mochests.registry.ModBlocks
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.BlockTags
import net.neoforged.neoforge.common.Tags
import net.neoforged.neoforge.common.data.BlockTagsProvider
import net.neoforged.neoforge.common.data.ExistingFileHelper
import java.util.concurrent.CompletableFuture

class ModBlockTagsProvider(
    output: PackOutput, lookupProvider: CompletableFuture<HolderLookup.Provider>, existingFileHelper: ExistingFileHelper
) : BlockTagsProvider(output, lookupProvider, ResourceLocation.DEFAULT_NAMESPACE, existingFileHelper) {

    override fun addTags(provider: HolderLookup.Provider) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(
            ModBlocks.IRON_CHEST.get(),
            ModBlocks.GOLD_CHEST.get(),
            ModBlocks.DIAMOND_CHEST.get(),
            ModBlocks.NETHERITE_CHEST.get(),
        )
        tag(Tags.Blocks.CHESTS).add(
            ModBlocks.IRON_CHEST.get(),
            ModBlocks.GOLD_CHEST.get(),
            ModBlocks.DIAMOND_CHEST.get(),
            ModBlocks.NETHERITE_CHEST.get(),
            ModBlocks.GLASS_CHEST.get(),
        )
    }

}