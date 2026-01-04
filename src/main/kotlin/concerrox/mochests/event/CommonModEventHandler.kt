package concerrox.mochests.event

import concerrox.mochests.registry.ModItems
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.CreativeModeTabs
import net.minecraft.world.item.Items
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent

object CommonModEventHandler {

    @SubscribeEvent
    fun onBuildCreativeModeTabContents(event: BuildCreativeModeTabContentsEvent) {
        if (event.tabKey == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.insertAfter(
                Items.CHEST.defaultInstance,
                ModItems.GLASS_CHEST.get().defaultInstance,
                CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            )
            event.insertAfter(
                Items.CHEST.defaultInstance,
                ModItems.NETHERITE_CHEST.get().defaultInstance,
                CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            )
            event.insertAfter(
                Items.CHEST.defaultInstance,
                ModItems.DIAMOND_CHEST.get().defaultInstance,
                CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            )
            event.insertAfter(
                Items.CHEST.defaultInstance,
                ModItems.GOLD_CHEST.get().defaultInstance,
                CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            )
            event.insertAfter(
                Items.CHEST.defaultInstance,
                ModItems.IRON_CHEST.get().defaultInstance,
                CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            )
        }
    }

}