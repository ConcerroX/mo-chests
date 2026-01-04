package concerrox.mochests.event

import concerrox.mochests.content.chest.MoChestBlockEntityRenderer
import concerrox.mochests.content.chest.MoChestBlockItemRenderer
import concerrox.mochests.content.chest.MoChestBlockScreen
import concerrox.mochests.registry.ModBlockEntityTypes
import concerrox.mochests.registry.ModItems
import concerrox.mochests.registry.ModMenuTypes
import net.minecraft.client.gui.screens.MenuScreens
import net.neoforged.api.distmarker.Dist
import net.neoforged.api.distmarker.OnlyIn
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.client.event.EntityRenderersEvent
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent

@OnlyIn(Dist.CLIENT)
object ClientModEventHandler {

    @SubscribeEvent
    fun onRegisterEntityRenderers(event: EntityRenderersEvent.RegisterRenderers) {
        event.registerBlockEntityRenderer(ModBlockEntityTypes.CHEST.get(), ::MoChestBlockEntityRenderer)
    }

    @SubscribeEvent
    fun onRegisterClientExtensions(event: RegisterClientExtensionsEvent) {
        event.registerItem(
            object : IClientItemExtensions {
                override fun getCustomRenderer() = MoChestBlockItemRenderer()
            },
            ModItems.IRON_CHEST,
            ModItems.GOLD_CHEST,
            ModItems.DIAMOND_CHEST,
            ModItems.NETHERITE_CHEST,
            ModItems.GLASS_CHEST
        )
    }

    @SubscribeEvent
    fun onRegisterScreens(event: RegisterMenuScreensEvent) {
        event.register(ModMenuTypes.CHEST.get(), MenuScreens.ScreenConstructor(::MoChestBlockScreen))
    }

}