package hacksa.zxing

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.client.j2se.MatrixToImageConfig
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.google.zxing.qrcode.encoder.Encoder
import com.google.zxing.qrcode.encoder.QRCode
import hacksa.zxing.qrcodehalftone.encoder.QRCodeHalftoneWriter
import hacksa.zxing.qrcodehalftone.encoder.QRCodeHalftoneWriter.DEFAULT_BLOCK_SIZE
import hacksa.zxing.qrcodehalftone.encoder.QRCodeHalftoneWriter.DEFAULT_QUIET_ZONE_SIZE

/**
 * a data class to store the specification of a QR Code and the encoded matrix of itself.
 *
 * @author hej@an5on.com (Anson Ng)
 */
data class QRCodeSpec(val content: String, val format: BarcodeFormat, val ratio: Int, val leftPaddingInPercentage: Double, val topPaddingInPercentage: Double, val zValue: Int, val imageConfig: MatrixToImageConfig = MatrixToImageConfig(), val hints: Map<EncodeHintType, Any>?) {
  val code: QRCode
  val bitMatrix: BitMatrix
  init {
    val errorCorrectionLevel = (hints?.get(EncodeHintType.ERROR_CORRECTION) as? String)
      ?.let { ErrorCorrectionLevel.valueOf(it) } ?: ErrorCorrectionLevel.L
    val quietZone = (hints?.get(EncodeHintType.MARGIN) as? Int) ?: DEFAULT_QUIET_ZONE_SIZE
    val blockSize = (hints?.get(EncodeHintType.QR_HALFTONE_BLOCK_SIZE) as? Int) ?: DEFAULT_BLOCK_SIZE

    code = Encoder.encode(content, errorCorrectionLevel, hints)
    bitMatrix = QRCodeHalftoneWriter.renderResult(code, 0, 0, quietZone, blockSize)
  }
}
