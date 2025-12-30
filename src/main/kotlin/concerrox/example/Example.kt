package concerrox.example

import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod

@Mod(Example.MOD_ID)
class Example(eventBus: IEventBus, modContainer: ModContainer) {

    companion object {
        const val MOD_ID = "example"
    }

}