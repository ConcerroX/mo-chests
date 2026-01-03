package concerrox.mochests

import concerrox.mochests.content.chest.MoChestBlockMenu
import net.neoforged.neoforge.common.ModConfigSpec

object MoChestsConfig {

    private const val SLOT_COUNT_OF_ONE_ROW = MoChestBlockMenu.SLOT_COUNT_OF_ONE_ROW
    private const val MAX_SLOT_COUNT = SLOT_COUNT_OF_ONE_ROW * 12

    private val BUILDER = ModConfigSpec.Builder().apply {
        group("V") {
            ironChestSlots = defineInRange("ironChestSlots", SLOT_COUNT_OF_ONE_ROW * 5, 0, MAX_SLOT_COUNT)
        }
    }
    val SPEC: ModConfigSpec = BUILDER.build()

    lateinit var ironChestSlots: ModConfigSpec.IntValue

    private fun ModConfigSpec.Builder.group(name: String, block: ModConfigSpec.Builder.() -> Unit) =
        push(name).apply(block).pop()

}