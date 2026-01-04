package concerrox.mochests.data

import concerrox.mochests.registry.ModItems
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.data.recipes.*
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.Ingredient
import net.neoforged.neoforge.common.Tags
import java.util.concurrent.CompletableFuture

class ModRecipeProvider(
    output: PackOutput, registries: CompletableFuture<HolderLookup.Provider>
) : RecipeProvider(output, registries) {

    override fun buildRecipes(output: RecipeOutput) {
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModItems.IRON_CHEST).define('C', Tags.Items.CHESTS_WOODEN)
            .define('I', Items.IRON_INGOT).pattern(" I ").pattern("ICI").pattern(" I ")
            .unlockedBy("has_chest", has(Tags.Items.CHESTS_WOODEN)).save(output, "iron_chest_from_chest")

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModItems.GOLD_CHEST).define('C', ModItems.IRON_CHEST)
            .define('G', Items.GOLD_INGOT).pattern(" G ").pattern("GCG").pattern(" G ")
            .unlockedBy("has_iron_chest", has(ModItems.IRON_CHEST)).save(output, "gold_chest_from_iron_chest")

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModItems.DIAMOND_CHEST).define('C', ModItems.GOLD_CHEST)
            .define('D', Items.DIAMOND).pattern(" D ").pattern("DCD").pattern(" D ")
            .unlockedBy("has_gold_chest", has(ModItems.GOLD_CHEST)).save(output, "diamond_chest_from_gold_chest")

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModItems.GLASS_CHEST).define('C', Items.CHEST)
            .define('G', Items.GLASS).pattern(" G ").pattern("GCG").pattern(" G ")
            .unlockedBy("has_chest", has(Items.CHEST)).save(output, "glass_chest_from_chest")

        SmithingTransformRecipeBuilder.smithing(
            Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
            Ingredient.of(ModItems.DIAMOND_CHEST),
            Ingredient.of(Items.NETHERITE_INGOT),
            RecipeCategory.DECORATIONS,
            ModItems.NETHERITE_CHEST.get()
        ).unlocks("has_netherite_ingot", has(Items.NETHERITE_INGOT))
            .save(output, "netherite_alloy_chest_from_diamond_chest")
    }

}