package io.github.mackimaow.kotlin.union

import kotlin.reflect.KClass


actual sealed external interface Union<CS: UCases<CS>>

internal actual fun <CS: UCases<CS>> wrapUnion(obj: Any?): Union<CS> {
    @Suppress("UNCHECKED_CAST")
    return obj as Union<CS>
}

internal actual fun unwrapCompletelyIfUnion(obj: Any?): Any? = obj
internal actual fun <CS: UCases<CS>> Union<CS>.unwrapOnce(): Any? {
    return this
}
