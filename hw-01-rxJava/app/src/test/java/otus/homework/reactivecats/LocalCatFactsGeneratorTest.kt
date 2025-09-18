package otus.homework.reactivecats

import android.content.Context
import android.content.res.Resources
import io.reactivex.observers.TestObserver
import io.reactivex.subscribers.TestSubscriber
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import java.util.concurrent.TimeUnit

@RunWith(MockitoJUnitRunner::class)
class LocalCatFactsGeneratorTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockResources: Resources

    private lateinit var generator: LocalCatFactsGenerator

    private val testFacts = arrayOf(
        "Cats have 230 bones, while humans only have 206.",
        "Cats have whiskers on the backs of their front legs, as well.",
        "Some cats are ambidextrous, but 40 percent are either left- or right-pawed.",
        "Cats live longer when they stay indoors.",
        "Cats can spend up to a third of their waking hours grooming.",
        "Cats will refuse an unpalatable food to the point of starvation."
    )

    @Before
    fun setUp() {
        `when`(mockContext.resources).thenReturn(mockResources)
        `when`(mockResources.getStringArray(R.array.local_cat_facts)).thenReturn(testFacts)

        generator = LocalCatFactsGenerator(mockContext)
    }

    @Test
    fun `generateCatFact should return Single with Fact containing random fact from array`() {
        val testObserver = TestObserver<Fact>()

        generator.generateCatFact().subscribe(testObserver)

        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.assertValueCount(1)

        val fact = testObserver.values()[0]
        assertTrue("Fact should be from test array", testFacts.contains(fact.fact))
    }

    @Test
    fun `generateCatFactPeriodically should emit facts every 2 seconds`() {
        generator.generateCatFactPeriodically()
            .take(3)
            .test()
            .awaitDone(6100, TimeUnit.MILLISECONDS)
            .assertComplete()
            .assertNoErrors()
            .assertValueCount(3)
            .assertValueAt(0) { fact -> testFacts.contains(fact.fact) }
            .assertValueAt(1) { fact -> testFacts.contains(fact.fact) }
            .assertValueAt(2) { fact -> testFacts.contains(fact.fact) }
    }

    @Test
    fun `generateCatFactPeriodically should filter duplicate consecutive facts`() {
        val testSubscriber = TestSubscriber<Fact>()

        // Создаем мок с ограниченным набором фактов
        val limitedFacts = arrayOf("Fact 1", "Fact 1", "Fact 2", "Fact 2")
        `when`(mockContext.resources.getStringArray(R.array.local_cat_facts)).thenReturn(
            limitedFacts
        )

        val testGenerator = LocalCatFactsGenerator(mockContext)

        testGenerator.generateCatFactPeriodically()
            .take(5) // Берем больше элементов чтобы проверить distinct
            .subscribe(testSubscriber)

        testSubscriber.await(10, TimeUnit.SECONDS)

        // Проверяем что нет подряд идущих дубликатов
        val facts = testSubscriber.values()
        print(facts.size)
        for (i in 1 until facts.size) {
            assertTrue(
                "Consecutive facts should not be equal: ${facts[i - 1]} and ${facts[i]}",
                facts[i].fact != facts[i - 1].fact
            )
        }
    }

}