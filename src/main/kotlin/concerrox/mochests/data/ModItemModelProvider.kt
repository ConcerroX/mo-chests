package concerrox.mochests.data

import concerrox.mochests.MoChests
import concerrox.mochests.registry.ModItems
import net.minecraft.data.PackOutput
import net.neoforged.neoforge.client.model.generators.ItemModelProvider
import net.neoforged.neoforge.common.data.ExistingFileHelper

class ModItemModelProvider(
    output: PackOutput, existingFileHelper: ExistingFileHelper
) : ItemModelProvider(output, MoChests.MOD_ID, existingFileHelper) {

    override fun registerModels() {
        withExistingParent(ModItems.IRON_CHEST.id.toString(), mcLoc("item/chest")).texture("particle", mcLoc("block/iron_block"))
        withExistingParent(ModItems.GOLD_CHEST.id.toString(), mcLoc("item/chest")).texture("particle", mcLoc("block/gold_block"))
        withExistingParent(ModItems.DIAMOND_CHEST.id.toString(), mcLoc("item/chest")).texture("particle", mcLoc("block/diamond_block"))
        withExistingParent(ModItems.NETHERITE_CHEST.id.toString(), mcLoc("item/chest")).texture("particle", mcLoc("block/netherite_block"))
        withExistingParent(ModItems.GLASS_CHEST.id.toString(), mcLoc("item/chest")).texture("particle", mcLoc("block/glass"))
    }

}