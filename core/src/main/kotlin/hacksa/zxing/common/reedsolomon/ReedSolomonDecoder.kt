package hacksa.zxing.common.reedsolomon

import com.google.zxing.common.reedsolomon.GenericGF
import com.google.zxing.common.reedsolomon.GenericGFPoly
import com.google.zxing.common.reedsolomon.ReedSolomonDecoder
import com.google.zxing.common.reedsolomon.ReedSolomonException

/**
 * Decodes given set of received codewords, which include both data and error-correction
 * codewords. Really, this means it uses Reed-Solomon to detect and correct errors, in-place,
 * in the input. Additionally, records the position of the errors.
 *
 * @param received data and error-correction codewords
 * @param twoS number of error-correction codewords available
 * @return the number of errors corrected
 * @throws ReedSolomonException if decoding fails for any reason
 *
 * @author hej@an5on.com (Anson Ng)
 */
fun ReedSolomonDecoder.decodeWithErrorPositions(received: IntArray, twoS: Int): List<Int> {
  val poly = GenericGFPoly(field, received)
  val syndromeCoefficients = IntArray(twoS)
  for (i in 0..<twoS) {
    val eval = poly.evaluateAt(field.exp(i + field.generatorBase))
    syndromeCoefficients[syndromeCoefficients.size - 1 - i] = eval
  }
  val syndrome = GenericGFPoly(field, syndromeCoefficients)
  val sigmaOmega =
    runEuclideanAlgorithm(field.buildMonomial(twoS, 1), syndrome, twoS)
  val sigma = sigmaOmega[0]
  val omega = sigmaOmega[1]
  val errorLocations = findErrorLocations(sigma)
  val errorMagnitudes = findErrorMagnitudes(omega, errorLocations)
  for (i in errorLocations.indices) {
    val position = received.size - 1 - field.log(errorLocations[i])
    if (position < 0) {
      throw ReedSolomonException("Bad error location")
    }
    received[position] = GenericGF.addOrSubtract(received[position], errorMagnitudes[i])
  }
  return errorLocations.map { received.size - 1 - field.log(it) }
}
