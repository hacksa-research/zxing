package hacksa.zxing

import com.google.zxing.client.j2se.MatrixToImageWriter
import java.awt.Graphics2D
import java.awt.Image
import java.awt.image.BufferedImage
import kotlin.math.max

/**
 * Generate a Nested QR Code.
 * Provided here instead of core since it depends on
 * Java SE libraries.
 *
 * @author hej@an5on.com (Anson Ng)
 */
object QRCodeNestedWriter {
  fun encodeToBufferedImage(specs: List<QRCodeSpec>): BufferedImage {
    var result = BufferedImage(specs[0].bitMatrix?.width!!, specs[0].bitMatrix?.height!!, BufferedImage.TYPE_INT_ARGB)
    val bufferedImages = specs.map {
      MatrixToImageWriter.toBufferedImage(it.bitMatrix, it.matrixToImageConfig)
    }

    bufferedImages.forEachIndexed {
      index, it ->
      val newResult: BufferedImage

      if (specs[index].ratio == null) {
        newResult = it
      } else {
        val innerWidth = max(it.width, (result.width * specs[index].ratio!!).toInt())
        val innerImage: Image
        val graphics: Graphics2D

        if (it.width < innerWidth) {
          newResult = result
          graphics = newResult.createGraphics()

          innerImage = it.getScaledInstance(innerWidth, innerWidth, Image.SCALE_DEFAULT)
        } else {
          val outerWidth = (innerWidth / specs[index].ratio!!).toInt()
          newResult = BufferedImage(outerWidth, outerWidth, BufferedImage.TYPE_INT_ARGB)
          graphics = newResult.createGraphics()
          graphics.drawImage(result, 0, 0, outerWidth, outerWidth, null)

          innerImage = it
        }

        val leftPadding = (newResult.width * specs[index].leftPaddingInPercentage).toInt()
        val topPadding = (newResult.height * specs[index].topPaddingInPercentage).toInt()
        graphics.drawImage(innerImage, leftPadding, topPadding, innerWidth, innerWidth, null)
        graphics.dispose()
      }
      result = newResult
    }
    return result
  }
}
