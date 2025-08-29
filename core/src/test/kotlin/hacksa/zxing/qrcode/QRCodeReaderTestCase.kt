package hacksa.zxing.qrcode

import com.google.zxing.BinaryBitmap
import com.google.zxing.BufferedImageLuminanceSource
import com.google.zxing.ResultMetadataType
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import java.io.FileInputStream
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals

class QRCodeReaderTestCase {
  companion object {
    const val TEST_CORRUPTED_HELLO_AUDREY_PATH = "src/test/resources/blackbox/hacksa-qrcode/corrupted/hello-audrey-1-25.png"
    const val TEST_HELLO_AUDREY_CONTENT = "Hello, Audrey!"
  }

  @Test
  fun testDecodeWithErrorPositions() {
    val bufferedImage = ImageIO.read(FileInputStream(TEST_CORRUPTED_HELLO_AUDREY_PATH))
    val bitmap = BinaryBitmap(HybridBinarizer(BufferedImageLuminanceSource(bufferedImage)))
    val reader = QRCodeReader()
    val result = reader.decodeWithErrorPositions(bitmap, null)

    assertEquals( result.text, TEST_HELLO_AUDREY_CONTENT)
    assertEquals(listOf(listOf(1,25)), result.resultMetadata[ResultMetadataType.QR_ERROR_POSITIONS])
  }
}
