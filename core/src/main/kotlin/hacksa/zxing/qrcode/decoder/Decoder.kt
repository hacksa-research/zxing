package hacksa.zxing.qrcode.decoder

import com.google.zxing.ChecksumException
import com.google.zxing.DecodeHintType
import com.google.zxing.FormatException
import com.google.zxing.common.BitMatrix
import com.google.zxing.common.DecoderResult
import com.google.zxing.qrcode.decoder.*
import hacksa.zxing.common.reedsolomon.decodeWithErrorPositions

/**
 * Decodes a QR Code represented as a {@link BitMatrix}. A 1 or "true" is taken to mean a black module.
 * Additionally, records the position of the errors.
 *
 * @param bits booleans representing white/black QR Code modules
 * @param hints decoding hints that should be used to influence decoding
 * @return text and bytes encoded within the QR Code
 * @throws FormatException if the QR Code cannot be decoded
 * @throws ChecksumException if error correction fails
 *
 * @author hej@an5on.com (Anson Ng)
 * @author
 */
fun Decoder.decodeWithErrorPositions(bits: BitMatrix, hints: Map<DecodeHintType?, Any>?): DecoderResult {
  // Construct a parser and read version, error-correction level
  val parser = BitMatrixParser(bits)
  var fe: FormatException? = null
  var ce: ChecksumException? = null
  try {
    return decodeWithErrorPositions(parser, hints)
  } catch (e: FormatException) {
    fe = e
  } catch (e: ChecksumException) {
    ce = e
  }

  try {
    // Revert the bit matrix
    parser.remask()

    // Will be attempting a mirrored reading of the version and format info.
    parser.setMirror(true)

    // Preemptively read the version.
    parser.readVersion()

    // Preemptively read the format information.
    parser.readFormatInformation()

    /*
       * Since we're here, this means we have successfully detected some kind
       * of version and format information when mirrored. This is a good sign,
       * that the QR code may be mirrored, and we should try once more with a
       * mirrored content.
       */
    // Prepare for a mirrored reading.
    parser.mirror()

    val result = decodeWithErrorPositions(parser, hints)

    // Success! Notify the caller that the code was mirrored.
    result.other = QRCodeDecoderMetaData(true)

    return result
  } catch (e: FormatException) {
    // Throw the exception from the original reading
    throw fe ?: ce!!
  } catch (e: ChecksumException) {
    throw fe ?: ce!!
  }
}

private fun Decoder.decodeWithErrorPositions(parser: BitMatrixParser, hints: Map<DecodeHintType?, Any>?): DecoderResult {
  val version: Version = parser.readVersion()
  val ecLevel: ErrorCorrectionLevel? = parser.readFormatInformation().errorCorrectionLevel

  // Read codewords
  val codewords: ByteArray = parser.readCodewords()

  // Separate into data blocks
  val dataBlocks = DataBlock.getDataBlocks(codewords, version, ecLevel)

  // Count total number of data bytes
  val totalBytes = dataBlocks.sumOf { it.numDataCodewords }
  val resultBytes = ByteArray(totalBytes)
  var resultOffset = 0

  // Error-correct and copy data blocks together into a stream of bytes
  var errorsCorrected = 0
  val errorPositionsAcrossBlocks = mutableListOf<List<Int>>()
  for (dataBlock in dataBlocks) {
    val codewordBytes = dataBlock.codewords
    val numDataCodewords = dataBlock.numDataCodewords
    val errorPositions = correctErrorsWithPositions(codewordBytes, numDataCodewords).sorted()
    errorPositionsAcrossBlocks.add(errorPositions)
    errorsCorrected += errorPositions.size
    for (i in 0..<numDataCodewords) {
      resultBytes[resultOffset++] = codewordBytes[i]
    }
  }

  // Decode the contents of that stream of bytes
  val result = DecodedBitStreamParser.decode(resultBytes, version, ecLevel, hints)
  return result.apply {
    this.errorsCorrected = errorsCorrected
    errorPositions = errorPositionsAcrossBlocks
  }
}

/**
 * Given data and error-correction codewords received, possibly corrupted by errors, attempts to
 * correct the errors in-place using Reed-Solomon error correction.
 *
 * @param codewordBytes data and error correction codewords
 * @param numDataCodewords number of codewords that are data bytes
 * @return the position of the block that errors occurred starting from lower right to top left
 * @throws ChecksumException if error correction fails
 *
 * @author hej@an5on.com (Anson Ng)
 */
fun Decoder.correctErrorsWithPositions(codewordBytes: ByteArray, numDataCodewords: Int): List<Int> {
  val codewordsInts = codewordBytes.map { it.toInt() and 0xFF }.toIntArray()
  return rsDecoder.decodeWithErrorPositions(codewordsInts, codewordBytes.size - numDataCodewords).also {
    codewordsInts.forEachIndexed { index, codeword -> codewordBytes[index] = codeword.toByte()}
  }
}


