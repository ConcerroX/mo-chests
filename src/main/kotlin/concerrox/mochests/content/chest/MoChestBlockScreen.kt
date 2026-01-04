package concerrox.mochests.content.chest

import com.lowdragmc.lowdraglib2.gui.holder.IModularUIHolderMenu
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory

class MoChestBlockScreen(menu: MoChestBlockMenu, inventory: Inventory, title: Component) :
    AbstractContainerScreen<MoChestBlockMenu>(menu, inventory, title) {

    override fun renderBg(p0: GuiGraphics, p1: Float, p2: Int, p3: Int) {}
    override fun renderLabels(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {}

    @Suppress("CAST_NEVER_SUCCEEDS")
    override fun init() {
        val mui = (getMenu() as IModularUIHolderMenu).modularUI!!
        imageWidth = mui.width.toInt()
        imageHeight = mui.height.toInt()
        super.init()
    }

}