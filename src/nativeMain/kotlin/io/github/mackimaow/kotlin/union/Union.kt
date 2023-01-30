package io.github.mackimaow.kotlin.union


actual sealed interface Union<L: UnionOptions<L>>

private value class UnionWrapper<L: UnionOptions<L>>(val value: Any): Union<L>


internal actual fun <L: UnionOptions<L>> wrapUnion(obj: Any): Union<L> {
    return UnionWrapper(obj)
}

actual fun <L: UnionOptions<L>> unwrapToAny(obj: Union<L>): Any {
    return (obj as UnionWrapper).value
}