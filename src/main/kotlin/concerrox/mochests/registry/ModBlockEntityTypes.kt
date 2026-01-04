package concerrox.mochests.registry

import concerrox.mochests.MoChests
import concerrox.mochests.content.chest.MoChestBlockEntity
import concerrox.mochests.util.new
import net.minecraft.core.registries.Registries
import net.minecraft.world.level.block.entity.BlockEntityType
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
object ModBlockEntityTypes {

    internal val BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MoChests.MOD_ID)

    val CHEST: DeferredHolder<BlockEntityType<*>, BlockEntityType<MoChestBlockEntity>> =
        BLOCK_ENTITY_TYPES.new("chest") {
            BlockEntityType.Builder.of(
                ::MoChestBlockEntity,
                ModBlocks.IRON_CHEST.get(),
                ModBlocks.GOLD_CHEST.get(),
                ModBlocks.DIAMOND_CHEST.get(),
                ModBlocks.NETHERITE_CHEST.get(),
                ModBlocks.GLASS_CHEST.get()
            ).build(null)
        }

}