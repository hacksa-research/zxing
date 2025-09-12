package hacksa.zxing.client.j2se

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.client.j2se.MatrixToImageConfig
import hacksa.zxing.QRCodeNestedWriter
import hacksa.zxing.QRCodeSpec
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test

class test {
  @Test
  fun test2() {
    val specs = listOf(
      QRCodeSpec(
        "Outer", BarcodeFormat.QR_CODE, null, 0.1, 0.1, 1, MatrixToImageConfig(0x3313727, -1), mapOf(
          EncodeHintType.QR_HALFTONE_BLOCK_SIZE to 3, EncodeHintType.QR_VERSION to 7
        )
      ),
      QRCodeSpec("Inner", BarcodeFormat.QR_CODE, 1.2, 0.6, 0.5, 2, hints = null)
    )

    val img = QRCodeNestedWriter.encodeToBufferedImage(specs)
    ImageIO.write(img, "png", File("avtest.png"))
  }
}
