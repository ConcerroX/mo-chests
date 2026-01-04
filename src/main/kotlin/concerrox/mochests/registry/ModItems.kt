package concerrox.mochests.registry

import concerrox.mochests.MoChests
import concerrox.mochests.content.chest.MoChestBlockItem
import net.minecraft.world.item.Item
import net.neoforged.neoforge.registries.DeferredItem
import net.neoforged.neoforge.registries.DeferredRegister

object ModItems {

    internal val ITEMS = DeferredRegister.createItems(MoChests.MOD_ID)

    val IRON_CHEST: DeferredItem<MoChestBlockItem> = ITEMS.registerItem("iron_chest") {
        MoChestBlockItem(ModBlocks.IRON_CHEST.get(), Item.Properties())
    }
    val GOLD_CHEST: DeferredItem<MoChestBlockItem> = ITEMS.registerItem("gold_chest") {
        MoChestBlockItem(ModBlocks.GOLD_CHEST.get(), Item.Properties())
    }
    val DIAMOND_CHEST: DeferredItem<MoChestBlockItem> = ITEMS.registerItem("diamond_chest") {
        MoChestBlockItem(ModBlocks.DIAMOND_CHEST.get(), Item.Properties())
    }
    val NETHERITE_CHEST: DeferredItem<MoChestBlockItem> = ITEMS.registerItem("netherite_chest") {
        MoChestBlockItem(ModBlocks.NETHERITE_CHEST.get(), Item.Properties().fireResistant())
    }
    val GLASS_CHEST: DeferredItem<MoChestBlockItem> = ITEMS.registerItem("glass_chest") {
        MoChestBlockItem(ModBlocks.GLASS_CHEST.get(), Item.Properties())
    }

}