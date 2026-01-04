package concerrox.mochests.content.chest

import concerrox.mochests.registry.ModTranslationKeys
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag

class MoChestBlockItem(block: MoChestBlock, properties: Properties) : BlockItem(block, properties) {

    override fun appendHoverText(
        stack: ItemStack, context: TooltipContext, tooltipComponents: MutableList<Component>, tooltipFlag: TooltipFlag
    ) {
        val mat = (block as MoChestBlock).chestMaterial
        val slots = mat.slotCountConfigValue.get()
        if (mat != MoChestBlock.ChestMaterial.GLASS) {
            tooltipComponents.add(
                Component.translatable("$descriptionId.upgrade").withStyle(ChatFormatting.DARK_GRAY)
            )
        }
        tooltipComponents.add(
            Component.translatable(ModTranslationKeys.DESC_STORAGE_ROWS.key, slots / 9, slots)
                .withStyle(ChatFormatting.GRAY)
        )
        tooltipComponents.add(Component.translatable("$descriptionId.desc").withStyle(ChatFormatting.DARK_GRAY))
    }

}