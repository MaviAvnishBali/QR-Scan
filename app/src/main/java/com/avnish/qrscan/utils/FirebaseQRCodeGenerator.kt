package com.avnish.qrscan.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Hashtable

class QRCodeGenerator private constructor() {
    private val writer = QRCodeWriter()
    private val hints = Hashtable<EncodeHintType, Any>().apply {
        put(EncodeHintType.CHARACTER_SET, "UTF-8")
        put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H)
        put(EncodeHintType.MARGIN, 1)
    }

    companion object {
        @Volatile
        private var instance: QRCodeGenerator? = null

        fun getInstance(): QRCodeGenerator {
            return instance ?: synchronized(this) {
                instance ?: QRCodeGenerator().also { instance = it }
            }
        }
    }

    suspend fun generateQRCode(context: Context, content: String, width: Int = 500, height: Int = 500): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height, hints)
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                
                for (x in 0 until width) {
                    for (y in 0 until height) {
                        bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                    }
                }
                bitmap
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
} 