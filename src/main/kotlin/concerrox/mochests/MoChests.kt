package concerrox.mochests

import concerrox.mochests.data.DataGenerator
import concerrox.mochests.event.CommonGameEventHandler
import concerrox.mochests.event.CommonModEventHandler
import concerrox.mochests.registry.ModBlockEntityTypes
import concerrox.mochests.registry.ModBlocks
import concerrox.mochests.registry.ModItems
import concerrox.mochests.registry.ModMenuTypes
import concerrox.mochests.registry.ModTranslationKeys
import net.minecraft.resources.ResourceLocation
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod
import net.neoforged.fml.config.ModConfig
import net.neoforged.neoforge.common.NeoForge

internal val Minecraft = net.minecraft.client.Minecraft.getInstance()
internal fun id(path: String) = ResourceLocation.fromNamespaceAndPath(MoChests.MOD_ID, path)

@Mod(MoChests.MOD_ID)
class MoChests(eventBus: IEventBus, modContainer: ModContainer) {

    init {
        eventBus.register(DataGenerator)
        eventBus.register(CommonModEventHandler)
        NeoForge.EVENT_BUS.register(CommonGameEventHandler)
        modContainer.registerConfig(ModConfig.Type.COMMON, MoChestsConfig.SPEC)

        ModItems.ITEMS.register(eventBus)
        ModBlocks.BLOCKS.register(eventBus)
        ModMenuTypes.MENU_TYPES.register(eventBus)
        ModBlockEntityTypes.BLOCK_ENTITY_TYPES.register(eventBus)

        ModTranslationKeys.register()
    }

    companion object {
        const val MOD_ID = "mo_chests"
    }

}