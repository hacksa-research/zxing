package hacksa.zxing.qrcodehalftone.encoder

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.Writer
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.google.zxing.qrcode.encoder.Encoder
import com.google.zxing.qrcode.encoder.QRCode
import hacksa.zxing.qrcodehalftone.encoder.MatrixUtil.checkDataBit
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

/**
 * This object renders a Halftone QR Code as a BitMatrix 2D array of greyscale values.
 *
 * @author hey@an5on.com (Anson Ng)
 */
object QRCodeHalftoneWriter : Writer {

  private const val DEFAULT_QUIET_ZONE_SIZE = 4
  private const val DEFAULT_BLOCK_SIZE = 3

  @Throws(WriterException::class)
  override fun encode(contents: String, format: BarcodeFormat, width: Int, height: Int): BitMatrix =
    encode(contents, format, width, height, null)

  override fun encode(
    contents: String,
    format: BarcodeFormat,
    width: Int,
    height: Int,
    hints: Map<EncodeHintType?, *>?
  ): BitMatrix {
    require(!contents.isEmpty()) { "Found empty contents" }

    require(format == BarcodeFormat.QR_CODE_HALFTONE) { "Can only encode QR_CODE_HALFTONE, but got $format" }

    require(!(width < 0 || height < 0)) {
      "Requested dimensions are too small: " + width + 'x' + height
    }

    val errorCorrectionLevel = (hints?.get(EncodeHintType.ERROR_CORRECTION) as? String)
      ?.let { ErrorCorrectionLevel.valueOf(it) } ?: ErrorCorrectionLevel.L
    val quietZone = (hints?.get(EncodeHintType.MARGIN) as? Int) ?: DEFAULT_QUIET_ZONE_SIZE
    val blockSize = (hints?.get(EncodeHintType.QR_HALFTONE_BLOCK_SIZE) as? Int) ?: DEFAULT_BLOCK_SIZE

    val code = Encoder.encode(contents, errorCorrectionLevel, hints)
    return renderResult(code, width, height, quietZone, blockSize)
  }

  /**
   * Renders the given {@link QRCode} as a {@link BitMatrix} in halftone style, scaling the
   * same to be compliant with the provided dimensions.
   *
   * <p>If no scaling is required, both {@code width} and {@code height}
   * arguments should be non-positive numbers.
   *
   * @param code {@code QRCode} to be adapted as a {@code BitMatrix}
   * @param width desired width for the {@code QRCode} (in pixel units)
   * @param height desired height for the {@code QRCode} (in pixel units)
   * @param quietZone the size of the QR quiet zone (in pixel units)
   * @param blockSize the size of one halftone block (in pixel units)
   * @return {@code BitMatrix} instance
   *
   * @throws IllegalStateException if {@code code} does not have
   *      a {@link QRCode#getMatrix() Matrix}
   */
  fun renderResult(code: QRCode, width: Int, height: Int, quietZone: Int, blockSize: Int): BitMatrix {
    // Note that the input matrix uses 0 == white, 1 == black, while the output matrix uses
    // 0 == black, 255 == white (i.e. an 8 bit greyscale bitmap).
    val input = code.matrix ?: throw IllegalStateException()

    val inputWidth = input.width
    val inputHeight = input.height
    val qrWidth = inputWidth * blockSize + 2 * quietZone
    val qrHeight = inputHeight * blockSize + 2 * quietZone
    val outputWidth = max(qrWidth, width)
    val outputHeight = max(qrHeight, height)

    val multiple = min(outputWidth / qrWidth, outputHeight / qrHeight)
    // Padding includes both the quiet zone and the extra white pixels to accommodate the requested
    // dimensions. For example, if input is 25x25 the QR will be 33x33 including the quiet zone.
    // If the requested size is 200x160, the multiple will be 4, for a QR of 132x132. These will
    // handle all the padding from 100x100 (the actual QR) up to 200x160.
    val leftPadding = (outputWidth - (inputWidth * blockSize * multiple)) / 2
    val topPadding = (outputHeight - (inputHeight * blockSize * multiple)) / 2
    // Block padding is the extra white pixels around each side of a block.
    val blockPadding = ceil((blockSize * multiple - multiple) / 2.0).toInt()

    val output = BitMatrix(outputWidth, outputHeight)


    for (inputY in 0..<inputHeight) {
      for (inputX in 0..<inputWidth) {
        if (input.get(inputX, inputY).toInt() == 0) {
          continue
        }

        val outputBlockStartX = leftPadding + inputX * multiple * blockSize
        val outputBlockStartY = topPadding + inputY * multiple * blockSize
        // The length of the data cell is always the same as the multiple.
        if (checkDataBit(inputX, inputY, code)) {
          output.setRegion(outputBlockStartX + blockPadding, outputBlockStartY + blockPadding, multiple, multiple)
        } else {
          // If this cell is a control cell, we need to set the whole block.
          output.setRegion(outputBlockStartX, outputBlockStartY, multiple * blockSize, multiple * blockSize)
        }
      }
    }

    return output
  }
}
