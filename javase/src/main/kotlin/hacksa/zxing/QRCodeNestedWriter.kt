package hacksa.zxing

import com.google.zxing.client.j2se.MatrixToImageWriter
import java.awt.image.BufferedImage

/**
 * Generate a Nested QR Code.
 * Provided here instead of core since it depends on
 * Java SE libraries.
 *
 * @author hej@an5on.com (Anson Ng)
 */
object QRCodeNestedWriter {
  fun encodeToBufferedImage(specs: List<QRCodeSpec>): BufferedImage {
    val result = BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB)
    for (spec in specs) {
      val specBufferedImage = MatrixToImageWriter.toBufferedImage(spec.bitMatrix, spec.matrixToImageConfig)
    }
    TODO()
    return result
  }
}
