package concerrox.mochests.content.chest

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import concerrox.mochests.MoChestsConfig
import concerrox.mochests.registry.ModBlockEntityTypes
import concerrox.mochests.registry.ModTranslationKeys
import it.unimi.dsi.fastutil.floats.Float2FloatFunction
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.server.level.ServerLevel
import net.minecraft.stats.Stats
import net.minecraft.util.RandomSource
import net.minecraft.util.StringRepresentable
import net.minecraft.world.Containers
import net.minecraft.world.InteractionResult
import net.minecraft.world.MenuProvider
import net.minecraft.world.SimpleContainer
import net.minecraft.world.entity.animal.Cat
import net.minecraft.world.entity.monster.piglin.PiglinAi
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Items
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.block.*
import net.minecraft.world.level.block.DoubleBlockCombiner.NeighborCombineResult
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.entity.LidBlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.*
import net.minecraft.world.level.material.FluidState
import net.minecraft.world.level.material.Fluids
import net.minecraft.world.level.pathfinder.PathComputationType
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape
import net.neoforged.neoforge.common.ModConfigSpec
import java.util.function.BiPredicate
import java.util.function.Consumer

class MoChestBlock(properties: Properties, internal val chestMaterial: ChestMaterial) : BaseEntityBlock(properties),
    SimpleWaterloggedBlock {

    companion object {
        private val CODEC = RecordCodecBuilder.mapCodec<MoChestBlock> { builder ->
            builder.group(
                propertiesCodec(), ChestMaterial.CODEC.fieldOf("chest_material").forGetter { it.chestMaterial })
                .apply(builder, ::MoChestBlock)
        }

        val FACING: DirectionProperty = HorizontalDirectionalBlock.FACING
        val TYPE: EnumProperty<ChestType> = BlockStateProperties.CHEST_TYPE
        val WATERLOGGED: BooleanProperty = BlockStateProperties.WATERLOGGED

        private val NORTH_AABB: VoxelShape = box(1.0, 0.0, 0.0, 15.0, 14.0, 15.0)
        private val SOUTH_AABB: VoxelShape = box(1.0, 0.0, 1.0, 15.0, 14.0, 16.0)
        private val WEST_AABB: VoxelShape = box(0.0, 0.0, 1.0, 15.0, 14.0, 15.0)
        private val EAST_AABB: VoxelShape = box(1.0, 0.0, 1.0, 16.0, 14.0, 15.0)
        private val AABB: VoxelShape = box(1.0, 0.0, 1.0, 15.0, 14.0, 15.0)

        private val MENU_PROVIDER_COMBINER = MoChestMenuProviderCombiner

        fun getBlockType(state: BlockState) = when (state.getValue(TYPE)) {
            ChestType.SINGLE -> DoubleBlockCombiner.BlockType.SINGLE
            ChestType.RIGHT -> DoubleBlockCombiner.BlockType.FIRST
            else -> DoubleBlockCombiner.BlockType.SECOND
        }

        fun getConnectedDirection(state: BlockState): Direction {
            val facing = state.getValue(FACING)
            return if (state.getValue(TYPE) == ChestType.LEFT) facing.clockWise else facing.counterClockWise
        }

        fun opennessCombiner(lid: LidBlockEntity): DoubleBlockCombiner.Combiner<MoChestBlockEntity, Float2FloatFunction> {
            return MoChestOpennessCombiner
        }

        fun isChestBlockedAt(level: LevelAccessor, pos: BlockPos): Boolean {
            return isBlockedChestByBlock(level, pos) || isCatSittingOnChest(level, pos)
        }

        private fun isBlockedChestByBlock(level: BlockGetter, pos: BlockPos): Boolean {
            val above = pos.above()
            return level.getBlockState(above).isRedstoneConductor(level, above)
        }

        private fun isCatSittingOnChest(level: LevelAccessor, pos: BlockPos): Boolean {
            val list = level.getEntitiesOfClass(
                Cat::class.java, AABB(
                    pos.x.toDouble(),
                    (pos.y + 1).toDouble(),
                    pos.z.toDouble(),
                    (pos.x + 1).toDouble(),
                    (pos.y + 2).toDouble(),
                    (pos.z + 1).toDouble()
                )
            )
            if (!list.isEmpty()) {
                for (cat in list) {
                    if (cat.isInSittingPose) {
                        return true
                    }
                }
            }
            return false
        }

    }

    override fun codec(): MapCodec<MoChestBlock> = CODEC
    override fun getRenderShape(state: BlockState) = RenderShape.ENTITYBLOCK_ANIMATED
    override fun newBlockEntity(pos: BlockPos, state: BlockState) = MoChestBlockEntity(pos, state)
    override fun hasAnalogOutputSignal(state: BlockState) = true
    override fun isPathfindable(state: BlockState, pathComputationType: PathComputationType) = false
    private fun getBlockEntityType() = ModBlockEntityTypes.CHEST.get()
    private fun getOpenChestStat() = Stats.CUSTOM.get(Stats.OPEN_CHEST)

    init {
        registerDefaultState(
            stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(TYPE, ChestType.SINGLE)
                .setValue(WATERLOGGED, false)
        )
    }

    override fun tick(state: BlockState, level: ServerLevel, pos: BlockPos, random: RandomSource) {
        val be = level.getBlockEntity(pos)
        if (be is MoChestBlockEntity) {
            be.recheckOpen()
        }
    }

    override fun updateShape(
        state: BlockState,
        facing: Direction,
        facingState: BlockState,
        level: LevelAccessor,
        currentPos: BlockPos,
        facingPos: BlockPos
    ): BlockState {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level))
        }

        if (facingState.`is`(this) && facing.axis.isHorizontal) {
            val chestType = facingState.getValue(TYPE)
            if (chestType != null && state.getValue(TYPE) == ChestType.SINGLE && chestType != ChestType.SINGLE && state.getValue(
                    FACING
                ) == facingState.getValue(FACING) && getConnectedDirection(facingState) == facing.opposite
            ) {
                return state.setValue(TYPE, chestType.opposite)
            }
        } else if (getConnectedDirection(state) == facing) {
            return state.setValue(TYPE, ChestType.SINGLE)
        }

        return super.updateShape(state, facing, facingState, level, currentPos, facingPos)
    }

    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape {
        return if (state.getValue(TYPE) == ChestType.SINGLE) {
            AABB
        } else when (getConnectedDirection(state)) {
            Direction.NORTH -> NORTH_AABB
            Direction.SOUTH -> SOUTH_AABB
            Direction.WEST -> WEST_AABB
            Direction.EAST -> EAST_AABB
            else -> throw IllegalStateException("Invalid chest type at $pos. ")
        }
    }

    override fun getStateForPlacement(context: BlockPlaceContext): BlockState {
        var chestType = ChestType.SINGLE
        var opposite = context.horizontalDirection.opposite
        val fluidState = context.level.getFluidState(context.clickedPos)
        val isShifting = context.isSecondaryUseActive
        val clickedFace = context.clickedFace

        if (chestMaterial == ChestMaterial.GLASS) {
            return defaultBlockState().setValue(FACING, opposite).setValue(TYPE, ChestType.SINGLE)
                .setValue(WATERLOGGED, fluidState.type == Fluids.WATER)
        }

        if (clickedFace.axis.isHorizontal && isShifting) {
            val partnerDire = candidatePartnerFacing(context, clickedFace.opposite)
            if (partnerDire != null && partnerDire.axis != clickedFace.axis) {
                opposite = partnerDire
                chestType = if (partnerDire.counterClockWise == clickedFace.opposite) {
                    ChestType.RIGHT
                } else {
                    ChestType.LEFT
                }
            }
        }

        if (chestType == ChestType.SINGLE && !isShifting) {
            if (opposite == candidatePartnerFacing(context, opposite.clockWise)) {
                chestType = ChestType.LEFT
            } else if (opposite == candidatePartnerFacing(context, opposite.counterClockWise)) {
                chestType = ChestType.RIGHT
            }
        }

        return defaultBlockState().setValue(FACING, opposite).setValue(TYPE, chestType)
            .setValue(WATERLOGGED, fluidState.type == Fluids.WATER)
    }

    override fun getFluidState(state: BlockState): FluidState = if (state.getValue(WATERLOGGED)) {
        Fluids.WATER.getSource(false)
    } else {
        super.getFluidState(state)
    }

    private fun candidatePartnerFacing(context: BlockPlaceContext, direction: Direction): Direction? {
        val partnerState = context.level.getBlockState(context.clickedPos.relative(direction))
        return if (partnerState.`is`(this) && partnerState.getValue(TYPE) == ChestType.SINGLE) {
            partnerState.getValue(FACING)
        } else {
            null
        }
    }

    override fun onRemove(state: BlockState, level: Level, pos: BlockPos, newState: BlockState, isMoving: Boolean) {
        if (!state.`is`(newState.block)) {
            val be = level.getBlockEntity(pos)
            if (be is MoChestBlockEntity) {
                val container = SimpleContainer(be.itemHandler.slots)
                for (i in 0..<be.itemHandler.slots) container.setItem(i, be.itemHandler.getStackInSlot(i))
                Containers.dropContents(level, pos, container)
                level.updateNeighbourForOutputSignal(pos, state.block)
            }
        }
        super.onRemove(state, level, pos, newState, isMoving)
    }

    override fun useWithoutItem(
        state: BlockState, level: Level, pos: BlockPos, player: Player, hitResult: BlockHitResult
    ): InteractionResult {
        if (level.isClientSide) return InteractionResult.SUCCESS

        val menuProviderAndBEDataWriter = getMenuProviderAndBEDataWriter(state, level, pos)
        if (menuProviderAndBEDataWriter != null) {
            player.openMenu(menuProviderAndBEDataWriter.first, menuProviderAndBEDataWriter.second)
            player.awardStat(getOpenChestStat())
            if (chestMaterial != ChestMaterial.GOLD) {
                PiglinAi.angerNearbyPiglins(player, true)
            }
        }
        return InteractionResult.CONSUME
    }

    internal fun getMenuProviderAndBEDataWriter(
        state: BlockState, level: Level, pos: BlockPos
    ): Pair<MenuProvider, Consumer<RegistryFriendlyByteBuf>>? {
        return combine(state, level, pos, false).apply(MENU_PROVIDER_COMBINER)
    }

    override fun getMenuProvider(state: BlockState, level: Level, pos: BlockPos): MenuProvider? {
        return getMenuProviderAndBEDataWriter(state, level, pos)?.first
    }

    fun combine(
        state: BlockState, level: Level, pos: BlockPos, override: Boolean
    ): NeighborCombineResult<out MoChestBlockEntity> {
        val blockedChestTest: BiPredicate<LevelAccessor, BlockPos> = if (override) {
            BiPredicate { _, _ -> false }
        } else {
            BiPredicate { level, pos -> isChestBlockedAt(level, pos) }
        }
        return DoubleBlockCombiner.combineWithNeigbour(
            getBlockEntityType(), ::getBlockType, ::getConnectedDirection, FACING, state, level, pos, blockedChestTest
        )
    }

    override fun <T : BlockEntity> getTicker(
        level: Level, state: BlockState, blockEntityType: BlockEntityType<T>
    ): BlockEntityTicker<T>? {
        if (!level.isClientSide) return null
        return createTickerHelper(blockEntityType, blockEntityType, MoChestBlockEntity::lidAnimateTick)
    }

    override fun rotate(state: BlockState, rotation: Rotation): BlockState {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)))
    }

    @Suppress("DEPRECATION")
    override fun mirror(state: BlockState, mirror: Mirror): BlockState {
        val rotated = state.rotate(mirror.getRotation(state.getValue(FACING)))
        return if (mirror == Mirror.NONE) {
            rotated
        } else {
            rotated.setValue(TYPE, rotated.getValue(TYPE).opposite)
        }
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(FACING, TYPE, WATERLOGGED)
    }

    enum class ChestMaterial(
        val key: String,
        val slotCountConfigValue: ModConfigSpec.IntValue,
        val translationKey: ModTranslationKeys.TranslationKey,
        val translationKeyLarge: ModTranslationKeys.TranslationKey,
        val upgradeIngredient: Ingredient,
        val upgradeAmount: Int
    ) : StringRepresentable {

        IRON(
            "iron",
            MoChestsConfig.ironChestSlots,
            ModTranslationKeys.CONTAINER_IRON_CHEST,
            ModTranslationKeys.CONTAINER_LARGE_IRON_CHEST,
            Ingredient.of(Items.IRON_INGOT),
            4
        ),
        GOLD(
            "gold",
            MoChestsConfig.goldChestSlots,
            ModTranslationKeys.CONTAINER_GOLD_CHEST,
            ModTranslationKeys.CONTAINER_LARGE_GOLD_CHEST,
            Ingredient.of(Items.GOLD_INGOT),
            4
        ),
        DIAMOND(
            "diamond",
            MoChestsConfig.diamondChestSlots,
            ModTranslationKeys.CONTAINER_DIAMOND_CHEST,
            ModTranslationKeys.CONTAINER_LARGE_DIAMOND_CHEST,
            Ingredient.of(Items.DIAMOND),
            4
        ),
        NETHERITE(
            "netherite",
            MoChestsConfig.netheriteChestSlots,
            ModTranslationKeys.CONTAINER_NETHERITE_CHEST,
            ModTranslationKeys.CONTAINER_LARGE_NETHERITE_CHEST,
            Ingredient.of(Items.NETHERITE_INGOT),
            1
        ),
        GLASS(
            "glass",
            MoChestsConfig.glassChestSlots,
            ModTranslationKeys.CONTAINER_GLASS_CHEST,
            ModTranslationKeys.CONTAINER_LARGE_GLASS_CHEST,
            Ingredient.of(Items.GLASS),
            4
        );

        override fun getSerializedName() = ordinal.toString()

        companion object {
            val CODEC: Codec<ChestMaterial> = StringRepresentable.fromEnum { entries.toTypedArray() }
        }

    }

}