package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object PcSoftwareExporter {

    private const val ASSET_FILE_NAME = "HK_NEWS_PC_Studio.html"

    suspend fun savePcSoftwareToDownloads(context: Context): Uri? = withContext(Dispatchers.IO) {
        val bytes = try {
            context.assets.open(ASSET_FILE_NAME).use { it.readBytes() }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
        if (bytes.isEmpty()) return@withContext null

        val outFileName = "HK_NEWS_PC_Studio_${System.currentTimeMillis() / 1000}.html"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, outFileName)
                put(MediaStore.Downloads.MIME_TYPE, "text/html")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return@withContext null
            return@withContext try {
                resolver.openOutputStream(uri)?.use { out ->
                    out.write(bytes)
                    out.flush()
                }
                val done = ContentValues().apply { put(MediaStore.Downloads.IS_PENDING, 0) }
                resolver.update(uri, done, null, null)
                uri
            } catch (e: Exception) {
                e.printStackTrace()
                try { resolver.delete(uri, null, null) } catch (_: Exception) {}
                null
            }
        } else {
            return@withContext try {
                @Suppress("DEPRECATION")
                val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!dir.exists()) dir.mkdirs()
                val dest = File(dir, outFileName)
                dest.writeBytes(bytes)
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", dest)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    suspend fun sharePcSoftwareFile(context: Context): Boolean = withContext(Dispatchers.IO) {
        try {
            val bytes = context.assets.open(ASSET_FILE_NAME).use { it.readBytes() }
            val outFile = File(context.cacheDir, ASSET_FILE_NAME)
            outFile.writeBytes(bytes)
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                outFile
            )
            withContext(Dispatchers.Main) {
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/html"
                    putExtra(Intent.EXTRA_STREAM, contentUri)
                    putExtra(Intent.EXTRA_SUBJECT, "HK NEWS PC Studio Pro (Windows / Mac / Linux)")
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "इस HK_NEWS_PC_Studio.html फ़ाइल को अपने कंप्यूटर/लैपटॉप पर सेव करें और Chrome या Edge में डबल-क्लिक करके चलाएँ!"
                    )
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                val chooser = Intent.createChooser(sendIntent, "PC / लैपटॉप पर भेजें").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
