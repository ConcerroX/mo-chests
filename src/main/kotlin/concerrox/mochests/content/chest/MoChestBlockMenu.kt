package concerrox.mochests.content.chest

import com.lowdragmc.lowdraglib2.gui.holder.IModularUIHolderMenu
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI
import com.lowdragmc.lowdraglib2.gui.ui.UI
import com.lowdragmc.lowdraglib2.gui.ui.UIElement
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label
import com.lowdragmc.lowdraglib2.gui.ui.elements.inventory.InventorySlots
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager
import concerrox.mochests.registry.ModMenuTypes
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.items.ItemStackHandler
import org.appliedenergistics.yoga.YogaFlexDirection
import org.appliedenergistics.yoga.YogaWrap

class MoChestBlockMenu(containerId: Int, inventory: Inventory, leftBlockEntity: MoChestBlockEntity) :
    AbstractContainerMenu(ModMenuTypes.CHEST.get(), containerId) {

    constructor(containerId: Int, inventory: Inventory, data1: RegistryFriendlyByteBuf) : this(
        containerId, inventory, inventory.player.level().getBlockEntity(data1.readBlockPos()) as MoChestBlockEntity
    )

    companion object {
        private const val ITEM_SLOT_SIZE = 18f
        internal const val SLOT_COUNT_OF_ONE_ROW = 9
    }

    val itemHandler = ItemStackHandler(9 * 9)

    init {
//        val leftItemHandler = leftBlockEntity.itemStackHandler

//        setContent(inventory, StylesheetManager.MC) {
        val root = UIElement().addClass("panel_bg").layout {
            it.paddingAll(7f).gapAll(1f)
        }.addChildren(
//                Label().apply {
//                    setText("Iron Chest")
//                    textStyle { it.textShadow(false).textColor(4210752) }
//                    layout { textLayout -> textLayout.marginBottom(1f) }
//                },
//                ScrollerView().apply {
//                    addScrollViewChildren(*rows.toTypedArray())
//                    layout { scrollerViewLayout -> scrollerViewLayout.heightFitContent() }
//                    viewPort { it.layout { vpLayout -> vpLayout.paddingAll(0f) } }
//                    scrollerViewStyle.verticalScrollDisplay(ScrollDisplay.ALWAYS)
//                    viewContainer {
//                        it.layout { vcLayout ->
//                            vcLayout.maxHeight(ITEM_SLOT_SIZE * 10.5f)
//                                .width(ITEM_SLOT_SIZE * MoChestsConfig.SLOT_COUNT_OF_ONE_ROW)
//                                .setFlexDirection(YogaFlexDirection.COLUMN)
//                        }
//                    }
//                    verticalScroller {
//                        it.layout { scrollerLayout -> scrollerLayout.width(14f) }
//                        it.scrollContainer.layout { scrollContainerLayout -> scrollContainerLayout.paddingAll(1f) }
//                        it.scrollBar.layout { scrollBarLayout -> scrollBarLayout.height(8f) }
//                    }
//                },
            UIElement().layout {
                it.width(ITEM_SLOT_SIZE * 9).flexDirection(YogaFlexDirection.ROW).wrap(YogaWrap.WRAP)
            }.selfCall {
                for (i in 0..<itemHandler.slots) it.addChildren(ItemSlot().bind(itemHandler, i))
            },
            Label().apply {
                setText("Inventory")
                textStyle { it.textShadow(false).textColor(4210752) }
                layout { it.marginTop(2f) }
            },
            InventorySlots(),
        )

        val mui = ModularUI.of(
            UI.of(root, StylesheetManager.INSTANCE.getStylesheetSafe(StylesheetManager.MC)),
            inventory.player
        )
        (this as IModularUIHolderMenu).`ldlib2$setModularUI`(mui)
//        }
    }

    override fun quickMoveStack(
        p0: Player, p1: Int
    ): ItemStack {
        return ItemStack.EMPTY
    }

    override fun stillValid(p0: Player): Boolean {
        return true
    }

}