package io.github.mackimaow.kotlin.union

import kotlin.reflect.KClass

actual sealed interface Union<CS: UCases<CS>>

@JvmInline
internal value class UnionWrapper<CS: UCases<CS>>(val value: Any?): Union<CS>

internal actual fun <CS: UCases<CS>> wrapUnion(obj: Any?): Union<CS> {
    return UnionWrapper(obj)
}

internal actual fun unwrapIfUnion(obj: Any?): Any? {
    return if (obj is Union<*>)
        unwrapIfUnion((obj as UnionWrapper<*>).value)
    else
        obj
}
internal actual fun isUnionClass(classObj : KClass<*>): Boolean =
    (classObj == Union::class) || (classObj == UnionWrapper::class)
