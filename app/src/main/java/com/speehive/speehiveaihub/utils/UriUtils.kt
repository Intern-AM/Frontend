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

inline fun <R> Uri.toBulkImportFilePart(
    context: Context,
    partName: String = "file",
    block: (MultipartBody.Part) -> Result<R>
): Result<R> {
    val tempFile = File(context.cacheDir, "import_${System.currentTimeMillis()}.xlsx")
    return try {
        val mimeType = context.contentResolver.getType(this) ?: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        val inputStream = context.contentResolver.openInputStream(this)
            ?: return Result.failure(Exception("Cannot open file"))

        inputStream.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
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

fun Uri.getFileName(context: Context): String {
    var result: String? = null
    if (scheme == "content") {
        val cursor = context.contentResolver.query(this, null, null, null, null)
        try {
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    result = cursor.getString(index)
                }
            }
        } finally {
            cursor?.close()
        }
    }
    if (result == null) {
        result = path
        val cut = result?.lastIndexOf('/') ?: -1
        if (cut != -1) {
            result = result?.substring(cut + 1)
        }
    }
    return result ?: "unknown_file.xlsx"
}

