package concerrox.mochests.util

import net.minecraft.resources.ResourceLocation
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister

fun <T : Any, R : T> DeferredRegister<T>.new(path: String, content: (ResourceLocation) -> R): DeferredHolder<T, R> =
    register(path, content)