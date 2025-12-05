package com.pwr_zpi.reservespotapp

import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileOutputStream
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

fun prepareImagePart(context: Context, uri: Uri): MultipartBody.Part? {
    return try {
        val contentResolver = context.contentResolver
        val type = contentResolver.getType(uri)
        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(type) ?: "jpg"
        val file = File(context.cacheDir, "upload_image.$extension")

        contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        val requestFile = file.asRequestBody(type?.toMediaTypeOrNull())
        MultipartBody.Part.createFormData("file", file.name, requestFile)
    } catch (e: Exception) {
        Log.e("Upload", "Error preparing image", e)
        null
    }
}