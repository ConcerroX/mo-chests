package concerrox.mochests.data

import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.data.event.GatherDataEvent

object DataGenerator {

    @SubscribeEvent
    fun onGatherData(event: GatherDataEvent) {
        val generator = event.generator
        val existingFileHelper = event.existingFileHelper
        val packOutput = generator.packOutput
        val registries = event.lookupProvider
        generator.addProvider(event.includeClient(), ModLanguageProvider(packOutput))
        generator.addProvider(event.includeClient(), ModItemModelProvider(packOutput, existingFileHelper))
        generator.addProvider(event.includeClient(), ModBlockStateProvider(packOutput, existingFileHelper))

        val blockGen = ModBlockTagsProvider(packOutput, registries, existingFileHelper)
        generator.addProvider(event.includeServer(), blockGen)
        generator.addProvider(event.includeServer(), ModItemTagsProvider(packOutput, registries, blockGen.contentsGetter()))
        generator.addProvider(event.includeServer(), ModRecipeProvider(packOutput, registries))
        generator.addProvider(event.includeServer(), ModLootTableProvider(packOutput, registries))
    }

}