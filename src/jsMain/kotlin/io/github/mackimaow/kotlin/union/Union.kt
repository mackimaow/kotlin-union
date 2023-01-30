package io.github.mackimaow.kotlin.union


actual sealed external interface Union<L: UnionOptions<L>>


internal actual fun <L: UnionOptions<L>> wrapUnion(obj: Any): Union<L> {
    @Suppress("UNCHECKED_CAST")
    return obj as Union<L>
}

actual fun <L: UnionOptions<L>> unwrapToAny(obj: Union<L>): Any {
    return obj
}
