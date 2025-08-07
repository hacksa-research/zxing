package hacksa.zxing.qrcodehalftone

import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.encoder.ByteMatrix
import com.google.zxing.qrcode.encoder.QRCode
import hacksa.zxing.qrcodehalftone.encoder.QRCodeHalftoneWriter
import java.nio.file.Files
import java.nio.file.Paths
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * @author hej@an5on.com (Anson Ng)
 */
class QRCodeHalftoneWriterTestCase {
  companion object {
    private const val TEST_CONTENT = "Hello, Audrey!"
    private val TEMP_IMAGE_PATH = Paths.get("src/test/resources/temp/qrcode/halftone")
  }

  @Test
  fun testQRCodeHalftoneWriter() {
    // The QR should be multiplied up to fit, with extra padding if necessary
    val bigEnough = 265
    val bigEnoughMatrix = QRCodeHalftoneWriter.encode(TEST_CONTENT, BarcodeFormat.QR_CODE_HALFTONE, bigEnough,
    bigEnough)

    assertEquals(bigEnough, bigEnoughMatrix.width)
    assertEquals(bigEnough, bigEnoughMatrix.height)

    // The QR will not fit in this size, so the matrix should come back bigger
    val tooSmall = 20
    val tooSmallMatrix = QRCodeHalftoneWriter.encode(TEST_CONTENT, BarcodeFormat.QR_CODE_HALFTONE, tooSmall,
      tooSmall)

    assertTrue { tooSmall < tooSmallMatrix.width }
    assertTrue { tooSmall < tooSmallMatrix.height }

    // We should also be able to handle non-square requests by padding them
    val strangeWidth = 500
    val strangeHeight = 100
    val strangeMatrix = QRCodeHalftoneWriter.encode(TEST_CONTENT, BarcodeFormat.QR_CODE_HALFTONE, strangeWidth,
      strangeHeight)

    assertEquals(strangeWidth, strangeMatrix.width)
    assertEquals(strangeHeight, strangeMatrix.height)
  }

  @Test
  fun testRenderResultScalesNothingWithOddBlockSize() {
    val expectedSize = 83 // Original Size (25) * Block Size (3) + quietZone * 2

    val code = QRCode().apply {
      matrix = ByteMatrix(25, 25) // QR Version 2! It's all white
      // but it doesn't matter here
    }

    // Test:
    val result = QRCodeHalftoneWriter.renderResult(code, -1, -1, 4, 3)

    assertEquals(result.height.toLong(), expectedSize.toLong())
    assertEquals(result.width.toLong(), expectedSize.toLong())
  }

  @Test
  fun testRenderResultScalesNothingWithEvenBlockSize() {
    val expectedSize = 108 // Original Size (25) * Block Size (4) + quietZone * 2

    val code = QRCode().apply {
      matrix = ByteMatrix(25, 25) // QR Version 2! It's all white
      // but it doesn't matter here
    }

    // Test:
    val result = QRCodeHalftoneWriter.renderResult(code, -1, -1, 4, 4)

    assertEquals(result.height.toLong(), expectedSize.toLong())
    assertEquals(result.width.toLong(), expectedSize.toLong())
  }

  @Test
  fun testRenderResultScalesWhenRequired() {
    val expectedSize = 120

    val code = QRCode().apply {
      matrix = ByteMatrix(25, 25) // QR Version 2! It's all white
      // but it doesn't matter here
    }

    // Test:
    val result = QRCodeHalftoneWriter.renderResult(code, 120, 120, 4, 3)

    assertEquals(result.height.toLong(), expectedSize.toLong())
    assertEquals(result.width.toLong(), expectedSize.toLong())
  }

  @Test
  fun testRenderResultThrowsExIfCodeIsIncomplete() {
    assertFailsWith<IllegalStateException> {
      QRCodeHalftoneWriter.renderResult(QRCode(), 0, 0, 0, 0)
    }
  }

  @Test
  fun testDecodeEncodedQRCodeHalftone() {
    // Encode a QR code in halftone style
    val matrix = QRCodeHalftoneWriter.encode(
      TEST_CONTENT, BarcodeFormat.QR_CODE_HALFTONE, 200, 200,
      mapOf(
        EncodeHintType.ERROR_CORRECTION to "L",
        EncodeHintType.MARGIN to 1,
        EncodeHintType.QR_HALFTONE_BLOCK_SIZE to 3
      ))

    val path = Files.createTempFile("hacksa_qrcode_halftone", ".png")
    MatrixToImageWriter.writeToPath(matrix, "png", path)

    // And then decode it to validate the content
    val img = ImageIO.read(path.toFile())
    val binaryBitmap = BinaryBitmap(HybridBinarizer(BufferedImageLuminanceSource(img)))
    val result = MultiFormatReader().decode(binaryBitmap)

    // Clean up the temp file
    Files.delete(path)

    assertEquals(TEST_CONTENT, result.text)
  }
}
