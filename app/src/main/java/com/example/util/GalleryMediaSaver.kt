package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.data.model.NewsProject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object GalleryMediaSaver {

    /**
     * Saves an existing video URI (recorded or picked) directly to the phone's Video Gallery
     */
    suspend fun saveVideoToGallery(
        context: Context,
        sourceUri: Uri,
        projectTitle: String = "HK_News"
    ): Uri? = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "HK_NEWS_${timeStamp}.mp4"

        val values = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.TITLE, projectTitle)
            put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
            put(MediaStore.Video.Media.DATE_MODIFIED, System.currentTimeMillis() / 1000)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "${Environment.DIRECTORY_MOVIES}/HkNewsStudio")
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }
        }

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        var targetUri: Uri? = null
        try {
            targetUri = resolver.insert(collection, values)
            if (targetUri != null) {
                resolver.openOutputStream(targetUri)?.use { outStream ->
                    resolver.openInputStream(sourceUri)?.use { inStream ->
                        inStream.copyTo(outStream)
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.clear()
                    values.put(MediaStore.Video.Media.IS_PENDING, 0)
                    resolver.update(targetUri, values, null, null)
                }

                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(targetUri.path ?: ""),
                    arrayOf("video/mp4"),
                    null
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (targetUri != null) {
                resolver.delete(targetUri, null, null)
                targetUri = null
            }
        }

        targetUri
    }

    /**
     * Renders and saves an HD 1080p News Broadcast Graphic / Video Poster to the Photos Gallery
     */
    suspend fun saveNewsFrameToGallery(
        context: Context,
        project: NewsProject
    ): Uri? = withContext(Dispatchers.IO) {
        val width = 1280
        val height = 720
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background dark gradient
        val bgPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                android.graphics.Color.parseColor("#0A0E1A"),
                android.graphics.Color.parseColor("#151D2F"),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Accent theme strip
        val accentColor = try {
            android.graphics.Color.parseColor(project.themeColorHex)
        } catch (_: Exception) {
            android.graphics.Color.parseColor("#C62828")
        }
        val themePaint = Paint().apply { color = accentColor }

        // Top Brand Banner
        val brandBoxPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#0D1527")
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(RectF(24f, 24f, 320f, 100f), 12f, 12f, brandBoxPaint)

        val goldPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#FFD54F")
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawRoundRect(RectF(24f, 24f, 320f, 100f), 12f, 12f, goldPaint)

        // Brand Texts
        val textPaint = Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 28f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText("हर खबर", 40f, 60f, textPaint)

        textPaint.color = android.graphics.Color.parseColor("#FFEB3B")
        textPaint.textSize = 20f
        canvas.drawText("${project.location} समाचार", 40f, 88f, textPaint)

        textPaint.color = android.graphics.Color.parseColor("#00E5FF")
        textPaint.textSize = 22f
        canvas.drawText("H.K NEWS", 180f, 75f, textPaint)

        // LIVE Badge
        val livePaint = Paint().apply {
            color = android.graphics.Color.RED
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(RectF(340f, 34f, 430f, 85f), 8f, 8f, livePaint)
        textPaint.color = android.graphics.Color.WHITE
        textPaint.textSize = 24f
        canvas.drawText("LIVE", 355f, 70f, textPaint)

        // District chip
        val distPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#1E293B")
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(RectF(width - 240f, 34f, width - 24f, 85f), 8f, 8f, distPaint)
        textPaint.color = android.graphics.Color.parseColor("#FFD54F")
        textPaint.textSize = 20f
        canvas.drawText("📍 ${project.location}", width - 220f, 68f, textPaint)

        // Reporter Lower Third
        if (project.showReporterCard && project.reporterName.isNotBlank()) {
            val repPaint = Paint().apply {
                color = android.graphics.Color.parseColor("#0F172A")
            }
            canvas.drawRoundRect(RectF(24f, height - 280f, 400f, height - 200f), 8f, 8f, repPaint)
            canvas.drawRect(24f, height - 280f, 32f, height - 200f, themePaint)

            textPaint.color = android.graphics.Color.WHITE
            textPaint.textSize = 26f
            canvas.drawText(project.reporterName, 44f, height - 245f, textPaint)

            textPaint.color = android.graphics.Color.parseColor("#FFD54F")
            textPaint.textSize = 18f
            canvas.drawText("${project.reporterRole} • ${project.location}", 44f, height - 218f, textPaint)
        }

        // Headline Banner Ribbon
        canvas.drawRect(0f, height - 190f, 260f, height - 150f, themePaint)
        textPaint.color = android.graphics.Color.parseColor("#FFD54F")
        textPaint.textSize = 22f
        canvas.drawText(project.category.uppercase(), 16f, height - 162f, textPaint)

        // Headline Main Card
        val headPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#0B132B")
        }
        canvas.drawRect(0f, height - 150f, width.toFloat(), height - 55f, headPaint)
        canvas.drawLine(0f, height - 150f, width.toFloat(), height - 150f, goldPaint)

        textPaint.color = android.graphics.Color.WHITE
        textPaint.textSize = 34f
        canvas.drawText(project.headline.take(45), 24f, height - 105f, textPaint)

        if (project.subHeadline.isNotBlank()) {
            textPaint.color = android.graphics.Color.parseColor("#FFCA28")
            textPaint.textSize = 22f
            canvas.drawText(project.subHeadline.take(60), 24f, height - 70f, textPaint)
        }

        // Ticker Tape
        val tickerBgPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#0F172A")
        }
        canvas.drawRect(0f, height - 55f, width.toFloat(), height.toFloat(), tickerBgPaint)

        val tickerTagPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#FFD54F")
        }
        canvas.drawRect(0f, height - 55f, 150f, height.toFloat(), tickerTagPaint)

        val blackText = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 20f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText("ताज़ा अपडेट", 20f, height - 20f, blackText)

        textPaint.color = android.graphics.Color.WHITE
        textPaint.textSize = 20f
        canvas.drawText(project.tickerText, 170f, height - 20f, textPaint)

        // Save Bitmap to MediaStore
        saveBitmapToMediaStore(context, bitmap, "HK_NEWS_POSTER_${System.currentTimeMillis()}")
    }

    private fun saveBitmapToMediaStore(
        context: Context,
        bitmap: Bitmap,
        title: String
    ): Uri? {
        val resolver = context.contentResolver
        val fileName = "$title.png"

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.TITLE, title)
            put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/HkNewsStudio")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        var uri: Uri? = null
        try {
            uri = resolver.insert(collection, values)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(uri, values, null, null)
                }

                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(uri.path ?: ""),
                    arrayOf("image/png"),
                    null
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (uri != null) {
                resolver.delete(uri, null, null)
                uri = null
            }
        }
        return uri
    }

    fun shareMedia(context: Context, uri: Uri, mimeType: String? = null) {
        val resolvedType = mimeType
            ?: context.contentResolver.getType(uri)
            ?: if (uri.toString().contains("video", ignoreCase = true) || uri.toString().endsWith(".mp4", ignoreCase = true)) "video/mp4" else "image/png"

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = resolvedType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, "H.K NEWS वीडियो/फ़्रेम शेयर करें").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
