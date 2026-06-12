package concerrox.mochests.content.chest

import com.lowdragmc.lowdraglib2.gui.ui.UIElement
import com.lowdragmc.lowdraglib2.gui.ui.data.ScrollDisplay
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView
import com.lowdragmc.lowdraglib2.gui.ui.elements.inventory.InventorySlots
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager
import concerrox.mochests.registry.ModMenuTypes
import concerrox.mochests.util.layoutStyle
import concerrox.mochests.util.setContent
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ContainerLevelAccess
import net.minecraft.world.item.ItemStack
import org.appliedenergistics.yoga.YogaFlexDirection
import org.appliedenergistics.yoga.YogaWrap

class MoChestBlockMenu(
    containerId: Int, inventory: Inventory, internal val blockEntityPair: Pair<MoChestBlockEntity, MoChestBlockEntity?>
) : AbstractContainerMenu(ModMenuTypes.CHEST.get(), containerId) {

    constructor(containerId: Int, inventory: Inventory, data: RegistryFriendlyByteBuf) : this(
        containerId, inventory, readBlockEntityData(inventory, data)
    )

    companion object {
        private const val ITEM_SLOT_SIZE = 18f
        internal const val SLOT_COUNT_OF_ONE_ROW = 9

        private fun readBlockEntityData(
            inventory: Inventory, data: RegistryFriendlyByteBuf
        ): Pair<MoChestBlockEntity, MoChestBlockEntity?> {
            val isDouble = data.readBoolean()
            val leftBlockEntity = inventory.player.level().getBlockEntity(data.readBlockPos()) as MoChestBlockEntity
            val rightBlockEntity = if (isDouble) inventory.player.level()
                .getBlockEntity(data.readBlockPos()) as MoChestBlockEntity else null
            return Pair(leftBlockEntity, rightBlockEntity)
        }
    }

    private val leftBlockEntity = blockEntityPair.first
    private val rightBlockEntity = blockEntityPair.second
    private val access = createAccess()

    private fun createAccess(): ContainerLevelAccess {
        val level = leftBlockEntity.level
        return if (level == null || level.isClientSide) ContainerLevelAccess.NULL else ContainerLevelAccess.create(
            level, leftBlockEntity.blockPos
        )
    }

    init {
        val leftItemHandler = leftBlockEntity.itemHandler
        val rightItemHandler = rightBlockEntity?.itemHandler

        leftBlockEntity.startOpen(inventory.player)
        rightBlockEntity?.startOpen(inventory.player)

        val title = if (rightBlockEntity != null) {
            if (rightBlockEntity.hasCustomName()) {
                rightBlockEntity.customName
            } else {
                (leftBlockEntity.blockState.block as MoChestBlock).chestMaterial.translationKeyLarge.toComponent()
            }
        } else {
            leftBlockEntity.displayName
        }

        setContent(inventory, StylesheetManager.MC) {
            UIElement().addClass("panel_bg").layoutStyle {
                paddingAll(7f)
                gapAll(0f)
            }.addChildren(
                Label().layoutStyle { marginBottom(2f) }.apply {
                    if (title != null) setText(title)
                    textStyle { it.textShadow(false).textColor(4210752) }
                },
                ScrollerView().apply {
                    for (i in 0..<leftItemHandler.slots) {
                        addScrollViewChildren(ItemSlot().bind(leftItemHandler, i))
                    }
                    if (rightItemHandler != null) {
                        for (i in 0..<rightItemHandler.slots) {
                            addScrollViewChildren(ItemSlot().bind(rightItemHandler, i))
                        }
                    }
                    scrollerViewStyle.verticalScrollDisplay(ScrollDisplay.ALWAYS)
                    viewPort.layoutStyle { paddingAll(0f) }
                    viewContainer.layoutStyle {
                        // todo: use scroll bar size property
                        maxHeight(ITEM_SLOT_SIZE * 10f)
                        width(ITEM_SLOT_SIZE * SLOT_COUNT_OF_ONE_ROW)
                        flexDirection(YogaFlexDirection.ROW)
                        wrap(YogaWrap.WRAP)
                    }
                    verticalScroller {
                        it.layoutStyle {
                            width(14f)
                            marginLeft(4f)
                        }
                        it.scrollContainer.layoutStyle { paddingAll(1f) }
                        it.scrollBar.layoutStyle { height(8f) }
                    }
                },
                Label().layoutStyle {
                    marginTop(4f)
                    marginBottom(2f)
                }.apply {
                    textStyle { it.textShadow(false).textColor(4210752) }
                    setText("Inventory")
                },
                InventorySlots(),
            )
        }
    }

    override fun quickMoveStack(
        player: Player, index: Int
    ): ItemStack {
        val slot = slots.getOrNull(index) ?: return ItemStack.EMPTY
        if (!slot.hasItem()) return ItemStack.EMPTY

        val stackInSlot = slot.item
        val copyForReturn = stackInSlot.copy()

        val leftSlots = leftBlockEntity.itemHandler.slots
        val rightSlots = rightBlockEntity?.itemHandler?.slots ?: 0
        val containerSlotCount = leftSlots + rightSlots

        val playerInventoryEnd = containerSlotCount + 36

        // 从箱子 玩家背包
        if (index < containerSlotCount) {
            if (!moveItemStackTo(stackInSlot, containerSlotCount, playerInventoryEnd, true)) {
                return ItemStack.EMPTY
            }
        }
        // 从玩家背包 箱子
        else {
            if (!moveItemStackTo(stackInSlot, 0, containerSlotCount, false)) {
                return ItemStack.EMPTY
            }
        }
        if (stackInSlot.isEmpty) {
            slot.set(ItemStack.EMPTY)
        } else {
            slot.setChanged()
        }
        if (stackInSlot.count == copyForReturn.count) {
            return ItemStack.EMPTY
        }
        slot.onTake(player, stackInSlot)
        return copyForReturn
    }

    override fun stillValid(p0: Player): Boolean {
        return stillValid(access, p0, leftBlockEntity.blockState.block)
    }

    override fun removed(player: Player) {
        leftBlockEntity.stopOpen(player)
        rightBlockEntity?.stopOpen(player)
        super.removed(player)
    }

}