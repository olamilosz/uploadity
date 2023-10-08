package com.uploadity.tools

import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream

class ImageTools {
    private fun encodeImage(bm: Bitmap): String? {
        val baos = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val b = baos.toByteArray()
        return Base64.encodeToString(b, Base64.DEFAULT)
    }
}