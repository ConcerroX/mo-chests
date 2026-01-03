package concerrox.mochests

import concerrox.mochests.event.ClientModEventHandler
import net.neoforged.api.distmarker.Dist
import net.neoforged.api.distmarker.OnlyIn
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.client.gui.ConfigurationScreen
import net.neoforged.neoforge.client.gui.IConfigScreenFactory

@OnlyIn(Dist.CLIENT)
@Mod(MoChests.MOD_ID, dist = [Dist.CLIENT])
class MoChestsClient(eventBus: IEventBus, modContainer: ModContainer) {

    init {
        eventBus.register(ClientModEventHandler)

        modContainer.registerExtensionPoint(
            IConfigScreenFactory::class.java, IConfigScreenFactory(::ConfigurationScreen)
        )
    }

}