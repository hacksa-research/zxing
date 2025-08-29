package hacksa.zxing.qrcode

import com.google.zxing.*
import com.google.zxing.common.DecoderResult
import com.google.zxing.qrcode.QRCodeReader
import com.google.zxing.qrcode.decoder.QRCodeDecoderMetaData
import com.google.zxing.qrcode.detector.Detector
import hacksa.zxing.qrcode.decoder.decodeWithErrorPositions

fun QRCodeReader.decodeWithErrorPositions(image: BinaryBitmap, hints: MutableMap<DecodeHintType?, Any>?): Result {
  val decoderResult: DecoderResult
  val points: Array<ResultPoint?>
  if (hints != null && hints.containsKey(DecodeHintType.PURE_BARCODE)) {
    val bits = QRCodeReader.extractPureBits(image.getBlackMatrix())
    decoderResult = decoder.decodeWithErrorPositions(bits, hints)
    points = QRCodeReader.NO_POINTS
  } else {
    val detectorResult = Detector(image.getBlackMatrix()).detect(hints)
    decoderResult = decoder.decodeWithErrorPositions(detectorResult.bits, hints)
    points = detectorResult.points
  }

  // If the code was mirrored: swap the bottom-left and the top-right points.
  if (decoderResult.other is QRCodeDecoderMetaData) {
    (decoderResult.other as QRCodeDecoderMetaData).applyMirroredCorrection(points)
  }

  val result = Result(decoderResult.text, decoderResult.rawBytes, points, BarcodeFormat.QR_CODE)
  val byteSegments = decoderResult.byteSegments
  if (byteSegments != null) {
    result.putMetadata(ResultMetadataType.BYTE_SEGMENTS, byteSegments)
  }
  val ecLevel = decoderResult.ecLevel
  if (ecLevel != null) {
    result.putMetadata(ResultMetadataType.ERROR_CORRECTION_LEVEL, ecLevel)
  }
  if (decoderResult.hasStructuredAppend()) {
    result.putMetadata(
      ResultMetadataType.STRUCTURED_APPEND_SEQUENCE,
      decoderResult.structuredAppendSequenceNumber
    )
    result.putMetadata(
      ResultMetadataType.STRUCTURED_APPEND_PARITY,
      decoderResult.structuredAppendParity
    )
  }
  result.putMetadata(ResultMetadataType.ERRORS_CORRECTED, decoderResult.errorsCorrected)
  result.putMetadata(ResultMetadataType.SYMBOLOGY_IDENTIFIER, "]Q" + decoderResult.symbologyModifier)
  result.putMetadata(ResultMetadataType.QR_ERROR_POSITIONS, decoderResult.errorPositions)
  return result
}
