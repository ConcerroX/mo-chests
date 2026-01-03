package concerrox.mochests.util

import com.lowdragmc.lowdraglib2.gui.holder.IModularUIHolderMenu
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI
import com.lowdragmc.lowdraglib2.gui.ui.UI
import com.lowdragmc.lowdraglib2.gui.ui.UIElement
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.AbstractContainerMenu

internal fun AbstractContainerMenu.setContent(
    inventory: Inventory, styleId: ResourceLocation, content: () -> UIElement
) {
    val mui = ModularUI.of(UI.of(content(), StylesheetManager.INSTANCE.getStylesheetSafe(styleId)), inventory.player)
    (this as IModularUIHolderMenu).`ldlib2$setModularUI`(mui)
}