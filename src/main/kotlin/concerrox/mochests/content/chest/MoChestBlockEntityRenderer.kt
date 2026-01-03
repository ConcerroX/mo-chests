package concerrox.mochests.content.chest

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import com.mojang.math.Axis
import concerrox.mochests.id
import concerrox.mochests.registry.ModBlocks
import it.unimi.dsi.fastutil.ints.Int2IntFunction
import net.minecraft.client.model.geom.ModelLayers
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.client.model.geom.PartPose
import net.minecraft.client.model.geom.builders.CubeListBuilder
import net.minecraft.client.model.geom.builders.LayerDefinition
import net.minecraft.client.model.geom.builders.MeshDefinition
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.Sheets
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.client.renderer.blockentity.BrightnessCombiner
import net.minecraft.client.resources.model.Material
import net.minecraft.core.Direction
import net.minecraft.world.level.block.ChestBlock
import net.minecraft.world.level.block.DoubleBlockCombiner
import net.minecraft.world.level.block.DoubleBlockCombiner.NeighborCombineResult
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
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
        val flag = level != null
        val blockstate = if (flag) blockEntity.blockState else ModBlocks.IRON_CHEST.get().defaultBlockState()
            .setValue(ChestBlock.FACING, Direction.SOUTH) as BlockState
        val chestType =
            if (blockstate.hasProperty(ChestBlock.TYPE)) blockstate.getValue(ChestBlock.TYPE) else ChestType.SINGLE
        val var12 = blockstate.block
        if (var12 is MoChestBlock) {
            val isNotSingle = chestType != ChestType.SINGLE
            poseStack.pushPose()
            val f = (blockstate.getValue(ChestBlock.FACING) as Direction).toYRot()
            poseStack.translate(0.5f, 0.5f, 0.5f)
            poseStack.mulPose(Axis.YP.rotationDegrees(-f))
            poseStack.translate(-0.5f, -0.5f, -0.5f)
            val neighborCombineResult: NeighborCombineResult<out MoChestBlockEntity> = if (flag) {
                var12.combine(blockstate, level, blockEntity.blockPos, true)
            } else {
                object : NeighborCombineResult<MoChestBlockEntity> {
                    override fun <T : Any?> apply(p0: DoubleBlockCombiner.Combiner<in MoChestBlockEntity, T>): T & Any {
                        return (p0.acceptNone() as T)!!
                    }
                }
            }

            var f1 = (neighborCombineResult.apply(MoChestBlock.opennessCombiner(blockEntity))).get(partialTick)
            f1 = 1.0f - f1
            f1 = 1.0f - f1 * f1 * f1
            val i = (neighborCombineResult.apply(BrightnessCombiner()) as Int2IntFunction).applyAsInt(
                packedLight
            )
            val material = getMaterial(blockEntity, chestType)
            val vertexConsumer = material.buffer(bufferSource, RenderType::entityCutout)
            if (isNotSingle) {
                if (chestType == ChestType.LEFT) {
                    render(
                        poseStack,
                        vertexConsumer,
                        this.doubleLeftLid,
                        this.doubleLeftLock,
                        this.doubleLeftBottom,
                        f1,
                        i,
                        packedOverlay
                    )
                } else {
                    render(
                        poseStack,
                        vertexConsumer,
                        this.doubleRightLid,
                        this.doubleRightLock,
                        this.doubleRightBottom,
                        f1,
                        i,
                        packedOverlay
                    )
                }
            } else {
                render(poseStack, vertexConsumer, this.lid, this.lock, this.bottom, f1, i, packedOverlay)
            }

            poseStack.popPose()
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

    protected fun getMaterial(blockEntity: T, chestType: ChestType): Material {
        return chooseMaterial(blockEntity, chestType, xmasTextures)
    }

    fun chooseMaterial(blockEntity: BlockEntity, chestType: ChestType, holiday: Boolean): Material {
        return chooseMaterial(
            chestType, CHEST_LOCATION, Sheets.CHEST_LOCATION_LEFT, Sheets.CHEST_LOCATION_RIGHT
        )
    }

    private val CHEST_LOCATION = chestMaterial("iron_normal")

    private fun chestMaterial(chestName: String): Material {
        return Material(Sheets.CHEST_SHEET, id("entity/chest/$chestName"))
    }

    private fun chooseMaterial(
        chestType: ChestType,
        singleMaterial: Material,
        leftMaterial: Material,
        rightMaterial: Material
    ): Material {
        return when (chestType) {
            ChestType.LEFT -> leftMaterial
            ChestType.RIGHT -> rightMaterial
            ChestType.SINGLE -> singleMaterial
        }
    }

    override fun getRenderBoundingBox(blockEntity: T): AABB {
        val pos = blockEntity.blockPos
        return AABB.encapsulatingFullBlocks(pos.offset(-1, 0, -1), pos.offset(1, 1, 1))
    }

    companion object {
        private const val BOTTOM = "bottom"
        private const val LID = "lid"
        private const val LOCK = "lock"
        fun createSingleBodyLayer(): LayerDefinition {
            val meshdefinition = MeshDefinition()
            val partdefinition = meshdefinition.getRoot()
            partdefinition.addOrReplaceChild(
                "bottom",
                CubeListBuilder.create().texOffs(0, 19).addBox(1.0f, 0.0f, 1.0f, 14.0f, 10.0f, 14.0f),
                PartPose.ZERO
            )
            partdefinition.addOrReplaceChild(
                "lid",
                CubeListBuilder.create().texOffs(0, 0).addBox(1.0f, 0.0f, 0.0f, 14.0f, 5.0f, 14.0f),
                PartPose.offset(0.0f, 9.0f, 1.0f)
            )
            partdefinition.addOrReplaceChild(
                "lock",
                CubeListBuilder.create().texOffs(0, 0).addBox(7.0f, -2.0f, 14.0f, 2.0f, 4.0f, 1.0f),
                PartPose.offset(0.0f, 9.0f, 1.0f)
            )
            return LayerDefinition.create(meshdefinition, 64, 64)
        }

        fun createDoubleBodyRightLayer(): LayerDefinition {
            val meshdefinition = MeshDefinition()
            val partdefinition = meshdefinition.getRoot()
            partdefinition.addOrReplaceChild(
                "bottom",
                CubeListBuilder.create().texOffs(0, 19).addBox(1.0f, 0.0f, 1.0f, 15.0f, 10.0f, 14.0f),
                PartPose.ZERO
            )
            partdefinition.addOrReplaceChild(
                "lid",
                CubeListBuilder.create().texOffs(0, 0).addBox(1.0f, 0.0f, 0.0f, 15.0f, 5.0f, 14.0f),
                PartPose.offset(0.0f, 9.0f, 1.0f)
            )
            partdefinition.addOrReplaceChild(
                "lock",
                CubeListBuilder.create().texOffs(0, 0).addBox(15.0f, -2.0f, 14.0f, 1.0f, 4.0f, 1.0f),
                PartPose.offset(0.0f, 9.0f, 1.0f)
            )
            return LayerDefinition.create(meshdefinition, 64, 64)
        }

        fun createDoubleBodyLeftLayer(): LayerDefinition {
            val meshdefinition = MeshDefinition()
            val partdefinition = meshdefinition.getRoot()
            partdefinition.addOrReplaceChild(
                "bottom",
                CubeListBuilder.create().texOffs(0, 19).addBox(0.0f, 0.0f, 1.0f, 15.0f, 10.0f, 14.0f),
                PartPose.ZERO
            )
            partdefinition.addOrReplaceChild(
                "lid",
                CubeListBuilder.create().texOffs(0, 0).addBox(0.0f, 0.0f, 0.0f, 15.0f, 5.0f, 14.0f),
                PartPose.offset(0.0f, 9.0f, 1.0f)
            )
            partdefinition.addOrReplaceChild(
                "lock",
                CubeListBuilder.create().texOffs(0, 0).addBox(0.0f, -2.0f, 14.0f, 1.0f, 4.0f, 1.0f),
                PartPose.offset(0.0f, 9.0f, 1.0f)
            )
            return LayerDefinition.create(meshdefinition, 64, 64)
        }
    }
}