package concerrox.mochests.event

import concerrox.mochests.content.chest.MoChestBlock
import concerrox.mochests.content.chest.MoChestBlockEntity
import concerrox.mochests.registry.ModBlocks
import net.minecraft.core.BlockPos
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.Container
import net.minecraft.world.InteractionResult
import net.minecraft.world.Nameable
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.ChestBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.ChestType
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.common.Tags
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent

object CommonGameEventHandler {

    @SubscribeEvent
    fun onRightClickBlock(event: PlayerInteractEvent.RightClickBlock) {
        val level = event.level
        val pos = event.pos
        val state = level.getBlockState(pos)
        val player = event.entity
        val stack = event.itemStack
        val isClient = level.isClientSide

        if (!player.isSecondaryUseActive) return

        if (state.block is MoChestBlock && (state.block as MoChestBlock).chestMaterial == MoChestBlock.ChestMaterial.GLASS) {
            event.isCanceled = true
            event.cancellationResult = InteractionResult.sidedSuccess(isClient)
            if (!isClient) performGlassInventorySwap(level, pos, player)
            return
        }

        if (!stack.isEmpty) {
            val isVanilla = state.`is`(Tags.Blocks.CHESTS_WOODEN)
            val currentMat = (state.block as? MoChestBlock)?.chestMaterial

            for (targetMat in MoChestBlock.ChestMaterial.entries) {
                if (targetMat.upgradeIngredient.test(stack)) {
                    val canUpgrade = when {
                        isVanilla -> targetMat == MoChestBlock.ChestMaterial.IRON
                        currentMat != null -> canUpgradeTo(currentMat, targetMat)
                        else -> false
                    }

                    if (canUpgrade) {
                        val isDouble =
                            state.hasProperty(ChestBlock.TYPE) && state.getValue(ChestBlock.TYPE) != ChestType.SINGLE
                        val required = if (isDouble) targetMat.upgradeAmount * 2 else targetMat.upgradeAmount
                        if (stack.count >= required) {
                            event.isCanceled = true
                            event.cancellationResult = InteractionResult.sidedSuccess(isClient)

                            if (!isClient) {
                                val targetBlock = getTargetBlock(targetMat)
                                performUpgradeLogic(level, pos, state, player, stack, required, targetBlock)
                            }
                            return
                        }
                    }
                }
            }
        }
    }

    private fun canUpgradeTo(current: MoChestBlock.ChestMaterial, target: MoChestBlock.ChestMaterial): Boolean {
        return when (target) {
            MoChestBlock.ChestMaterial.GOLD -> current == MoChestBlock.ChestMaterial.IRON
            MoChestBlock.ChestMaterial.DIAMOND -> current == MoChestBlock.ChestMaterial.GOLD
            MoChestBlock.ChestMaterial.NETHERITE -> current == MoChestBlock.ChestMaterial.DIAMOND
            MoChestBlock.ChestMaterial.GLASS -> false
            else -> false
        }
    }

    private fun getTargetBlock(mat: MoChestBlock.ChestMaterial) = when (mat) {
        MoChestBlock.ChestMaterial.IRON -> ModBlocks.IRON_CHEST.get()
        MoChestBlock.ChestMaterial.GOLD -> ModBlocks.GOLD_CHEST.get()
        MoChestBlock.ChestMaterial.DIAMOND -> ModBlocks.DIAMOND_CHEST.get()
        MoChestBlock.ChestMaterial.NETHERITE -> ModBlocks.NETHERITE_CHEST.get()
        MoChestBlock.ChestMaterial.GLASS -> ModBlocks.GLASS_CHEST.get()
    }

    private fun performGlassInventorySwap(level: Level, pos: BlockPos, player: Player) {
        val be = level.getBlockEntity(pos) as? MoChestBlockEntity ?: return
        val stackHandler = be.itemHandler
        val playerInv = player.inventory
        val slotCount = minOf(stackHandler.slots, 9)
        for (i in 0 until slotCount) {
            val playerStack = playerInv.getItem(i).copy()
            val chestStack = stackHandler.getStackInSlot(i).copy()
            playerInv.setItem(i, chestStack)
            stackHandler.setStackInSlot(i, playerStack)
        }
        level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1.0f, 1.2f)
        be.setChanged()
    }

    private fun performUpgradeLogic(
        level: Level, pos: BlockPos, state: BlockState, player: Player, stack: ItemStack, count: Int, targetBlock: Block
    ) {
        if (state.hasProperty(ChestBlock.TYPE) && state.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
            val facing = state.getValue(ChestBlock.FACING)
            val type = state.getValue(ChestBlock.TYPE)
            val partnerDirection = if (type == ChestType.LEFT) facing.clockWise else facing.counterClockWise
            val partnerPos = pos.relative(partnerDirection)
            val partnerState = level.getBlockState(partnerPos)

            upgradeSingleBlock(level, pos, state, targetBlock)
            upgradeSingleBlock(level, partnerPos, partnerState, targetBlock)
        } else {
            upgradeSingleBlock(level, pos, state, targetBlock)
        }

        if (!player.isCreative) stack.shrink(count)
        level.playSound(null, pos, SoundEvents.ARMOR_EQUIP_IRON.value(), SoundSource.BLOCKS, 1f, 1f)
    }

    fun upgradeSingleBlock(level: Level, pos: BlockPos, oldState: BlockState, newBlock: Block) {
        val oldBe = level.getBlockEntity(pos)
        val customName = (oldBe as? Nameable)?.customName
        val items = mutableListOf<ItemStack>()

        if (oldBe is MoChestBlockEntity) {
            for (i in 0 until oldBe.itemHandler.slots) {
                items.add(oldBe.itemHandler.getStackInSlot(i).copy())
                oldBe.itemHandler.setStackInSlot(i, ItemStack.EMPTY)
            }
        } else if (oldBe is Container) {
            for (i in 0 until oldBe.containerSize) {
                items.add(oldBe.getItem(i).copy())
                oldBe.setItem(i, ItemStack.EMPTY)
            }
        }
        level.removeBlockEntity(pos)

        val newState = newBlock.defaultBlockState().setValue(MoChestBlock.FACING, oldState.getValue(ChestBlock.FACING))
            .setValue(MoChestBlock.TYPE, oldState.getValue(ChestBlock.TYPE))
            .setValue(MoChestBlock.WATERLOGGED, oldState.getValue(ChestBlock.WATERLOGGED))
        level.setBlock(pos, newState, 3)

        val newBe = level.getBlockEntity(pos) as? MoChestBlockEntity
        newBe?.let { be ->
            customName?.let { be.customName = it }
            for (i in 0 until items.size) {
                be.itemHandler.setStackInSlot(i, items[i])
            }
            be.setChanged()
        }
    }

}