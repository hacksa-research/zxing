package hacksa.zxing.qrcodehalftone.encoder

import com.google.zxing.qrcode.decoder.Version
import com.google.zxing.qrcode.encoder.ByteMatrix
import com.google.zxing.qrcode.encoder.MatrixUtil.*
import com.google.zxing.qrcode.encoder.QRCode

/**
 * @author hej@an5on.com (Anson Ng)
 */
object MatrixUtil {

  private val QRCode.basicPatternsMatrix: ByteMatrix
    get() = ByteMatrix(matrix.width, matrix.height).also {
      clearMatrix(it)
      embedBasicPatterns(version, it)
    }

  private val QRCode.typeInfoMatrix: ByteMatrix
    get() = ByteMatrix(matrix.width, matrix.width).also {
      clearMatrix(it)
      embedTypeInfo(ecLevel, maskPattern, it)
    }

  private val QRCode.versionInfoMatrix: ByteMatrix
    get() = ByteMatrix(matrix.width, matrix.width).also {
      clearMatrix(it)
      maybeEmbedVersionInfo(version, it)
    }

  // Checks if the given cell (x, y) is a data bit in the QR Code matrix.
  fun checkDataBit(x: Int, y: Int, code: QRCode): Boolean {
    val matrix = code.matrix

    return !(
      checkBasicPatterns(x, y, code.basicPatternsMatrix) ||
        checkTypeInfo(x, y, code.typeInfoMatrix) ||
        maybeCheckVersionInfo(x, y, code.version, code.versionInfoMatrix)
      )
  }

  fun checkBasicPatterns(x: Int, y: Int, basicPatternsMatrix: ByteMatrix): Boolean =
    basicPatternsMatrix.get(x, y).toInt() == 1

  fun checkTypeInfo(x: Int, y: Int, typeInfoMatrix: ByteMatrix): Boolean =
    typeInfoMatrix.get(x, y).toInt() == 1

  fun maybeCheckVersionInfo(x: Int, y: Int, version: Version, versionInfoMatrix: ByteMatrix): Boolean =
    if (version.versionNumber >= 7)
      versionInfoMatrix.get(x, y).toInt() == 1
    else
      false

}
