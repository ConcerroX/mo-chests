package concerrox.mochests.registry

import concerrox.mochests.MoChests
import net.minecraft.network.chat.Component

object ModTranslationKeys {

    internal val TRANSLATION_KEYS = mutableSetOf<TranslationKey>()

    val CONTAINER_IRON_CHEST = create("container", "iron_chest", "Iron Chest")
    val CONTAINER_LARGE_IRON_CHEST = create("container", "iron_chest.large", "Large Iron Chest")
    val CONTAINER_GOLD_CHEST = create("container", "gold_chest", "Gold Chest")
    val CONTAINER_LARGE_GOLD_CHEST = create("container", "gold_chest.large", "Large Gold Chest")
    val CONTAINER_DIAMOND_CHEST = create("container", "diamond_chest", "Diamond Chest")
    val CONTAINER_LARGE_DIAMOND_CHEST = create("container", "diamond_chest.large", "Large Diamond Chest")
    val CONTAINER_NETHERITE_CHEST = create("container", "netherite_chest", "Netherite Chest")
    val CONTAINER_LARGE_NETHERITE_CHEST = create("container", "netherite_chest.large", "Large Netherite Chest")
    val CONTAINER_GLASS_CHEST = create("container", "glass_chest", "Glass Chest")
    val CONTAINER_LARGE_GLASS_CHEST = create("container", "glass_chest.large", "Large Glass Chest")

    val DESC_STORAGE_ROWS = create("block", "chest.size", "Storage size: %s rows (%s slots)")
    val DESC_IRON_CHEST = create("block", "iron_chest.desc", "A solid upgrade for your storage.")
    val DESC_GOLD_CHEST = create("block", "gold_chest.desc", "Piglins are not angered when you open this chest.")
    val DESC_DIAMOND_CHEST = create("block", "diamond_chest.desc", "Extreme durability and massive space.")
    val DESC_NETHERITE_CHEST = create("block", "netherite_chest.desc", "Explosion-resistant and immune to fire and lava.")
    val DESC_GLASS_CHEST = create("block", "glass_chest.desc", "Transparent casing lets you see inside. Shift-click to swap your inventory with its contents.")

    val UPGRADE_IRON = create("block", "iron_chest.upgrade", "Shift-click Wooden Chest with 4 Iron Ingots to upgrade.")
    val UPGRADE_GOLD = create("block", "gold_chest.upgrade", "Shift-click Iron Chest with 4 Gold Ingots to upgrade.")
    val UPGRADE_DIAMOND = create("block", "diamond_chest.upgrade", "Shift-click Gold Chest with 4 Diamonds to upgrade.")
    val UPGRADE_NETHERITE = create("block", "netherite_chest.upgrade", "Shift-click Diamond Chest with 1 Netherite Ingot to upgrade.")

    internal fun register() {}

    private fun create(type: String, key: String, defaultValue: String): TranslationKey {
        return TranslationKey("$type.${MoChests.MOD_ID}.$key", defaultValue).also { TRANSLATION_KEYS += it }
    }

    data class TranslationKey(val key: String, val defaultValue: String) {
        fun toComponent(): Component = Component.translatable(key)
    }

}