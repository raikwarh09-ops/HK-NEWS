package com.example.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.example.R
import com.example.data.model.NewsProject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object NewsVideoCompositor {

    /**
     * Composites the loaded/live video or studio animation WITH the full H.K NEWS broadcast frame
     * into an optimized MP4 video file and saves it directly to the phone's Video Gallery.
     */
    suspend fun compositeAndSaveVideo(
        context: Context,
        project: NewsProject,
        sourceVideoUri: Uri?,
        onProgress: (Float) -> Unit = {}
    ): Uri? = withContext(Dispatchers.IO) {
        val isFast = project.exportFastMode
        val (videoWidth, videoHeight) = when {
            isFast && project.exportResolution == "480p" -> 854 to 480
            isFast -> 854 to 480
            project.exportResolution == "1080p" -> 1920 to 1080
            project.exportResolution == "480p" -> 854 to 480
            else -> 1280 to 720
        }
        val frameRate = if (isFast) 12 else project.exportFps.coerceIn(12, 30)
        val bitRate = when (videoHeight) {
            1080 -> 4_200_000
            720 -> 2_500_000
            else -> 1_600_000
        }

        var retriever: MediaMetadataRetriever? = null
        var videoDurationMs = if (isFast) 5000L else 8000L

        if (sourceVideoUri != null) {
            try {
                retriever = MediaMetadataRetriever()
                retriever.setDataSource(context, sourceVideoUri)
                val durStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                if (durStr != null) {
                    val parsed = durStr.toLongOrNull() ?: 5000L
                    videoDurationMs = if (isFast) parsed.coerceIn(2000L, 8000L) else parsed.coerceIn(2000L, 20000L)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                retriever = null
            }
        }

        val totalFrames = ((videoDurationMs / 1000f) * frameRate).toInt().coerceIn(24, if (isFast) 72 else 150)
        val tempOutputFile = File(context.cacheDir, "hk_news_export_${System.currentTimeMillis()}.mp4")

        val success = encodeVideoWithFrame(
            context = context,
            project = project,
            retriever = retriever,
            totalFrames = totalFrames,
            videoDurationMs = videoDurationMs,
            videoWidth = videoWidth,
            videoHeight = videoHeight,
            frameRate = frameRate,
            bitRate = bitRate,
            outputFile = tempOutputFile,
            onProgress = onProgress
        )

        try {
            retriever?.release()
        } catch (_: Exception) {}

        if (success && tempOutputFile.exists() && tempOutputFile.length() > 1024) {
            val savedUri = saveFileToVideoGallery(context, tempOutputFile, project.headline)
            tempOutputFile.delete()
            return@withContext savedUri
        } else {
            tempOutputFile.delete()
            if (sourceVideoUri != null) {
                return@withContext GalleryMediaSaver.saveVideoToGallery(context, sourceVideoUri, project.headline)
            } else {
                return@withContext GalleryMediaSaver.saveNewsFrameToGallery(context, project)
            }
        }
    }

    private fun encodeVideoWithFrame(
        context: Context,
        project: NewsProject,
        retriever: MediaMetadataRetriever?,
        totalFrames: Int,
        videoDurationMs: Long,
        videoWidth: Int,
        videoHeight: Int,
        frameRate: Int,
        bitRate: Int,
        outputFile: File,
        onProgress: (Float) -> Unit
    ): Boolean {
        var codec: MediaCodec? = null
        var muxer: MediaMuxer? = null
        var isMuxerStarted = false
        var trackIndex = -1
        var samplesWritten = 0

        try {
            val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, videoWidth, videoHeight).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar)
                setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
                setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            }

            codec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
            codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            codec.start()

            muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

            val frameBitmap = Bitmap.createBitmap(videoWidth, videoHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(frameBitmap)
            val argbPixels = IntArray(videoWidth * videoHeight)
            val yuvBuffer = ByteArray(videoWidth * videoHeight * 3 / 2)

            val logoDrawable = ContextCompat.getDrawable(context, R.drawable.hk_news_logo)
            val logoSize = if (videoWidth <= 854) 110 else 160
            val logoBitmap = logoDrawable?.toBitmap(logoSize, logoSize)

            val bufferInfo = MediaCodec.BufferInfo()
            val timeStepUs = (videoDurationMs * 1000L) / totalFrames

            for (frameIndex in 0 until totalFrames) {
                val currentTimeUs = frameIndex * timeStepUs

                // 1. Draw video background frame or virtual news studio
                var bgFrame: Bitmap? = null
                if (retriever != null) {
                    try {
                        bgFrame = retriever.getFrameAtTime(currentTimeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                    } catch (_: Exception) {}
                }

                if (bgFrame != null) {
                    val progress = frameIndex.toFloat() / totalFrames.coerceAtLeast(1)
                    val zoomScale = if (project.panZoomLocked) {
                        project.panZoomStartScale
                    } else {
                        project.panZoomStartScale + (project.panZoomEndScale - project.panZoomStartScale) * progress
                    }.coerceIn(1.0f, 2.5f)

                    val cropW = (bgFrame.width / zoomScale).toInt().coerceAtLeast(1)
                    val cropH = (bgFrame.height / zoomScale).toInt().coerceAtLeast(1)
                    val cropX = ((bgFrame.width - cropW) / 2).coerceAtLeast(0)
                    val cropY = ((bgFrame.height - cropH) / 2).coerceAtLeast(0)
                    val srcRect = Rect(cropX, cropY, cropX + cropW, cropY + cropH)
                    val dstRect = Rect(0, 0, videoWidth, videoHeight)

                    val framePaint = if (project.videoContrast != 1.0f || project.videoBrightness != 0.0f) {
                        val contrastMatrix = android.graphics.ColorMatrix().apply {
                            val c = project.videoContrast
                            val b = project.videoBrightness * 255f
                            val t = (128f * (1f - c)) + b
                            set(floatArrayOf(
                                c, 0f, 0f, 0f, t,
                                0f, c, 0f, 0f, t,
                                0f, 0f, c, 0f, t,
                                0f, 0f, 0f, 1f, 0f
                            ))
                        }
                        Paint().apply {
                            colorFilter = android.graphics.ColorMatrixColorFilter(contrastMatrix)
                        }
                    } else null

                    canvas.drawBitmap(bgFrame, srcRect, dstRect, framePaint)
                    bgFrame.recycle()
                } else {
                    drawStudioBackdrop(canvas, frameIndex, totalFrames, videoWidth, videoHeight)
                }

                // 2. Draw the complete H.K NEWS Broadcast Frame Overlays (with finger drag offsets)
                drawBroadcastOverlays(
                    canvas = canvas,
                    project = project,
                    logoBitmap = logoBitmap,
                    frameIndex = frameIndex,
                    totalFrames = totalFrames,
                    videoWidth = videoWidth,
                    videoHeight = videoHeight
                )

                // 3. Convert ARGB to NV12 YUV420SemiPlanar
                frameBitmap.getPixels(argbPixels, 0, videoWidth, 0, 0, videoWidth, videoHeight)
                encodeYUV420SP(yuvBuffer, argbPixels, videoWidth, videoHeight)

                // 4. Feed input buffer to MediaCodec
                val isLastFrame = (frameIndex == totalFrames - 1)
                var inputBufferIndex = codec.dequeueInputBuffer(15000)
                while (inputBufferIndex < 0 && !isLastFrame) {
                    val res = drainCodec(codec, muxer, bufferInfo, isMuxerStarted, trackIndex, false)
                    isMuxerStarted = res.first
                    trackIndex = res.second
                    samplesWritten += res.third
                    inputBufferIndex = codec.dequeueInputBuffer(15000)
                }

                if (inputBufferIndex >= 0) {
                    val inputBuffer = codec.getInputBuffer(inputBufferIndex)
                    inputBuffer?.clear()
                    inputBuffer?.put(yuvBuffer)
                    val ptsUs = (frameIndex * 1_000_000L) / frameRate
                    val flags = if (isLastFrame) MediaCodec.BUFFER_FLAG_END_OF_STREAM else 0
                    codec.queueInputBuffer(inputBufferIndex, 0, yuvBuffer.size, ptsUs, flags)
                }

                // 5. Drain encoded data into MediaMuxer
                val res = drainCodec(codec, muxer, bufferInfo, isMuxerStarted, trackIndex, false)
                isMuxerStarted = res.first
                trackIndex = res.second
                samplesWritten += res.third

                onProgress(0.1f + (frameIndex.toFloat() / totalFrames) * 0.85f)
            }

            // Drain remaining frames until EOS
            val finalRes = drainCodec(codec, muxer, bufferInfo, isMuxerStarted, trackIndex, true)
            isMuxerStarted = finalRes.first
            trackIndex = finalRes.second
            samplesWritten += finalRes.third

            frameBitmap.recycle()
            return (samplesWritten > 0)
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            try {
                codec?.stop()
                codec?.release()
            } catch (_: Exception) {}
            try {
                if (isMuxerStarted && samplesWritten > 0) {
                    muxer?.stop()
                }
                muxer?.release()
            } catch (_: Exception) {}
        }
    }

    private fun drainCodec(
        codec: MediaCodec,
        muxer: MediaMuxer,
        bufferInfo: MediaCodec.BufferInfo,
        muxerStarted: Boolean,
        currentTrackIndex: Int,
        endOfStream: Boolean
    ): Triple<Boolean, Int, Int> {
        var isStarted = muxerStarted
        var trackIdx = currentTrackIndex
        var samplesAdded = 0

        while (true) {
            val outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 10000)
            if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                if (!endOfStream) break
            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                if (!isStarted) {
                    val newFormat = codec.outputFormat
                    trackIdx = muxer.addTrack(newFormat)
                    muxer.start()
                    isStarted = true
                }
            } else if (outputBufferIndex >= 0) {
                val encodedData = codec.getOutputBuffer(outputBufferIndex)
                if (encodedData != null && isStarted && trackIdx >= 0) {
                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                        bufferInfo.size = 0
                    }
                    if (bufferInfo.size > 0) {
                        encodedData.position(bufferInfo.offset)
                        encodedData.limit(bufferInfo.offset + bufferInfo.size)
                        muxer.writeSampleData(trackIdx, encodedData, bufferInfo)
                        samplesAdded++
                    }
                }
                codec.releaseOutputBuffer(outputBufferIndex, false)
                if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    break
                }
            }
        }
        return Triple(isStarted, trackIdx, samplesAdded)
    }

    private fun drawStudioBackdrop(canvas: Canvas, frameIndex: Int, totalFrames: Int, width: Int, height: Int) {
        val bgPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                android.graphics.Color.parseColor("#060A16"),
                android.graphics.Color.parseColor("#101935"),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        val lightX = (width * (frameIndex.toFloat() / totalFrames))
        val lightPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#15244A")
            alpha = 80
        }
        canvas.drawCircle(lightX, height * 0.35f, width * 0.22f, lightPaint)
    }

    private fun drawBroadcastOverlays(
        canvas: Canvas,
        project: NewsProject,
        logoBitmap: Bitmap?,
        frameIndex: Int,
        totalFrames: Int,
        videoWidth: Int,
        videoHeight: Int
    ) {
        val scale = videoWidth / 400f
        val themeColor = try {
            android.graphics.Color.parseColor(project.themeColorHex)
        } catch (_: Exception) {
            android.graphics.Color.parseColor("#C62828")
        }
        val themePaint = Paint().apply { color = themeColor }

        // ================= 1. CHANNEL LOGO (with Finger Drag Offset) =================
        if (project.showLogo && logoBitmap != null) {
            val baseLogoSize = if (videoWidth <= 854) 84f else 115f
            val baseLogoX = if (project.logoPosition.contains("LEFT")) 20f else (videoWidth - baseLogoSize - 20f)
            val baseLogoY = if (project.logoPosition.contains("BOTTOM")) (videoHeight - 200f) else 18f

            val logoX = baseLogoX + (project.logoOffsetX * (videoWidth / 360f))
            val logoY = baseLogoY + (project.logoOffsetY * (videoHeight / 640f))

            val logoCard = RectF(logoX, logoY, logoX + baseLogoSize, logoY + baseLogoSize)
            val cardBg = Paint().apply {
                color = android.graphics.Color.parseColor("#0D47A1")
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(logoCard, 10f, 10f, cardBg)

            val goldBorder = Paint().apply {
                color = android.graphics.Color.parseColor("#FFD54F")
                style = Paint.Style.STROKE
                strokeWidth = 2.5f
            }
            canvas.drawRoundRect(logoCard, 10f, 10f, goldBorder)

            val dstLogo = RectF(logoX + 3f, logoY + 3f, logoX + baseLogoSize - 3f, logoY + baseLogoSize - 3f)
            canvas.drawBitmap(logoBitmap, null, dstLogo, null)

            // Animated Shine Sweep over logo
            if (project.logoAnimation == "SHINE_SWEEP") {
                val shinePhase = (frameIndex % 30) / 30f
                val sweepX = logoX - 25f + ((baseLogoSize + 50f) * shinePhase)
                val shinePaint = Paint().apply {
                    shader = LinearGradient(
                        sweepX, logoY, sweepX + 30f, logoY + baseLogoSize,
                        intArrayOf(0x00FFFFFF, 0x88FFFFFF.toInt(), 0xAAFFD54F.toInt(), 0x00FFFFFF),
                        null,
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRoundRect(logoCard, 10f, 10f, shinePaint)
            }
        }

        // ================= 2. LIVE BADGE (with Finger Drag Offset) =================
        if (project.showLiveBadge) {
            val baseLiveX = if (project.liveBadgePosition == "TOP_RIGHT") {
                (videoWidth - 130f)
            } else if (project.logoPosition == "TOP_LEFT") {
                (if (videoWidth <= 854) 120f else 155f)
            } else {
                20f
            }
            val baseLiveY = 22f

            val liveX = baseLiveX + (project.liveBadgeOffsetX * (videoWidth / 360f))
            val liveY = baseLiveY + (project.liveBadgeOffsetY * (videoHeight / 640f))
            val liveScale = project.liveBadgeScale.coerceIn(0.7f, 1.6f)
            val liveW = (if (videoWidth <= 854) 75f else 90f) * liveScale
            val liveH = (if (videoWidth <= 854) 34f else 42f) * liveScale

            val livePaint = Paint().apply {
                color = android.graphics.Color.RED
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(RectF(liveX, liveY, liveX + liveW, liveY + liveH), 5f, 5f, livePaint)

            val blink = (frameIndex % 15) < 8
            if (blink) {
                val dotPaint = Paint().apply { color = android.graphics.Color.WHITE }
                canvas.drawCircle(liveX + 14f, liveY + (liveH / 2f), 4f, dotPaint)
            }

            val liveText = Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = (if (videoWidth <= 854) 16f else 20f) * liveScale
                isFakeBoldText = true
                isAntiAlias = true
            }
            canvas.drawText(project.liveBadgeText.ifBlank { "LIVE" }.take(10), liveX + 24f, liveY + (liveH * 0.68f), liveText)
        }

        // ================= 3. LOCATION CHIP (with Finger Drag Offset) =================
        if (project.showLocation && project.location.isNotBlank()) {
            val chipW = if (videoWidth <= 854) 180f else 240f
            val baseLocX = videoWidth - chipW - 20f
            val baseLocY = 22f

            val locX = baseLocX + (project.locationOffsetX * (videoWidth / 360f))
            val locY = baseLocY + (project.locationOffsetY * (videoHeight / 640f))
            val locH = if (videoWidth <= 854) 34f else 42f

            val locPaint = Paint().apply {
                color = android.graphics.Color.parseColor("#0F172A")
            }
            canvas.drawRoundRect(RectF(locX, locY, locX + chipW, locY + locH), 5f, 5f, locPaint)

            val locBorder = Paint().apply {
                color = android.graphics.Color.parseColor("#FFD54F")
                style = Paint.Style.STROKE
                strokeWidth = 1.5f
            }
            canvas.drawRoundRect(RectF(locX, locY, locX + chipW, locY + locH), 5f, 5f, locBorder)

            val locText = Paint().apply {
                color = android.graphics.Color.parseColor("#FFD54F")
                textSize = if (videoWidth <= 854) 15f else 18f
                isFakeBoldText = true
                isAntiAlias = true
            }
            canvas.drawText("📍 ${project.location}", locX + 12f, locY + (locH * 0.68f), locText)
        }

        // ================= 4. REPORTER LOWER THIRD (with Finger Drag Offset) =================
        if (project.showReporterCard && project.reporterName.isNotBlank()) {
            val repScale = project.reporterScale.coerceIn(0.7f, 1.6f)
            val repW = (if (videoWidth <= 854) 320f else 440f) * repScale
            val repH = (if (videoWidth <= 854) 52f else 66f) * repScale
            val baseRepY = videoHeight - (if (videoWidth <= 854) 170f else 225f)
            val baseRepX = if (project.reporterPosition == "BOTTOM_RIGHT") (videoWidth - repW - 20f) else 20f

            val repX = baseRepX + (project.reporterOffsetX * (videoWidth / 360f))
            val repY = baseRepY + (project.reporterOffsetY * (videoHeight / 640f))

            val repCard = RectF(repX, repY, repX + repW, repY + repH)
            val repBg = Paint().apply {
                color = android.graphics.Color.parseColor("#0F172A")
            }
            canvas.drawRoundRect(repCard, 6f, 6f, repBg)

            canvas.drawRect(repX, repY, repX + 8f, repY + repH, themePaint)

            val repNamePaint = Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = (if (videoWidth <= 854) 19f else 24f) * repScale
                isFakeBoldText = true
                isAntiAlias = true
            }
            canvas.drawText(project.reporterName, repX + 18f, repY + (repH * 0.48f), repNamePaint)

            val repRolePaint = Paint().apply {
                color = android.graphics.Color.parseColor("#FFD54F")
                textSize = (if (videoWidth <= 854) 13f else 16f) * repScale
                isAntiAlias = true
            }
            canvas.drawText("${project.reporterRole} • ${project.location}", repX + 18f, repY + (repH * 0.85f), repRolePaint)
        }

        // ================= 5 & 6. BREAKING NEWS RIBBON + ANIMATED HEADLINE CARD =================
        if (project.showHeadline) {
            val headScale = project.headlineScale.coerceIn(0.7f, 1.6f)
            val ribbonH = (if (videoWidth <= 854) 30f else 38f) * headScale
            val ribbonY = videoHeight - (if (videoWidth <= 854) 115f else 158f) + (project.headlineOffsetY * (videoHeight / 640f))
            val ribbonW = (if (videoWidth <= 854) 210f else 280f) * headScale
            canvas.drawRect(0f, ribbonY, ribbonW, ribbonY + ribbonH, themePaint)

            val ribbonText = Paint().apply {
                color = android.graphics.Color.parseColor("#FFD54F")
                textSize = (if (videoWidth <= 854) 16f else 20f) * headScale
                isFakeBoldText = true
                isAntiAlias = true
            }
            canvas.drawText(project.category.uppercase(), 16f, ribbonY + (ribbonH * 0.7f), ribbonText)

            val headH = (if (videoWidth <= 854) 58f else 76f) * headScale
            val baseHeadY = videoHeight - (if (videoWidth <= 854) 86f else 120f)
            val headY = baseHeadY + (project.headlineOffsetY * (videoHeight / 640f))
            val headX = (project.headlineOffsetX * (videoWidth / 360f))

            val headBg = Paint().apply {
                color = android.graphics.Color.parseColor("#0B132B")
            }
            canvas.drawRect(0f, headY, videoWidth.toFloat(), headY + headH, headBg)

            // Headline Animation effects in exported video
            val headPhase = (frameIndex % 36) / 36f
            if (project.headlineAnimation == "SHINE_SWEEP" || project.headlineAnimation == "SLIDE_REVEAL") {
                val sweepStart = -120f + (videoWidth + 240f) * headPhase
                val sweepPaint = Paint().apply {
                    shader = LinearGradient(
                        sweepStart, headY, sweepStart + 140f, headY + headH,
                        intArrayOf(0x00FFFFFF, 0x44FFD54F, 0x00FFFFFF),
                        null,
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRect(0f, headY, videoWidth.toFloat(), headY + headH, sweepPaint)
            }

            val lineFlash = (frameIndex % 16) < 8
            val goldLine = Paint().apply {
                color = when {
                    project.headlineAnimation == "NEON_FLASH" && lineFlash -> android.graphics.Color.CYAN
                    project.headlineAnimation == "PULSE_GLOW" && lineFlash -> themeColor
                    else -> android.graphics.Color.parseColor("#FFD54F")
                }
                strokeWidth = if (project.headlineAnimation != "NONE") 3.5f else 2.5f
            }
            canvas.drawLine(0f, headY, videoWidth.toFloat(), headY, goldLine)

            val headText = Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = (if (videoWidth <= 854) 22f else 30f) * headScale
                isFakeBoldText = true
                isAntiAlias = true
            }
            canvas.drawText(project.headline.take(45), headX + 18f, headY + (headH * 0.5f), headText)

            if (project.subHeadline.isNotBlank()) {
                val subText = Paint().apply {
                    color = android.graphics.Color.parseColor("#FFCA28")
                    textSize = (if (videoWidth <= 854) 14f else 19f) * headScale
                    isAntiAlias = true
                }
                canvas.drawText(project.subHeadline.take(65), headX + 18f, headY + (headH * 0.86f), subText)
            }
        }

        // ================= 7. "ताज़ा अपडेट" TAG & ANIMATED SCROLLING TICKER =================
        if (project.showTicker) {
            val tickerScale = project.tickerScale.coerceIn(0.7f, 1.6f)
            val tickerH = (if (videoWidth <= 854) 30f else 38f) * tickerScale
            val baseTickerY = videoHeight - tickerH
            val tickerY = baseTickerY + (project.tickerOffsetY * (videoHeight / 640f))
            val tickerX = (project.tickerOffsetX * (videoWidth / 360f))

            // Tag placed ABOVE the running line (चलने वाली लाइन के ऊपर)
            val tagHeight = (if (videoWidth <= 854) 22f else 28f) * tickerScale
            val tagY = tickerY - tagHeight
            val tagWidth = (if (videoWidth <= 854) 150f else 195f) * tickerScale
            val tagCard = RectF(tickerX, tagY, tickerX + tagWidth, tickerY)
            val flashToggle = (frameIndex % 14) < 7
            val tagBg = Paint().apply {
                shader = if (project.tickerAnimation == "FLASH_TAG" && flashToggle) {
                    LinearGradient(
                        tickerX, tagY, tickerX + tagWidth, tickerY,
                        themeColor,
                        android.graphics.Color.RED,
                        Shader.TileMode.CLAMP
                    )
                } else {
                    LinearGradient(
                        tickerX, tagY, tickerX + tagWidth, tickerY,
                        android.graphics.Color.parseColor("#FFD54F"),
                        android.graphics.Color.parseColor("#FF8F00"),
                        Shader.TileMode.CLAMP
                    )
                }
            }
            canvas.drawRoundRect(tagCard, 5f, 5f, tagBg)

            val tagBorder = Paint().apply {
                color = android.graphics.Color.WHITE
                style = Paint.Style.STROKE
                strokeWidth = 1.2f
            }
            canvas.drawRoundRect(tagCard, 5f, 5f, tagBorder)

            val redDot = Paint().apply { color = android.graphics.Color.RED }
            canvas.drawCircle(tickerX + 14f, tagY + (tagHeight / 2f), 4f, redDot)

            val tagTextPaint = Paint().apply {
                color = if (project.tickerAnimation == "FLASH_TAG" && flashToggle) android.graphics.Color.WHITE else android.graphics.Color.BLACK
                textSize = (if (videoWidth <= 854) 13f else 16f) * tickerScale
                isFakeBoldText = true
                isAntiAlias = true
            }
            val cleanTag = project.tickerTagText.ifBlank { "ताज़ा अपडेट" }.take(18)
            canvas.drawText(cleanTag, tickerX + 24f, tagY + (tagHeight * 0.72f), tagTextPaint)

            // Running Ticker Line (चलने वाली लाइन) - Full Width
            val tickerBg = Paint().apply {
                color = android.graphics.Color.parseColor("#0C1427")
            }
            canvas.drawRect(0f, tickerY, videoWidth.toFloat(), tickerY + tickerH, tickerBg)

            if (project.tickerAnimation == "BREAKING_SWEEP" || project.tickerAnimation == "WAVE_GLOW") {
                val tPhase = (frameIndex % 28) / 28f
                val sweepStart = -100f + (videoWidth + 200f) * tPhase
                val tickerSweep = Paint().apply {
                    shader = LinearGradient(
                        sweepStart, tickerY, sweepStart + 120f, tickerY + tickerH,
                        intArrayOf(0x00FFFFFF, 0x38FFD54F, 0x00FFFFFF),
                        null,
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRect(0f, tickerY, videoWidth.toFloat(), tickerY + tickerH, tickerSweep)
            }

            val tickerTopLine = Paint().apply {
                color = if (project.tickerAnimation == "PULSE_BORDER" && flashToggle) themeColor else android.graphics.Color.parseColor("#FFD54F")
                strokeWidth = if (project.tickerAnimation == "PULSE_BORDER") 2.6f else 1.8f
            }
            canvas.drawLine(0f, tickerY, videoWidth.toFloat(), tickerY, tickerTopLine)

            // Moving Ticker Text (respects manual tickerSpeed)
            val textPaint = Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = (if (videoWidth <= 854) 16f else 20f) * tickerScale
                isFakeBoldText = true
                isAntiAlias = true
            }
            val tickerFull = "${project.tickerText}      ★      ${project.channelName}      ★      ${project.helplineNumber}"
            val textWidth = textPaint.measureText(tickerFull)
            val baseSpeed = if (videoWidth <= 854) 6.5f else 8f
            val speed = baseSpeed * project.tickerSpeed.coerceIn(0.4f, 2.5f)
            val scrollOffset = (frameIndex * speed) % (textWidth + videoWidth)
            val xPos = (videoWidth - scrollOffset)

            canvas.drawText(tickerFull, xPos, tickerY + (tickerH * 0.7f), textPaint)
        }
    }

    private fun encodeYUV420SP(yuv420sp: ByteArray, argb: IntArray, width: Int, height: Int) {
        val frameSize = width * height
        var yIndex = 0
        var uvIndex = frameSize
        var index = 0
        for (j in 0 until height) {
            for (i in 0 until width) {
                val c = argb[index++]
                val r = (c shr 16) and 0xff
                val g = (c shr 8) and 0xff
                val b = c and 0xff

                var y = (66 * r + 129 * g + 25 * b + 128 shr 8) + 16
                var u = (-38 * r - 74 * g + 112 * b + 128 shr 8) + 128
                var v = (112 * r - 94 * g - 18 * b + 128 shr 8) + 128

                yuv420sp[yIndex++] = y.coerceIn(0, 255).toByte()
                if (j % 2 == 0 && i % 2 == 0) {
                    yuv420sp[uvIndex++] = u.coerceIn(0, 255).toByte()
                    yuv420sp[uvIndex++] = v.coerceIn(0, 255).toByte()
                }
            }
        }
    }

    private fun saveFileToVideoGallery(context: Context, sourceFile: File, title: String): Uri? {
        val resolver = context.contentResolver
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "HK_NEWS_VIDEO_${timeStamp}.mp4"

        val values = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.TITLE, title)
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
                    FileInputStream(sourceFile).use { inStream ->
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
        return targetUri
    }
}
