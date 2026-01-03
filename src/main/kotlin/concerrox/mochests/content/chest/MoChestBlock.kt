package concerrox.mochests.content.chest

import com.mojang.serialization.MapCodec
import concerrox.mochests.registry.ModBlockEntityTypes
import it.unimi.dsi.fastutil.floats.Float2FloatFunction
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.*
import net.minecraft.world.entity.animal.Cat
import net.minecraft.world.entity.monster.piglin.PiglinAi
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ChestMenu
import net.minecraft.world.item.context.BlockPlaceContext
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
import java.util.*
import java.util.function.BiPredicate
import java.util.function.Supplier
import kotlin.math.max

class MoChestBlock(
    properties: Properties, private val blockEntityType: Supplier<BlockEntityType<out MoChestBlockEntity>>
) : BaseEntityBlock(properties), SimpleWaterloggedBlock {

    public override fun codec(): MapCodec<MoChestBlock> {
        return CODEC
    }

    override fun getRenderShape(state: BlockState) = RenderShape.ENTITYBLOCK_ANIMATED

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
            else -> NORTH_AABB
        }
    }

    override fun getStateForPlacement(context: BlockPlaceContext): BlockState {
        var chestType = ChestType.SINGLE
        var direction = context.horizontalDirection.opposite
        val fluidState = context.level.getFluidState(context.clickedPos)
        val isShifting = context.isSecondaryUseActive
        val clickedDirection = context.clickedFace
        if (clickedDirection.axis.isHorizontal && isShifting) {
            val partnerDirection = candidatePartnerFacing(context, clickedDirection.opposite)
            if (partnerDirection != null && partnerDirection.axis != clickedDirection.axis) {
                direction = partnerDirection
                chestType = if (partnerDirection.counterClockWise == clickedDirection.opposite) {
                    ChestType.RIGHT
                } else {
                    ChestType.LEFT
                }
            }
        }

        if (chestType == ChestType.SINGLE && !isShifting) {
            if (direction == candidatePartnerFacing(context, direction.clockWise)) {
                chestType = ChestType.LEFT
            } else if (direction == candidatePartnerFacing(context, direction.counterClockWise)) {
                chestType = ChestType.RIGHT
            }
        }

        return defaultBlockState().setValue(FACING, direction).setValue(TYPE, chestType)
            .setValue(WATERLOGGED, fluidState.type == Fluids.WATER)
    }

    override fun getFluidState(state: BlockState): FluidState {
        return if (state.getValue(WATERLOGGED)) {
            Fluids.WATER.getSource(false)
        } else {
            super.getFluidState(state)
        }
    }

    private fun candidatePartnerFacing(context: BlockPlaceContext, direction: Direction): Direction? {
        val state = context.level.getBlockState(context.clickedPos.relative(direction))
        return if (state.`is`(this) && state.getValue(TYPE) == ChestType.SINGLE) {
            state.getValue(FACING)
        } else {
            null
        }
    }

    // TODO: Container on remove
//    override fun onRemove(state: BlockState, level: Level, pos: BlockPos, newState: BlockState, isMoving: Boolean) {
//        Containers.dropContentsOnDestroy(state, newState, level, pos)
//        super.onRemove(state, level, pos, newState, isMoving)
//    }

    override fun useWithoutItem(
        state: BlockState, level: Level, pos: BlockPos, player: Player, hitResult: BlockHitResult
    ): InteractionResult {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS
        } else {
            val menuProvider = getMenuProvider(state, level, pos)
            if (menuProvider != null) {
                player.openMenu(
                    SimpleMenuProvider(
                        level.getBlockEntity(pos) as MoChestBlockEntity,
                        Component.translatable("menu.title.mo_chests.mo_chest")
                    ), {
                        it.writeBlockPos(pos)
                    })
//                player.openMenu(menuProvider)
//                player.awardStat(this.openChestStat)
                PiglinAi.angerNearbyPiglins(player, true)
            }
            return InteractionResult.CONSUME
        }
    }

    // TODO: open chest stat
//    protected open val openChestStat: Stat<ResourceLocation?>
//        get() = Stats.CUSTOM.get(Stats.OPEN_CHEST)

    fun blockEntityType(): BlockEntityType<out MoChestBlockEntity> {
        return blockEntityType.get()
    }

    fun combine(
        state: BlockState, level: Level, pos: BlockPos, override: Boolean
    ): NeighborCombineResult<out MoChestBlockEntity> {
        val biPredicate: BiPredicate<LevelAccessor, BlockPos> = if (override) {
            BiPredicate { _, _ -> false }
        } else {
            BiPredicate { level, pos -> isChestBlockedAt(level, pos) }
        }

        return DoubleBlockCombiner.combineWithNeigbour(
            blockEntityType.get(), ::getBlockType, ::getConnectedDirection, FACING, state, level, pos, biPredicate
        )
    }

    override fun getMenuProvider(state: BlockState, level: Level, pos: BlockPos): MenuProvider? {
        return combine(state, level, pos, false).apply(MENU_PROVIDER_COMBINER).orElse(null)
    }

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return MoChestBlockEntity(pos, state)
    }

    override fun <T : BlockEntity> getTicker(
        level: Level, state: BlockState, blockEntityType: BlockEntityType<T>
    ): BlockEntityTicker<T>? {
        return if (level.isClientSide) {
            createTickerHelper(blockEntityType, blockEntityType(), MoChestBlockEntity::lidAnimateTick)
        } else {
            null
        }
    }

    override fun hasAnalogOutputSignal(state: BlockState): Boolean {
        return true
    }

    override fun getAnalogOutputSignal(blockState: BlockState, level: Level, pos: BlockPos): Int {
        return AbstractContainerMenu.getRedstoneSignalFromContainer(getContainer(this, blockState, level, pos, false))
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

    override fun isPathfindable(state: BlockState, pathComputationType: PathComputationType): Boolean {
        return false
    }

    override fun tick(state: BlockState, level: ServerLevel, pos: BlockPos, random: RandomSource) {
        val be = level.getBlockEntity(pos)
        if (be is MoChestBlockEntity) be.recheckOpen()
    }

    init {
        registerDefaultState(
            stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(TYPE, ChestType.SINGLE)
                .setValue(WATERLOGGED, false)
        )
    }

    companion object {

        val CODEC: MapCodec<MoChestBlock> = simpleCodec {
            MoChestBlock(it) { ModBlockEntityTypes.CHEST.get() }
        }
        val FACING: DirectionProperty = HorizontalDirectionalBlock.FACING
        val TYPE: EnumProperty<ChestType> = BlockStateProperties.CHEST_TYPE
        val WATERLOGGED: BooleanProperty = BlockStateProperties.WATERLOGGED

        const val EVENT_SET_OPEN_COUNT: Int = 1
        protected const val AABB_OFFSET: Int = 1
        protected const val AABB_HEIGHT: Int = 14

        protected val NORTH_AABB: VoxelShape = box(1.0, 0.0, 0.0, 15.0, 14.0, 15.0)
        protected val SOUTH_AABB: VoxelShape = box(1.0, 0.0, 1.0, 15.0, 14.0, 16.0)
        protected val WEST_AABB: VoxelShape = box(0.0, 0.0, 1.0, 15.0, 14.0, 15.0)
        protected val EAST_AABB: VoxelShape = box(1.0, 0.0, 1.0, 16.0, 14.0, 15.0)
        protected val AABB: VoxelShape = box(1.0, 0.0, 1.0, 15.0, 14.0, 15.0)

        private val CHEST_COMBINER: DoubleBlockCombiner.Combiner<MoChestBlockEntity, Optional<Container>> =
            object : DoubleBlockCombiner.Combiner<MoChestBlockEntity, Optional<Container>> {

                override fun acceptDouble(
                    blockEntity: MoChestBlockEntity, entity: MoChestBlockEntity
                ): Optional<Container> {
                    return Optional.of(CompoundContainer(blockEntity, entity))
                }

                override fun acceptSingle(p_51589_: MoChestBlockEntity): Optional<Container> {
                    return Optional.of(p_51589_)
                }

                override fun acceptNone(): Optional<Container> {
                    return Optional.empty()
                }

            }

        private val MENU_PROVIDER_COMBINER: DoubleBlockCombiner.Combiner<MoChestBlockEntity, Optional<MenuProvider>> =
            object : DoubleBlockCombiner.Combiner<MoChestBlockEntity, Optional<MenuProvider>> {

                override fun acceptDouble(
                    p_51604_: MoChestBlockEntity, p_51605_: MoChestBlockEntity
                ): Optional<MenuProvider> {
                    val container: Container = CompoundContainer(p_51604_, p_51605_)
                    return Optional.of(object : MenuProvider {
                        override fun createMenu(
                            p_51622_: Int, p_51623_: Inventory, p_51624_: Player
                        ): AbstractContainerMenu? {
                            if (p_51604_.canOpen(p_51624_) && p_51605_.canOpen(p_51624_)) {
                                p_51604_.unpackLootTable(p_51623_.player)
                                p_51605_.unpackLootTable(p_51623_.player)
                                return ChestMenu.sixRows(p_51622_, p_51623_, container)
                            } else {
                                return null
                            }
                        }

                        override fun getDisplayName(): Component {
                            if (p_51604_.hasCustomName()) {
                                return p_51604_.displayName
                            } else {
                                return (if (p_51605_.hasCustomName()) p_51605_.displayName else Component.translatable(
                                    "container.chestDouble"
                                )) as Component
                            }
                        }
                    })
                }

                override fun acceptSingle(p_51602_: MoChestBlockEntity): Optional<MenuProvider> {
                    return Optional.of(p_51602_)
                }

                override fun acceptNone(): Optional<MenuProvider> {
                    return Optional.empty()
                }
            }

        fun getBlockType(state: BlockState): DoubleBlockCombiner.BlockType {
            val chestType = state.getValue(TYPE)
            return when (chestType) {
                ChestType.SINGLE -> DoubleBlockCombiner.BlockType.SINGLE
                ChestType.RIGHT -> DoubleBlockCombiner.BlockType.FIRST
                else -> DoubleBlockCombiner.BlockType.SECOND
            }
        }

        fun getConnectedDirection(state: BlockState): Direction {
            val direction = state.getValue(FACING)
            return if (state.getValue(TYPE) == ChestType.LEFT) direction.clockWise else direction.counterClockWise
        }

        fun getContainer(
            chest: MoChestBlock, state: BlockState, level: Level, pos: BlockPos, override: Boolean
        ): Container? {
            return chest.combine(state, level, pos, override).apply(CHEST_COMBINER).orElse(null)
        }

        fun opennessCombiner(lid: LidBlockEntity): DoubleBlockCombiner.Combiner<MoChestBlockEntity, Float2FloatFunction> {
            return object : DoubleBlockCombiner.Combiner<MoChestBlockEntity, Float2FloatFunction> {
                override fun acceptDouble(
                    p_51633_: MoChestBlockEntity, p_51634_: MoChestBlockEntity
                ): Float2FloatFunction {
                    return Float2FloatFunction { p_51638_: Float ->
                        max(
                            p_51633_.getOpenNess(p_51638_), p_51634_.getOpenNess(p_51638_)
                        )
                    }
                }

                override fun acceptSingle(p_51631_: MoChestBlockEntity): Float2FloatFunction {
                    Objects.requireNonNull(p_51631_)
                    return Float2FloatFunction { partialTicks -> p_51631_.getOpenNess(partialTicks) }
                }

                override fun acceptNone(): Float2FloatFunction {
                    return Float2FloatFunction { v -> lid.getOpenNess(v) }
                }
            }
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

}