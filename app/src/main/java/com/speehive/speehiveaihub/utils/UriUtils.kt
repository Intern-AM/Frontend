package com.speehive.speehiveaihub.utils

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

inline fun <R> Uri.toMultipartBodyPart(
    context: Context,
    partName: String = "image",
    prefix: String = "upload_",
    block: (MultipartBody.Part) -> Result<R>
): Result<R> {
    val tempFile = File(context.cacheDir, "${prefix}${System.currentTimeMillis()}.jpg")
    return try {
        val mimeType = context.contentResolver.getType(this) ?: "image/jpeg"
        val allowedTypes = listOf("image/png", "image/jpeg")
        if (mimeType !in allowedTypes) {
            return Result.failure(Exception("Only PNG and JPEG images are allowed"))
        }

        val inputStream = context.contentResolver.openInputStream(this)
            ?: return Result.failure(Exception("Cannot open image"))

        inputStream.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        val maxSize = 10L * 1024 * 1024
        if (tempFile.length() > maxSize) {
            return Result.failure(Exception("File size must be under 10 MB"))
        }

        val requestBody = tempFile.asRequestBody(mimeType.toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData(partName, tempFile.name, requestBody)

        block(part)
    } catch (e: Exception) {
        Result.failure(e)
    } finally {
        tempFile.delete()
    }
}
