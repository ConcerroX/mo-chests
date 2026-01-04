package concerrox.mochests.content.chest

import concerrox.mochests.registry.ModBlockEntityTypes
import concerrox.mochests.registry.ModTranslationKeys
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.Nameable
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.ChestBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.ChestLidController
import net.minecraft.world.level.block.entity.ContainerOpenersCounter
import net.minecraft.world.level.block.entity.LidBlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.ChestType
import net.neoforged.neoforge.items.ItemStackHandler

open class MoChestBlockEntity(pos: BlockPos, blockState: BlockState) :
    BlockEntity(ModBlockEntityTypes.CHEST.get(), pos, blockState), Nameable, LidBlockEntity {

    companion object {

        private const val EVENT_SET_OPEN_COUNT = 1
        private const val TAG_KEY = "Items"
        private const val TAG_CUSTOM_NAME = "CustomName"

        fun lidAnimateTick(level: Level, pos: BlockPos, state: BlockState, blockEntity: BlockEntity) {
            if (blockEntity is MoChestBlockEntity) blockEntity.chestLidController.tickLid()
        }

        fun playSound(level: Level, pos: BlockPos, state: BlockState, sound: SoundEvent) {
            val chestType = state.getValue(ChestBlock.TYPE) as ChestType
            if (chestType != ChestType.LEFT) {
                var d0 = pos.x + 0.5
                val d1 = pos.y + 0.5
                var d2 = pos.z + 0.5
                if (chestType == ChestType.RIGHT) {
                    val direction = ChestBlock.getConnectedDirection(state)
                    d0 += direction.stepX * 0.5
                    d2 += direction.stepZ * 0.5
                }
                level.playSound(
                    null, d0, d1, d2, sound, SoundSource.BLOCKS, 0.5f, level.random.nextFloat() * 0.1f + 0.9f
                )
            }
        }

        fun getOpenCount(level: BlockGetter, pos: BlockPos): Int {
            val state = level.getBlockState(pos)
            if (state.hasBlockEntity()) {
                val be = level.getBlockEntity(pos)
                if (be is MoChestBlockEntity) {
                    return be.openersCounter.openerCount
                }
            }
            return 0
        }

    }

    private val block = blockState.block as MoChestBlock
    private val chestLidController = ChestLidController()
    internal var customName: Component? = null

    val itemHandler = object : ItemStackHandler(block.chestMaterial.slotCountConfigValue.get()) {
        override fun onContentsChanged(slot: Int) {
            setChanged()
            level?.let {
                it.sendBlockUpdated(pos, it.getBlockState(pos), it.getBlockState(pos), 3)
            }
        }
    }

    private val openersCounter = object : ContainerOpenersCounter() {

        override fun onOpen(level: Level, pos: BlockPos, state: BlockState) {
            playSound(level, pos, state, SoundEvents.CHEST_OPEN)
        }

        override fun onClose(level: Level, pos: BlockPos, state: BlockState) {
            playSound(level, pos, state, SoundEvents.CHEST_CLOSE)
        }

        override fun openerCountChanged(level: Level, pos: BlockPos, state: BlockState, eventId: Int, eventParam: Int) {
            this@MoChestBlockEntity.signalOpenCount(level, pos, state, eventId, eventParam)
        }

        override fun isOwnContainer(player: Player): Boolean {
            if (player.containerMenu !is MoChestBlockMenu) return false
            val pair = (player.containerMenu as MoChestBlockMenu).blockEntityPair
            return pair.first == this@MoChestBlockEntity || pair.second == this@MoChestBlockEntity
        }

    }

    override fun getDisplayName() = name
    override fun getName() = customName ?: block.chestMaterial.translationKey.toComponent()
    override fun getCustomName() = customName
    override fun getOpenNess(partialTicks: Float) = chestLidController.getOpenness(partialTicks)

    override fun loadAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.loadAdditional(tag, registries)
        itemHandler.deserializeNBT(registries, tag.getCompound(TAG_KEY))
        if (tag.contains(TAG_CUSTOM_NAME, 8)) {
            customName = parseCustomNameSafe(tag.getString(TAG_CUSTOM_NAME), registries)
        }
    }

    override fun saveAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.saveAdditional(tag, registries)
        tag.put(TAG_KEY, itemHandler.serializeNBT(registries))
        customName?.let {
            tag.putString(TAG_CUSTOM_NAME, Component.Serializer.toJson(it, registries))
        }
    }

    override fun getUpdatePacket(): Packet<ClientGamePacketListener> {
        return ClientboundBlockEntityDataPacket.create(this)
    }

    override fun getUpdateTag(registries: HolderLookup.Provider): CompoundTag {
        return saveCustomOnly(registries)
    }

    override fun triggerEvent(id: Int, type: Int) = if (id == EVENT_SET_OPEN_COUNT) {
        chestLidController.shouldBeOpen(type > 0)
        true
    } else {
        super.triggerEvent(id, type)
    }

    internal fun startOpen(player: Player) {
        val level = level
        if (!remove && !player.isSpectator && level != null) {
            openersCounter.incrementOpeners(player, level, blockPos, blockState)
        }
    }

    internal fun stopOpen(player: Player) {
        val level = level
        if (!remove && !player.isSpectator && level != null) {
            openersCounter.decrementOpeners(player, level, blockPos, blockState)
        }
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun setBlockState(newState: BlockState) {
        val oldState = blockState
        super.setBlockState(newState)
        if (oldState.getValue(ChestBlock.FACING) != newState.getValue(ChestBlock.FACING) || oldState.getValue(
                ChestBlock.TYPE
            ) != newState.getValue(ChestBlock.TYPE)
        ) {
            invalidateCapabilities()
        }
    }

    fun recheckOpen() {
        val level = level
        if (!remove && level != null) openersCounter.recheckOpeners(level, blockPos, blockState)
    }

    protected open fun signalOpenCount(level: Level, pos: BlockPos, state: BlockState, eventId: Int, eventParam: Int) {
        val block = state.block
        level.blockEvent(pos, block, EVENT_SET_OPEN_COUNT, eventParam)
    }

}