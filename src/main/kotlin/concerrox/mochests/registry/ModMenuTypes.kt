package concerrox.mochests.registry

import concerrox.mochests.MoChests
import concerrox.mochests.content.chest.MoChestBlockMenu
import concerrox.mochests.util.new
import net.minecraft.core.registries.Registries
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension
import net.neoforged.neoforge.registries.DeferredRegister

object ModMenuTypes {

    internal val MENU_TYPES = DeferredRegister.create(Registries.MENU, MoChests.MOD_ID)

    val CHEST = MENU_TYPES.new("chest") {
        IMenuTypeExtension.create(::MoChestBlockMenu)
    }

}