package concerrox.mochests.content.chest

import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.world.MenuProvider
import net.minecraft.world.SimpleMenuProvider
import net.minecraft.world.level.block.DoubleBlockCombiner
import java.util.function.Consumer

private typealias ResultOrNull = Pair<MenuProvider, Consumer<RegistryFriendlyByteBuf>>?

object MoChestMenuProviderCombiner : DoubleBlockCombiner.Combiner<MoChestBlockEntity, ResultOrNull> {

    override fun acceptDouble(leftBE: MoChestBlockEntity, rightBE: MoChestBlockEntity): ResultOrNull = Pair(
        SimpleMenuProvider(
            { containerId, inventory, _ -> MoChestBlockMenu(containerId, inventory, leftBE to rightBE) }, leftBE.displayName
        ),
        Consumer {
            it.writeBoolean(true)
            it.writeBlockPos(leftBE.blockPos)
            it.writeBlockPos(rightBE.blockPos)
        },
    )

    override fun acceptSingle(blockEntity: MoChestBlockEntity): ResultOrNull = Pair(
        SimpleMenuProvider({ containerId, inventory, _ ->
            MoChestBlockMenu(containerId, inventory, blockEntity to null)
        }, blockEntity.displayName),
        Consumer {
            it.writeBoolean(false)
            it.writeBlockPos(blockEntity.blockPos)
        },
    )

    override fun acceptNone(): ResultOrNull = null

}