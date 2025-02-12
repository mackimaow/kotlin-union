package io.github.mackimaow.kotlin.union

actual sealed interface Union<CS: UCases<CS>>

internal value class UnionWrapper<CS: UCases<CS>>(val value: Any?): Union<CS>

internal actual fun <CS: UCases<CS>> wrapUnion(obj: Any?): Union<CS> {
    return UnionWrapper(obj)
}

internal actual fun unwrapCompletelyIfUnion(obj: Any?): Any? {
    return if (obj is Union<*>)
        unwrapCompletelyIfUnion((obj as UnionWrapper<*>).value)
    else
        obj
}
internal actual fun <CS: UCases<CS>> Union<CS>.unwrapOnce(): Any? {
    return (this as UnionWrapper<*>).value
}
