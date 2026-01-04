package concerrox.mochests.content.chest

import com.mojang.blaze3d.vertex.PoseStack
import concerrox.mochests.Minecraft
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.core.BlockPos
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack

class MoChestBlockItemRenderer() : BlockEntityWithoutLevelRenderer(
    Minecraft.blockEntityRenderDispatcher, Minecraft.entityModels
) {

    override fun renderByItem(
        stack: ItemStack,
        displayContext: ItemDisplayContext,
        poseStack: PoseStack,
        buffer: MultiBufferSource,
        packedLight: Int,
        packedOverlay: Int
    ) {
        val block = (stack.item as BlockItem).block
        if (block is MoChestBlock) {
            val be = MoChestBlockEntity(BlockPos.ZERO, block.defaultBlockState())
            Minecraft.blockEntityRenderDispatcher.renderItem(be, poseStack, buffer, packedLight, packedOverlay)
        }
    }

}