package concerrox.mochests

import concerrox.mochests.data.DataGenerator
import concerrox.mochests.registry.ModBlockEntityTypes
import concerrox.mochests.registry.ModBlocks
import concerrox.mochests.registry.ModItems
import concerrox.mochests.registry.ModMenuTypes
import net.minecraft.resources.ResourceLocation
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod
import net.neoforged.fml.config.ModConfig

internal val Minecraft = net.minecraft.client.Minecraft.getInstance()
internal fun id(path: String) = ResourceLocation.fromNamespaceAndPath(MoChests.MOD_ID, path)

@Mod(MoChests.MOD_ID)
class MoChests(eventBus: IEventBus, modContainer: ModContainer) {

    init {
        eventBus.register(DataGenerator)
        modContainer.registerConfig(ModConfig.Type.COMMON, MoChestsConfig.SPEC)

        ModItems.ITEMS.register(eventBus)
        ModBlocks.BLOCKS.register(eventBus)
        ModMenuTypes.MENU_TYPES.register(eventBus)
        ModBlockEntityTypes.BLOCK_ENTITY_TYPES.register(eventBus)
    }

    companion object {
        const val MOD_ID = "mo_chests"
    }

}