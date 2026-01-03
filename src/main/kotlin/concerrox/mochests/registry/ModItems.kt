package concerrox.mochests.registry

import concerrox.mochests.MoChests
import net.minecraft.world.item.BlockItem
import net.neoforged.neoforge.registries.DeferredItem
import net.neoforged.neoforge.registries.DeferredRegister

object ModItems {

    internal val ITEMS = DeferredRegister.createItems(MoChests.MOD_ID)

    val IRON_CHEST: DeferredItem<BlockItem> = ITEMS.registerSimpleBlockItem(ModBlocks.IRON_CHEST)

}