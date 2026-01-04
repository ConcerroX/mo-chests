package concerrox.mochests.content.chest

import it.unimi.dsi.fastutil.floats.Float2FloatFunction
import net.minecraft.world.level.block.DoubleBlockCombiner
import java.util.Objects
import kotlin.math.max

object MoChestOpennessCombiner: DoubleBlockCombiner.Combiner<MoChestBlockEntity, Float2FloatFunction> {

    override fun acceptDouble(
        blockEntityLeft: MoChestBlockEntity, blockEntityRight: MoChestBlockEntity
    ): Float2FloatFunction {
        return Float2FloatFunction { partialTicks ->
            max(blockEntityLeft.getOpenNess(partialTicks), blockEntityRight.getOpenNess(partialTicks))
        }
    }

    override fun acceptSingle(blockEntity: MoChestBlockEntity): Float2FloatFunction {
        Objects.requireNonNull(blockEntity)
        return Float2FloatFunction { partialTicks -> blockEntity.getOpenNess(partialTicks) }
    }

    override fun acceptNone(): Float2FloatFunction {
        return Float2FloatFunction { _ -> 0f }
    }
}