package concerrox.mochests.content.chest

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import com.mojang.math.Axis
import concerrox.mochests.Minecraft
import concerrox.mochests.id
import concerrox.mochests.registry.ModBlocks
import net.minecraft.client.model.geom.ModelLayers
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.Sheets
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.client.renderer.blockentity.BrightnessCombiner
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.client.resources.model.Material
import net.minecraft.core.Direction
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.level.block.ChestBlock
import net.minecraft.world.level.block.DoubleBlockCombiner
import net.minecraft.world.level.block.DoubleBlockCombiner.NeighborCombineResult
import net.minecraft.world.level.block.state.properties.ChestType
import net.minecraft.world.phys.AABB
import net.neoforged.api.distmarker.Dist
import net.neoforged.api.distmarker.OnlyIn
import java.util.*

@OnlyIn(Dist.CLIENT)
class MoChestBlockEntityRenderer<T : MoChestBlockEntity>(context: BlockEntityRendererProvider.Context) :
    BlockEntityRenderer<T> {

    private val lid: ModelPart
    private val bottom: ModelPart
    private val lock: ModelPart
    private val doubleLeftLid: ModelPart
    private val doubleLeftBottom: ModelPart
    private val doubleLeftLock: ModelPart
    private val doubleRightLid: ModelPart
    private val doubleRightBottom: ModelPart
    private val doubleRightLock: ModelPart
    private var xmasTextures = false

    init {
        val calendar = Calendar.getInstance()
        if (calendar.get(2) + 1 == 12 && calendar.get(5) >= 24 && calendar.get(5) <= 26) {
            this.xmasTextures = true
        }

        val modelpart = context.bakeLayer(ModelLayers.CHEST)
        this.bottom = modelpart.getChild("bottom")
        this.lid = modelpart.getChild("lid")
        this.lock = modelpart.getChild("lock")
        val modelpart1 = context.bakeLayer(ModelLayers.DOUBLE_CHEST_LEFT)
        this.doubleLeftBottom = modelpart1.getChild("bottom")
        this.doubleLeftLid = modelpart1.getChild("lid")
        this.doubleLeftLock = modelpart1.getChild("lock")
        val modelpart2 = context.bakeLayer(ModelLayers.DOUBLE_CHEST_RIGHT)
        this.doubleRightBottom = modelpart2.getChild("bottom")
        this.doubleRightLid = modelpart2.getChild("lid")
        this.doubleRightLock = modelpart2.getChild("lock")
    }

    override fun render(
        blockEntity: T,
        partialTick: Float,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        packedLight: Int,
        packedOverlay: Int
    ) {
        val level = blockEntity.getLevel()
        val levelNotNull = level != null
        val state = if (levelNotNull) {
            blockEntity.blockState
        } else {
            ModBlocks.IRON_CHEST.get().defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH)
        }

        val chestType = if (state.hasProperty(ChestBlock.TYPE)) {
            state.getValue(ChestBlock.TYPE)
        } else {
            ChestType.SINGLE
        }

        val block = state.block as MoChestBlock
        val isNotSingle = chestType != ChestType.SINGLE

        poseStack.pushPose()
        val yRot = state.getValue(ChestBlock.FACING).toYRot()
        poseStack.translate(0.5f, 0.5f, 0.5f)
        poseStack.mulPose(Axis.YP.rotationDegrees(-yRot))
        poseStack.translate(-0.5f, -0.5f, -0.5f)

        val neighborCombineResult = if (levelNotNull) {
            block.combine(state, level, blockEntity.blockPos, true)
        } else {
            object : NeighborCombineResult<MoChestBlockEntity> {
                override fun <T : Any?> apply(p0: DoubleBlockCombiner.Combiner<in MoChestBlockEntity, T>): T & Any {
                    return (p0.acceptNone() as T)!!
                }
            }
        }

        var lidAngle = neighborCombineResult.apply(MoChestBlock.opennessCombiner(blockEntity)).get(partialTick)
        lidAngle = 1f - lidAngle
        lidAngle = 1f - lidAngle * lidAngle * lidAngle

        val light = neighborCombineResult.apply(BrightnessCombiner()).applyAsInt(packedLight)
        val material = getMaterial(blockEntity, chestType)
        val vertexConsumer = material.buffer(bufferSource, RenderType::entityCutout)

        if (isNotSingle) {
            if (chestType == ChestType.LEFT) {
                render(
                    poseStack, vertexConsumer, doubleLeftLid, doubleLeftLock, doubleLeftBottom,
                    lidAngle, light, packedOverlay,
                )
            } else {
                render(
                    poseStack, vertexConsumer, doubleRightLid, doubleRightLock, doubleRightBottom,
                    lidAngle, light, packedOverlay,
                )
            }
        } else {
            render(poseStack, vertexConsumer, lid, lock, bottom, lidAngle, light, packedOverlay)
        }

        poseStack.popPose()

        if (block.chestMaterial == MoChestBlock.ChestMaterial.GLASS) {
            renderGlassChestItems(
                blockEntity, poseStack, bufferSource, packedLight
            )
        }
    }

    private fun render(
        poseStack: PoseStack,
        consumer: VertexConsumer,
        lidPart: ModelPart,
        lockPart: ModelPart,
        bottomPart: ModelPart,
        lidAngle: Float,
        packedLight: Int,
        packedOverlay: Int
    ) {
        lidPart.xRot = -(lidAngle * (Math.PI.toFloat() / 2f))
        lockPart.xRot = lidPart.xRot
        lidPart.render(poseStack, consumer, packedLight, packedOverlay)
        lockPart.render(poseStack, consumer, packedLight, packedOverlay)
        bottomPart.render(poseStack, consumer, packedLight, packedOverlay)
    }

    private fun getMaterial(blockEntity: T, chestType: ChestType): Material {
        val state = blockEntity.blockState
        val block = state.block as MoChestBlock
        val material = block.chestMaterial
        return chooseMaterial(material, chestType)
    }

    private fun chooseMaterial(
        material: MoChestBlock.ChestMaterial, chestType: ChestType
    ): Material {
        val base = material.key
        val path = when (chestType) {
            ChestType.SINGLE -> "entity/chest/${base}"
            ChestType.LEFT -> "entity/chest/${base}_left"
            ChestType.RIGHT -> "entity/chest/${base}_right"
        }
        return Material(Sheets.CHEST_SHEET, id(path))
    }

    override fun getRenderBoundingBox(blockEntity: T): AABB {
        val pos = blockEntity.blockPos
        return AABB.encapsulatingFullBlocks(pos.offset(-1, 0, -1), pos.offset(1, 1, 1))
    }

    private fun renderGlassChestItems(
        blockEntity: T, poseStack: PoseStack, bufferSource: MultiBufferSource, packedLight: Int
    ) {
        val level = blockEntity.level ?: return
        val itemRenderer = Minecraft.itemRenderer
        val state = blockEntity.blockState
        val facing = state.getValue(ChestBlock.FACING)

        poseStack.pushPose()
        poseStack.translate(0.5, 0.0, 0.5)
        poseStack.mulPose(Axis.YP.rotationDegrees(facing.toYRot()))
        poseStack.translate(-0.5, 0.0, -0.5)

        val inventory = blockEntity.itemHandler

        for (i in 0 ..<9) {
            val stack = inventory.getStackInSlot(i)
            if (stack.isEmpty) continue

            val row = i / 3
            val col = i % 3

            poseStack.pushPose()
            poseStack.translate(
                0.25 + col * 0.25, 0.25, 0.25 + row * 0.25
            )
            val scale = 0.4f
            poseStack.scale(scale, scale, scale)
            itemRenderer.renderStatic(
                stack,
                ItemDisplayContext.FIXED,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                bufferSource,
                level,
                i
            )
            poseStack.popPose()
        }
        poseStack.popPose()
    }


}