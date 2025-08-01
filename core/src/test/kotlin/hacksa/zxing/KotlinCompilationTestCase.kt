package hacksa.zxing

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * This is a simple test case to make sure that Kotlin is properly integrated in this library.
 * It includes a trivial test that checks if 1 + 2 equals 3.
 */
class KotlinCompilationTestCase {

  @Test
  fun trivialTest() {
    val a = 1
    val b = 2
      assertEquals(3, a + b, "1 + 2 should equal 3")
  }
}
