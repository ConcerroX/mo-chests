package concerrox.mochests.data

import concerrox.mochests.registry.ModBlocks
import net.minecraft.core.HolderLookup
import net.minecraft.core.component.DataComponents
import net.minecraft.data.PackOutput
import net.minecraft.data.loot.BlockLootSubProvider
import net.minecraft.data.loot.LootTableProvider
import net.minecraft.world.flag.FeatureFlags
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.storage.loot.LootPool
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.entries.LootItem
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue
import java.util.concurrent.CompletableFuture


class ModLootTableProvider(
    output: PackOutput, lookupProvider: CompletableFuture<HolderLookup.Provider>
) : LootTableProvider(
    output, setOf(), listOf(SubProviderEntry(::ModBlockLootSubProvider, LootContextParamSets.BLOCK)), lookupProvider
) {

    class ModBlockLootSubProvider(lookupProvider: HolderLookup.Provider) : BlockLootSubProvider(
        setOf(), FeatureFlags.DEFAULT_FLAGS, lookupProvider
    ) {

        override fun generate() {
            add(ModBlocks.IRON_CHEST.get(), ::createNameableBlockEntityTable)
            add(ModBlocks.GOLD_CHEST.get(), ::createNameableBlockEntityTable)
            add(ModBlocks.DIAMOND_CHEST.get(), ::createNameableBlockEntityTable)
            add(ModBlocks.NETHERITE_CHEST.get(), ::createNameableBlockEntityTable)
            add(ModBlocks.GLASS_CHEST.get(), ::createNameableBlockEntityTableWithSilkTouch)
        }

        override fun getKnownBlocks() = ModBlocks.BLOCKS.entries.map { e -> e.value() }

        private fun createNameableBlockEntityTableWithSilkTouch(block: Block): LootTable.Builder {
            return LootTable.lootTable().withPool(
                applyExplosionCondition(
                    block, LootPool.lootPool().setRolls(ConstantValue.exactly(1f)).add(
                        LootItem.lootTableItem(block).apply(
                            CopyComponentsFunction.copyComponents(CopyComponentsFunction.Source.BLOCK_ENTITY)
                                .include(DataComponents.CUSTOM_NAME)
                        )
                    )
                ).`when`(hasSilkTouch())
            )
        }


    }

}