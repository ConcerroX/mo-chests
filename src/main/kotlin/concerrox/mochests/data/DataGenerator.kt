package concerrox.mochests.data

import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.data.event.GatherDataEvent

object DataGenerator {

    @SubscribeEvent
    fun onGatherData(event: GatherDataEvent) {
        val generator = event.generator
        val existingFileHelper = event.existingFileHelper
        val packOutput = generator.packOutput
        generator.addProvider(event.includeClient(), ModBlockStateProvider(packOutput, existingFileHelper))
    }

}