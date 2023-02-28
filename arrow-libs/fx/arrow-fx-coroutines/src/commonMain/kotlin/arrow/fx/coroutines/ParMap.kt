package arrow.fx.coroutines

import arrow.core.AccumulatingRaise
import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.flattenOrAccumulate
import arrow.core.raise.Raise
import arrow.core.raise.either
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

public suspend fun <A, B> Iterable<A>.parMap(
  ctx: CoroutineContext = EmptyCoroutineContext,
  concurrency: Int,
  f: suspend CoroutineScope.(A) -> B
): List<B> {
  val semaphore = Semaphore(concurrency)
  return parMap(ctx) {
    semaphore.withPermit { f(it) }
  }
}

public suspend fun <A, B> Iterable<A>.parMap(
  context: CoroutineContext = EmptyCoroutineContext,
  transform: suspend CoroutineScope.(A) -> B
): List<B> = coroutineScope {
  map { async(context) { transform.invoke(this, it) } }.awaitAll()
}

/** Temporary intersection type, until we have context receivers */
public class ScopedAccumulatingRaise<Error>(
  raise: Raise<NonEmptyList<Error>>,
  scope: CoroutineScope
) : CoroutineScope by scope, AccumulatingRaise<Error>(raise)

public suspend fun <Error, A, B> Iterable<A>.parMapOrAccumulate(
  context: CoroutineContext = EmptyCoroutineContext,
  concurrency: Int,
  combine: (Error, Error) -> Error,
  transform: suspend ScopedAccumulatingRaise<Error>.(A) -> B
): Either<Error, List<B>> =
  coroutineScope {
    val semaphore = Semaphore(concurrency)
    map {
      async(context) {
        either {
          semaphore.withPermit {
            transform(ScopedAccumulatingRaise(this, this@coroutineScope), it)
          }
        }
      }
    }.awaitAll().flattenOrAccumulate(combine)
  }

public suspend fun <Error, A, B> Iterable<A>.parMapOrAccumulate(
  context: CoroutineContext = EmptyCoroutineContext,
  combine: (Error, Error) -> Error,
  transform: suspend ScopedAccumulatingRaise<Error>.(A) -> B
): Either<Error, List<B>> =
  coroutineScope {
    map {
      async(context) {
        either {
          transform(ScopedAccumulatingRaise(this, this@coroutineScope), it)
        }
      }
    }.awaitAll().flattenOrAccumulate(combine)
  }

public suspend fun <Error, A, B> Iterable<A>.parMapOrAccumulate(
  context: CoroutineContext = EmptyCoroutineContext,
  concurrency: Int,
  transform: suspend ScopedAccumulatingRaise<Error>.(A) -> B
): Either<NonEmptyList<Error>, List<B>> =
  coroutineScope {
    val semaphore = Semaphore(concurrency)
    map {
      async(context) {
        either {
          semaphore.withPermit {
            transform(ScopedAccumulatingRaise(this, this@coroutineScope), it)
          }
        }
      }
    }.awaitAll().flattenOrAccumulate()
  }

public suspend fun <Error, A, B> Iterable<A>.parMapOrAccumulate(
  context: CoroutineContext = EmptyCoroutineContext,
  transform: suspend ScopedAccumulatingRaise<Error>.(A) -> B
): Either<NonEmptyList<Error>, List<B>> =
  coroutineScope {
    map {
      async(context) {
        either {
          transform(ScopedAccumulatingRaise(this, this@coroutineScope), it)
        }
      }
    }.awaitAll().flattenOrAccumulate()
  }
