package kategory.instances

import io.kotlintest.KTestJUnitRunner
import kategory.*
import kategory.laws.FunctorFilterLaws
import org.junit.runner.RunWith

typealias OptionTNel = HK<OptionTKindPartial<NonEmptyListHK>, Int>

@RunWith(KTestJUnitRunner::class)
class ComposedInstancesTest : UnitSpec() {
    val EQ_OPTION_NEL: Eq<HK<Nested<OptionHK, NonEmptyListHK>, Int>> = object : Eq<HK<Nested<OptionHK, NonEmptyListHK>, Int>> {
        override fun eqv(a: HK<Nested<OptionHK, NonEmptyListHK>, Int>, b: HK<Nested<OptionHK, NonEmptyListHK>, Int>): Boolean =
                a.unnest().ev() == b.unnest().ev()
    }

    val EQ_LKW_OPTION: Eq<HK<Nested<ListKWHK, OptionHK>, Int>> = object : Eq<HK<Nested<ListKWHK, OptionHK>, Int>> {
        override fun eqv(a: HK<Nested<ListKWHK, OptionHK>, Int>, b: HK<Nested<ListKWHK, OptionHK>, Int>): Boolean =
                a.unnest().ev() == b.unnest().ev()
    }

    val EQ_OPTIONT_ID_NEL: Eq<HK<Nested<OptionTKindPartial<IdHK>, OptionTKindPartial<NonEmptyListHK>>, Int>> =
            object : Eq<HK<Nested<OptionTKindPartial<IdHK>, OptionTKindPartial<NonEmptyListHK>>, Int>> {
                override fun eqv(a: HK<Nested<OptionTKindPartial<IdHK>, OptionTKindPartial<NonEmptyListHK>>, Int>, b: HK<Nested<OptionTKindPartial<IdHK>, OptionTKindPartial<NonEmptyListHK>>, Int>): Boolean =
                        a.unnest().value().value().fold(
                                { b.unnest().value().value().isEmpty },
                                { optionA: OptionTNel ->
                                    b.unnest().value().value().ev().fold(
                                            { false },
                                            { it.value() == optionA.value() })
                                })
            }

    val cf: (Int) -> HK<Nested<OptionHK, NonEmptyListHK>, Int> = { it.nel().some().nest() }

    init {
        testLaws(FunctorLaws.laws(ComposedFunctor(Option.functor(), NonEmptyList.functor()), cf, EQ_OPTION_NEL))
        testLaws(FunctorFilterLaws.laws(ComposedFunctorFilter(OptionT.functorFilter(Id.monad()), OptionT.functorFilter(NonEmptyList.monad())), { OptionT.pure(OptionT.pure(it, NonEmptyList.monad()), Id.monad()).nest() }, EQ_OPTIONT_ID_NEL))
        testLaws(ApplicativeLaws.laws(ComposedApplicative(Option.applicative(), NonEmptyList.applicative()), EQ_OPTION_NEL))
        testLaws(FoldableLaws.laws(ComposedFoldable(Option.foldable(), NonEmptyList.foldable()), cf, Eq.any()))
        testLaws(TraverseLaws.laws(ComposedTraverse(Option.traverse(), NonEmptyList.traverse(), NonEmptyList.applicative()), ComposedFunctor.invoke(Option.functor(), NonEmptyList.functor()), cf, EQ_OPTION_NEL))
        testLaws(SemigroupKLaws.laws(ComposedSemigroupK<ListKWHK, OptionHK>(ListKW.semigroupK()), ComposedApplicative(ListKW.applicative(), Option.applicative()), EQ_LKW_OPTION))
        testLaws(MonoidKLaws.laws(ComposedMonoidK<ListKWHK, OptionHK>(ListKW.monoidK()), ComposedApplicative(ListKW.applicative(), Option.applicative()), EQ_LKW_OPTION))
    }
}