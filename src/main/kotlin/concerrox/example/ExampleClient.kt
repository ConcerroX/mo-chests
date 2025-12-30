package concerrox.example

import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.client.gui.ConfigurationScreen
import net.neoforged.neoforge.client.gui.IConfigScreenFactory

@Mod(Example.MOD_ID, dist = [Dist.CLIENT])
class ExampleClient(eventBus: IEventBus, modContainer: ModContainer) {

    init {
        modContainer.registerExtensionPoint(
            IConfigScreenFactory::class.java, IConfigScreenFactory(::ConfigurationScreen)
        )
    }

}