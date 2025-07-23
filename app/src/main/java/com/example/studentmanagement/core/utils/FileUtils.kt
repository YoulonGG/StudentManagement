package com.example.studentmanagement.core.utils

import android.content.Context
import android.net.Uri
import androidx.navigation.NavOptions
import com.example.studentmanagement.R
import okio.IOException
import java.io.File
import java.io.FileOutputStream

/**
 * @Author: John Youlong.
 * @Date: 6/24/25.
 * @Email: johnyoulong@gmail.com.
 */


object FileUtils {
    @Throws(IOException::class)
    fun from(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IOException("Cannot open input stream from URI")
        val file = File(context.cacheDir, "upload_temp_file")
        val outputStream = FileOutputStream(file)
        inputStream.copyTo(outputStream)
        outputStream.close()
        inputStream.close()
        return file
    }
}