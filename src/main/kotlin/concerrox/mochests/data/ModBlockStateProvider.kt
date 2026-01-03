package concerrox.mochests.data

import concerrox.mochests.MoChests
import concerrox.mochests.registry.ModBlocks
import net.minecraft.data.PackOutput
import net.neoforged.neoforge.client.model.generators.BlockStateProvider
import net.neoforged.neoforge.common.data.ExistingFileHelper

class ModBlockStateProvider(
    output: PackOutput, existingFileHelper: ExistingFileHelper
) : BlockStateProvider(output, MoChests.MOD_ID, existingFileHelper) {

    override fun registerStatesAndModels() {
//        blockEntityModels(ModelLocationUtils.decorateBlockModelLocation("chest"), Blocks.OAK_PLANKS).createWithoutBlockItem(Blocks.CHEST, Blocks.TRAPPED_CHEST);
    }

//    private fun blockEntityModels(
//        entityBlockModelLocation: ResourceLocation,
//        particleBlock: Block
//    ): BlockEntityModelGenerator {
//        return BlockEntityModelGenerator(entityBlockModelLocation, particleBlock)
//    }

//    inner class BlockEntityModelGenerator(baseModel: ResourceLocation, particleBlock: Block) {
//        private val baseModel: ResourceLocation = ModelTemplates.PARTICLE_ONLY.create(
//            baseModel,
//            TextureMapping.particle(particleBlock),
//            this@ModBlockStateProvider.output
//        )
//
//        fun create(vararg blocks: Block): BlockEntityModelGenerator {
//            for (block in blocks) {
//                this@BlockModelGenerators.blockStateOutput.accept(
//                    BlockModelGenerators.createSimpleBlock(
//                        block,
//                        this.baseModel
//                    )
//                )
//            }
//
//            return this
//        }
//
//        fun createWithoutBlockItem(vararg blocks: Block): BlockEntityModelGenerator {
//            for (block in blocks) {
//                this@BlockModelGenerators.skipAutoItemBlock(block)
//            }
//
//            return this.create(*blocks)
//        }
//
//        fun createWithCustomBlockItemModel(
//            modelTemplate: ModelTemplate,
//            vararg blocks: Block
//        ): BlockEntityModelGenerator {
//            for (block in blocks) {
//                modelTemplate.create(
//                    ModelLocationUtils.getModelLocation(block.asItem()),
//                    TextureMapping.particle(block),
//                    this@BlockModelGenerators.modelOutput
//                )
//            }
//
//            return this.create(*blocks)
//        }
//    }

}