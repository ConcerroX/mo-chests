package concerrox.mochests.content.chest

import concerrox.mochests.MoChestsConfig
import concerrox.mochests.registry.ModBlockEntityTypes
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.HolderLookup
import net.minecraft.core.NonNullList
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.CompoundContainer
import net.minecraft.world.ContainerHelper
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ChestMenu
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.ChestBlock
import net.minecraft.world.level.block.entity.*
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.ChestType
import net.neoforged.neoforge.items.ItemStackHandler

open class MoChestBlockEntity protected constructor(
    type: BlockEntityType<out MoChestBlockEntity>, pos: BlockPos, blockState: BlockState
) : RandomizableContainerBlockEntity(type, pos, blockState), LidBlockEntity {

    val itemStackHandler = object: ItemStackHandler(MoChestsConfig.ironChestSlots.get()) {
        override fun onContentsChanged(slot: Int) {

        }
    }

    private var items: NonNullList<ItemStack?>
    private val openersCounter: ContainerOpenersCounter
    private val chestLidController: ChestLidController

    init {

        this.items = NonNullList.withSize(27, ItemStack.EMPTY)
        this.openersCounter = object : ContainerOpenersCounter() {
            override fun onOpen(p_155357_: Level, p_155358_: BlockPos, p_155359_: BlockState) {
                playSound(p_155357_, p_155358_, p_155359_, SoundEvents.CHEST_OPEN)
            }

            override fun onClose(p_155367_: Level, p_155368_: BlockPos, p_155369_: BlockState) {
                playSound(p_155367_, p_155368_, p_155369_, SoundEvents.CHEST_CLOSE)
            }

            override fun openerCountChanged(
                p_155361_: Level, p_155362_: BlockPos, p_155363_: BlockState, p_155364_: Int, p_155365_: Int
            ) {
                this@MoChestBlockEntity.signalOpenCount(p_155361_, p_155362_, p_155363_, p_155364_, p_155365_)
            }

            override fun isOwnContainer(p_155355_: Player): Boolean {
                if (p_155355_.containerMenu !is ChestMenu) {
                    return false
                } else {
                    val container = (p_155355_.containerMenu as ChestMenu).getContainer()
                    return container === this@MoChestBlockEntity || container is CompoundContainer && container.contains(
                        this@MoChestBlockEntity
                    )
                }
            }
        }
        this.chestLidController = ChestLidController()
    }

    constructor(pos: BlockPos, blockState: BlockState) : this(ModBlockEntityTypes.CHEST.get(), pos, blockState)

    override fun createMenu(containerId: Int, playerInventory: Inventory, player: Player): AbstractContainerMenu {
        return MoChestBlockMenu(containerId, playerInventory, this)
    }

    override fun getContainerSize(): Int {
        return 27
    }

    override fun getDefaultName(): Component {
        return Component.translatable("container.chest")
    }

    override fun loadAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.loadAdditional(tag, registries)
        this.items = NonNullList.withSize<ItemStack?>(this.getContainerSize(), ItemStack.EMPTY)
        if (!this.tryLoadLootTable(tag)) {
            ContainerHelper.loadAllItems(tag, this.items, registries)
        }
    }

    override fun saveAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.saveAdditional(tag, registries)
        if (!this.trySaveLootTable(tag)) {
            ContainerHelper.saveAllItems(tag, this.items, registries)
        }
    }

    override fun triggerEvent(id: Int, type: Int): Boolean {
        if (id == 1) {
            this.chestLidController.shouldBeOpen(type > 0)
            return true
        } else {
            return super.triggerEvent(id, type)
        }
    }

    override fun startOpen(player: Player) {
        if (!this.remove && !player.isSpectator()) {
            this.openersCounter.incrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState())
        }
    }

    override fun stopOpen(player: Player) {
        if (!this.remove && !player.isSpectator()) {
            this.openersCounter.decrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState())
        }
    }

    override fun getItems(): NonNullList<ItemStack?> {
        return this.items
    }

    override fun setItems(items: NonNullList<ItemStack?>) {
        this.items = items
    }

    override fun getOpenNess(partialTicks: Float): Float {
        return this.chestLidController.getOpenness(partialTicks)
    }

    override fun createMenu(id: Int, player: Inventory): AbstractContainerMenu {
        return ChestMenu.threeRows(id, player, this)
    }

    override fun setBlockState(p_155251_: BlockState) {
        val oldState = this.getBlockState()
        super.setBlockState(p_155251_)
        if (oldState.getValue<Direction?>(ChestBlock.FACING) != p_155251_.getValue<Direction?>(ChestBlock.FACING) || oldState.getValue<ChestType?>(
                ChestBlock.TYPE
            ) != p_155251_.getValue<ChestType?>(ChestBlock.TYPE)
        ) {
            this.invalidateCapabilities()
        }
    }

    fun recheckOpen() {
        if (!this.remove) {
            this.openersCounter.recheckOpeners(this.getLevel(), this.getBlockPos(), this.getBlockState())
        }
    }

    protected open fun signalOpenCount(level: Level, pos: BlockPos, state: BlockState, eventId: Int, eventParam: Int) {
        val block = state.getBlock()
        level.blockEvent(pos, block, 1, eventParam)
    }

    companion object {
        private const val EVENT_SET_OPEN_COUNT = 1
        fun lidAnimateTick(level: Level?, pos: BlockPos?, state: BlockState?, blockEntity: MoChestBlockEntity) {
            blockEntity.chestLidController.tickLid()
        }

        fun playSound(level: Level, pos: BlockPos, state: BlockState, sound: SoundEvent) {
            val chesttype = state.getValue<ChestType?>(ChestBlock.TYPE) as ChestType
            if (chesttype != ChestType.LEFT) {
                var d0 = pos.getX().toDouble() + 0.5
                val d1 = pos.getY().toDouble() + 0.5
                var d2 = pos.getZ().toDouble() + 0.5
                if (chesttype == ChestType.RIGHT) {
                    val direction = ChestBlock.getConnectedDirection(state)
                    d0 += direction.getStepX().toDouble() * 0.5
                    d2 += direction.getStepZ().toDouble() * 0.5
                }

                level.playSound(
                    null as Player?, d0, d1, d2, sound, SoundSource.BLOCKS, 0.5f, level.random.nextFloat() * 0.1f + 0.9f
                )
            }
        }

        fun getOpenCount(level: BlockGetter, pos: BlockPos): Int {
            val blockstate = level.getBlockState(pos)
            if (blockstate.hasBlockEntity()) {
                val blockentity = level.getBlockEntity(pos)
                if (blockentity is MoChestBlockEntity) {
                    return blockentity.openersCounter.openerCount
                }
            }

            return 0
        }

        fun swapContents(chest: MoChestBlockEntity, otherChest: MoChestBlockEntity) {
            val nonnulllist = chest.getItems()
            chest.setItems(otherChest.getItems())
            otherChest.setItems(nonnulllist)
        }
    }

}