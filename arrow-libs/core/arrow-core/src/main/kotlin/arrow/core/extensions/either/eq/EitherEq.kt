package arrow.core.extensions.either.eq

import arrow.core.Either
import arrow.core.Either.Companion
import arrow.core.extensions.EitherEq
import arrow.typeclasses.Eq
import arrow.typeclasses.EqDeprecation
import kotlin.Boolean
import kotlin.Suppress
import kotlin.jvm.JvmName

@JvmName("neqv")
@Suppress(
  "UNCHECKED_CAST",
  "USELESS_CAST",
  "EXTENSION_SHADOWED_BY_MEMBER",
  "UNUSED_PARAMETER"
)
@Deprecated(EqDeprecation, ReplaceWith("this != arg1"))
fun <L, R> Either<L, R>.neqv(
  EQL: Eq<L>,
  EQR: Eq<R>,
  arg1: Either<L, R>
): Boolean = arrow.core.Either.eq<L, R>(EQL, EQR).run {
  this@neqv.neqv(arg1) as kotlin.Boolean
}

@Suppress(
  "UNCHECKED_CAST",
  "NOTHING_TO_INLINE"
)
@Deprecated(EqDeprecation)
inline fun <L, R> Companion.eq(EQL: Eq<L>, EQR: Eq<R>): EitherEq<L, R> = object :
  arrow.core.extensions.EitherEq<L, R> {
  override fun EQL(): arrow.typeclasses.Eq<L> = EQL

  override fun EQR(): arrow.typeclasses.Eq<R> = EQR
}