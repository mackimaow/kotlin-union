package io.github.mackimaow.kotlin.union

import kotlin.reflect.KClass


actual sealed external interface Union<CS: UCases<CS>>

internal actual fun <CS: UCases<CS>> wrapUnion(obj: Any?): Union<CS> {
    @Suppress("UNCHECKED_CAST")
    return obj as Union<CS>
}

internal actual fun unwrapIfUnion(obj: Any?): Any? = obj

internal actual fun isUnionClass(
    classObj : KClass<*>
): Boolean{
    // Cannot check on JS so we return true
    // (Fortunately, when it happens to be true, internal use of this function won't be called because
    // JS doesn't allow use of external functions in reified type parameters, and this is called exactly
    // in that context)
    return false
}
