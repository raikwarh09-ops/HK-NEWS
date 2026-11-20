package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.media.Image
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import android.net.Uri
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.example.R
import com.example.data.model.NewsProject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import kotlin.coroutines.coroutineContext

object NewsVideoCompositor {

    const val MAX_VIDEO_DURATION_MS = 7_200_000L // 2 Hours (120 Minutes)

    internal data class AdaptiveExportProfile(
        val frameRate: Int,
        val iFrameIntervalSec: Int,
        val minSampleStepUs: Long,
        val bitRate: Int
    )

    internal fun resolveAdaptiveExportProfile(
        trimmedDurationMs: Long,
        isFast: Boolean,
        requestedFps: Int,
        videoWidth: Int,
        videoHeight: Int
    ): AdaptiveExportProfile {
        val pixels = videoWidth * videoHeight
        val baseBitRate = when {
            pixels >= 1920 * 1080 -> 4_000_000
            pixels >= 1280 * 720 -> 2_400_000
            else -> 1_400_000
        }
        return when {
            trimmedDurationMs > 30 * 60_000L -> AdaptiveExportProfile(
                frameRate = if (isFast) 2 else 4,
                iFrameIntervalSec = 2,
                minSampleStepUs = 1_000_000L,
                bitRate = (baseBitRate * 0.55f).toInt().coerceAtLeast(700_000)
            )
            trimmedDurationMs > 5 * 60_000L -> AdaptiveExportProfile(
                frameRate = if (isFast) 6 else requestedFps.coerceIn(6, 12),
                iFrameIntervalSec = 2,
                minSampleStepUs = 500_000L,
                bitRate = (baseBitRate * 0.75f).toInt().coerceAtLeast(900_000)
            )
            trimmedDurationMs > 60_000L -> AdaptiveExportProfile(
                frameRate = if (isFast) 10 else requestedFps.coerceIn(10, 20),
                iFrameIntervalSec = 1,
                minSampleStepUs = 150_000L,
                bitRate = baseBitRate
            )
            else -> AdaptiveExportProfile(
                frameRate = if (isFast) 12 else requestedFps.coerceIn(12, 30),
                iFrameIntervalSec = 1,
                minSampleStepUs = 0L,
                bitRate = baseBitRate
            )
        }
    }

    private class FrameSourceCache(val minSampleStepUs: Long) {
        var lastFetchedTimeUs: Long = Long.MIN_VALUE
        var lastBitmap: Bitmap? = null

        fun getFrame(retriever: MediaMetadataRetriever, timeUs: Long): Bitmap? {
            if (lastBitmap != null && !lastBitmap!!.isRecycled && minSampleStepUs > 0L &&
                kotlin.math.abs(timeUs - lastFetchedTimeUs) < minSampleStepUs
            ) {
                return lastBitmap
            }
            val fresh = try {
                retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            } catch (_: Exception) {
                null
            }
            if (fresh != null) {
                if (lastBitmap != null && lastBitmap !== fresh && !lastBitmap!!.isRecycled) {
                    lastBitmap?.recycle()
                }
                lastBitmap = fresh
                lastFetchedTimeUs = timeUs
                return fresh
            }
            return lastBitmap?.takeIf { !it.isRecycled }
        }

        fun release() {
            try {
                if (lastBitmap != null && !lastBitmap!!.isRecycled) {
                    lastBitmap?.recycle()
                }
            } catch (_: Exception) {}
            lastBitmap = null
        }
    }

    /**
     * Composites the loaded/live video, gallery image, or studio animation WITH the full H.K NEWS
     * broadcast frame into a standard MP4 video file (up to 2 hours) and saves it directly to the phone's Video Gallery.
     */
    suspend fun compositeAndSaveVideo(
        context: Context,
        project: NewsProject,
        sourceVideoUri: Uri?,
        sourceImageUri: Uri? = null,
        splitPointRatio: Float? = null,
        onProgress: (Float) -> Unit = {}
    ): Uri? = withContext(Dispatchers.IO) {
        val isFast = project.exportFastMode
        val (videoWidth, videoHeight) = resolveAlignedDimensions(
            aspectPreset = project.aspectPreset,
            resolution = if (isFast) "480p" else project.exportResolution
        )

        var retriever: MediaMetadataRetriever? = null
        var videoDurationMs = project.videoTotalDurationMs.coerceIn(2000L, MAX_VIDEO_DURATION_MS)

        if (sourceVideoUri != null) {
            try {
                retriever = MediaMetadataRetriever()
                if (sourceVideoUri.scheme == "file" && !sourceVideoUri.path.isNullOrBlank()) {
                    retriever.setDataSource(sourceVideoUri.path)
                } else {
                    retriever.setDataSource(context, sourceVideoUri)
                }
                val durStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                val parsed = durStr?.toLongOrNull()
                if (parsed != null && parsed > 0L) {
                    videoDurationMs = parsed.coerceIn(2000L, MAX_VIDEO_DURATION_MS)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                try { retriever?.release() } catch (_: Exception) {}
                retriever = null
            }
        }

        val sourceImageBitmap: Bitmap? = if (retriever == null) {
            val fallbackImgUri = sourceImageUri ?: sourceVideoUri
            if (fallbackImgUri != null) loadBitmapFromUri(context, fallbackImgUri) else null
        } else null

        val trimStart = project.videoTrimStart.coerceIn(0f, 0.95f)
        val trimEnd = project.videoTrimEnd.coerceIn(trimStart + 0.02f, 1.0f)
        val speedFactor = project.videoSpeed.coerceIn(0.25f, 3.0f)
        val trimmedDurationMs = (((videoDurationMs * (trimEnd - trimStart)) / speedFactor).toLong()).coerceIn(2000L, MAX_VIDEO_DURATION_MS)
        val profile = resolveAdaptiveExportProfile(
            trimmedDurationMs = trimmedDurationMs,
            isFast = isFast,
            requestedFps = project.exportFps,
            videoWidth = videoWidth,
            videoHeight = videoHeight
        )
        val frameRate = profile.frameRate
        val bitRate = profile.bitRate
        val totalFrames = ((trimmedDurationMs / 1000.0) * frameRate).toInt().coerceAtLeast(24)
        val rawVideoFile = File(context.cacheDir, "hk_news_raw_${System.currentTimeMillis()}.mp4")
        val finalMuxedFile = File(context.cacheDir, "hk_news_export_${System.currentTimeMillis()}.mp4")
        val scopeActiveCheck = { coroutineContext.isActive }

        var encodedOk = encodeVideoWithFallbackCodecs(
            context = context,
            project = project,
            retriever = retriever,
            sourceImageBitmap = sourceImageBitmap,
            totalFrames = totalFrames,
            videoDurationMs = videoDurationMs,
            videoWidth = videoWidth,
            videoHeight = videoHeight,
            frameRate = frameRate,
            iFrameIntervalSec = profile.iFrameIntervalSec,
            minSampleStepUs = profile.minSampleStepUs,
            bitRate = bitRate,
            splitPointRatio = splitPointRatio,
            outputFile = rawVideoFile,
            isActiveCheck = scopeActiveCheck,
            onProgress = onProgress
        )

        // Safe 480p / 12fps codec retry if high-res encoder failed
        if (!encodedOk && coroutineContext.isActive && !isFast) {
            val (safeW, safeH) = resolveAlignedDimensions(project.aspectPreset, "480p")
            val safeFps = minOf(frameRate, 12)
            val safeTotalFrames = ((trimmedDurationMs / 1000.0) * safeFps).toInt().coerceAtLeast(24)
            encodedOk = encodeVideoWithFallbackCodecs(
                context = context,
                project = project,
                retriever = retriever,
                sourceImageBitmap = sourceImageBitmap,
                totalFrames = safeTotalFrames,
                videoDurationMs = videoDurationMs,
                videoWidth = safeW,
                videoHeight = safeH,
                frameRate = safeFps,
                iFrameIntervalSec = 2,
                minSampleStepUs = profile.minSampleStepUs,
                bitRate = 1_200_000,
                splitPointRatio = splitPointRatio,
                outputFile = rawVideoFile,
                isActiveCheck = scopeActiveCheck,
                onProgress = onProgress
            )
        }

        try { retriever?.release() } catch (_: Exception) {}
        try { sourceImageBitmap?.recycle() } catch (_: Exception) {}

        if (encodedOk && coroutineContext.isActive && rawVideoFile.exists() && rawVideoFile.length() > 1024L) {
            val trimStartMs = (videoDurationMs * trimStart).toLong()
            val trimEndMs = (videoDurationMs * trimEnd).toLong()
            val speedChanged = kotlin.math.abs(speedFactor - 1.0f) > 0.03f
            val isMuted = project.videoVolume <= 0.02f || project.audioVolumeBoost <= 0.02f || project.vnReversePlayback
            val needsAudioDsp = !isMuted && trimmedDurationMs <= 10 * 60_000L && (
                project.voiceChangerEffect != "NORMAL" ||
                    kotlin.math.abs(project.audioPitch - 1.0f) > 0.03f ||
                    kotlin.math.abs(project.audioVolumeBoost - 1.0f) > 0.05f ||
                    project.videoVolume < 0.96f ||
                    project.audioFadeIn ||
                    project.audioFadeOut ||
                    speedChanged
                )
            val audioMuxedOk = if (sourceVideoUri != null && !isMuted) {
                (if (needsAudioDsp) {
                    tryProcessAndMuxAudio(
                        context = context,
                        videoFile = rawVideoFile,
                        sourceAudioUri = sourceVideoUri,
                        outputFile = finalMuxedFile,
                        trimStartMs = trimStartMs,
                        trimEndMs = trimEndMs,
                        trimmedDurationMs = trimmedDurationMs,
                        speedFactor = speedFactor,
                        project = project
                    )
                } else false) || (!speedChanged && tryMuxWithSourceAudio(
                    context = context,
                    videoFile = rawVideoFile,
                    sourceAudioUri = sourceVideoUri,
                    outputFile = finalMuxedFile,
                    trimStartMs = trimStartMs,
                    trimEndMs = trimEndMs,
                    trimmedDurationMs = trimmedDurationMs
                ))
            } else false

            val fileToSave = if (audioMuxedOk && finalMuxedFile.exists() && finalMuxedFile.length() > 1024L) {
                rawVideoFile.delete()
                finalMuxedFile
            } else {
                finalMuxedFile.delete()
                rawVideoFile
            }

            val savedUri = GalleryMediaSaver.saveLocalFileToVideoGallery(
                context = context,
                sourceFile = fileToSave,
                title = project.headline.ifBlank { "HK NEWS" }
            )
            rawVideoFile.delete()
            finalMuxedFile.delete()
            return@withContext savedUri
        } else {
            rawVideoFile.delete()
            finalMuxedFile.delete()
            if (coroutineContext.isActive && sourceVideoUri != null) {
                return@withContext GalleryMediaSaver.saveVideoToGallery(context, sourceVideoUri, project.headline)
            }
            return@withContext null
        }
    }

    private fun resolveAlignedDimensions(aspectPreset: String, resolution: String): Pair<Int, Int> {
        val raw = when (aspectPreset) {
            "9:16" -> when (resolution) {
                "1080p" -> 1088 to 1920
                "720p" -> 720 to 1280
                else -> 480 to 864
            }
            "1:1" -> when (resolution) {
                "1080p" -> 1088 to 1088
                "720p" -> 720 to 720
                else -> 480 to 480
            }
            "4:5" -> when (resolution) {
                "1080p" -> 864 to 1088
                "720p" -> 640 to 800
                else -> 480 to 608
            }
            else -> when (resolution) {
                "1080p" -> 1920 to 1088
                "720p" -> 1280 to 720
                else -> 864 to 480
            }
        }
        // Guarantee multiples of 16 for universal MediaCodec H.264 compatibility
        val w = (raw.first / 16) * 16
        val h = (raw.second / 16) * 16
        return w.coerceAtLeast(320) to h.coerceAtLeast(320)
    }

    private fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && uri.scheme != "file") {
                val src = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(src) { decoder, info, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    val maxDim = 1280
                    if (info.size.width > maxDim || info.size.height > maxDim) {
                        val ratio = minOf(maxDim.toFloat() / info.size.width, maxDim.toFloat() / info.size.height)
                        decoder.setTargetSize(
                            (info.size.width * ratio).toInt().coerceAtLeast(1),
                            (info.size.height * ratio).toInt().coerceAtLeast(1)
                        )
                    }
                }
            } else {
                val stream = if (uri.scheme == "file" && !uri.path.isNullOrBlank()) {
                    File(uri.path!!).inputStream()
                } else {
                    context.contentResolver.openInputStream(uri)
                }
                stream?.use { BitmapFactory.decodeStream(it) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private data class EncoderCandidate(
        val codecName: String?,
        val colorFormat: Int,
        val useSurfaceApi: Boolean = false,
        val useImageApi: Boolean = false
    )

    private fun buildEncoderCandidates(): List<EncoderCandidate> {
        val candidates = mutableListOf<EncoderCandidate>()
        try {
            val list = MediaCodecList(MediaCodecList.REGULAR_CODECS)
            val avcInfos = list.codecInfos.filter { info ->
                info.isEncoder && info.supportedTypes.any { it.equals(MediaFormat.MIMETYPE_VIDEO_AVC, ignoreCase = true) }
            }
            // Prefer hardware encoders first, then software encoders (e.g. c2.android.avc.encoder / OMX.google.h264.encoder)
            val sortedInfos = avcInfos.sortedBy { info ->
                val n = info.name.lowercase()
                if (n.startsWith("omx.google.") || n.startsWith("c2.android.") || n.contains("sw")) 1 else 0
            }
            for (info in sortedInfos) {
                val caps = try {
                    info.getCapabilitiesForType(MediaFormat.MIMETYPE_VIDEO_AVC)
                } catch (_: Exception) { null } ?: continue

                val formats = caps.colorFormats
                if (formats.contains(MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)) {
                    candidates.add(EncoderCandidate(info.name, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface, useSurfaceApi = true))
                }
                if (formats.contains(MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible)) {
                    candidates.add(EncoderCandidate(info.name, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible, useSurfaceApi = false, useImageApi = true))
                }
                if (formats.contains(MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar)) {
                    candidates.add(EncoderCandidate(info.name, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar, useSurfaceApi = false, useImageApi = false))
                }
                if (formats.contains(MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar)) {
                    candidates.add(EncoderCandidate(info.name, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar, useSurfaceApi = false, useImageApi = false))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Guaranteed generic fallbacks if list inspection yielded nothing
        if (candidates.isEmpty()) {
            candidates.add(EncoderCandidate(null, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface, useSurfaceApi = true))
            candidates.add(EncoderCandidate(null, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible, useSurfaceApi = false, useImageApi = true))
            candidates.add(EncoderCandidate(null, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar, useSurfaceApi = false, useImageApi = false))
            candidates.add(EncoderCandidate(null, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar, useSurfaceApi = false, useImageApi = false))
        }
        return candidates.distinct()
    }

    private fun encodeVideoWithFallbackCodecs(
        context: Context,
        project: NewsProject,
        retriever: MediaMetadataRetriever?,
        sourceImageBitmap: Bitmap?,
        totalFrames: Int,
        videoDurationMs: Long,
        videoWidth: Int,
        videoHeight: Int,
        frameRate: Int,
        iFrameIntervalSec: Int,
        minSampleStepUs: Long,
        bitRate: Int,
        splitPointRatio: Float?,
        outputFile: File,
        isActiveCheck: () -> Boolean,
        onProgress: (Float) -> Unit
    ): Boolean {
        val candidates = buildEncoderCandidates()
        for (candidate in candidates) {
            if (!isActiveCheck()) return false
            if (outputFile.exists()) outputFile.delete()
            val ok = if (candidate.useSurfaceApi) {
                encodeVideoWithSurface(
                    context = context,
                    project = project,
                    retriever = retriever,
                    sourceImageBitmap = sourceImageBitmap,
                    totalFrames = totalFrames,
                    videoDurationMs = videoDurationMs,
                    videoWidth = videoWidth,
                    videoHeight = videoHeight,
                    frameRate = frameRate,
                    iFrameIntervalSec = iFrameIntervalSec,
                    minSampleStepUs = minSampleStepUs,
                    bitRate = bitRate,
                    splitPointRatio = splitPointRatio,
                    candidate = candidate,
                    outputFile = outputFile,
                    isActiveCheck = isActiveCheck,
                    onProgress = onProgress
                )
            } else {
                encodeVideoWithFrame(
                    context = context,
                    project = project,
                    retriever = retriever,
                    sourceImageBitmap = sourceImageBitmap,
                    totalFrames = totalFrames,
                    videoDurationMs = videoDurationMs,
                    videoWidth = videoWidth,
                    videoHeight = videoHeight,
                    frameRate = frameRate,
                    iFrameIntervalSec = iFrameIntervalSec,
                    minSampleStepUs = minSampleStepUs,
                    bitRate = bitRate,
                    splitPointRatio = splitPointRatio,
                    candidate = candidate,
                    outputFile = outputFile,
                    isActiveCheck = isActiveCheck,
                    onProgress = onProgress
                )
            }
            if (ok && outputFile.exists() && outputFile.length() > 1024L) {
                return true
            }
        }
        return false
    }

    private fun encodeVideoWithSurface(
        context: Context,
        project: NewsProject,
        retriever: MediaMetadataRetriever?,
        sourceImageBitmap: Bitmap?,
        totalFrames: Int,
        videoDurationMs: Long,
        videoWidth: Int,
        videoHeight: Int,
        frameRate: Int,
        iFrameIntervalSec: Int,
        minSampleStepUs: Long,
        bitRate: Int,
        splitPointRatio: Float?,
        candidate: EncoderCandidate,
        outputFile: File,
        isActiveCheck: () -> Boolean,
        onProgress: (Float) -> Unit
    ): Boolean {
        var codec: MediaCodec? = null
        var muxer: MediaMuxer? = null
        var inputSurface: android.view.Surface? = null
        var isMuxerStarted = false
        var trackIndex = -1
        var samplesWritten = 0
        val frameCache = FrameSourceCache(minSampleStepUs)

        try {
            val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, videoWidth, videoHeight).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
                setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
                setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, iFrameIntervalSec)
            }

            codec = if (candidate.codecName != null) {
                MediaCodec.createByCodecName(candidate.codecName)
            } else {
                MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
            }
            codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            inputSurface = codec.createInputSurface()
            codec.start()

            muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

            val logoDrawable = ContextCompat.getDrawable(context, R.drawable.hk_news_logo)
            val logoSize = if (videoWidth <= 864) 110 else 160
            val logoBitmap = logoDrawable?.toBitmap(logoSize, logoSize)

            val bufferInfo = MediaCodec.BufferInfo()
            val frameDurationUs = 1_000_000L / frameRate.coerceAtLeast(1)

            for (frameIndex in 0 until totalFrames) {
                if (!isActiveCheck()) return false
                val canvas = inputSurface.lockCanvas(null)
                try {
                    renderCompositeFrame(
                        canvas = canvas,
                        project = project,
                        retriever = retriever,
                        frameCache = frameCache,
                        sourceImageBitmap = sourceImageBitmap,
                        logoBitmap = logoBitmap,
                        frameIndex = frameIndex,
                        totalFrames = totalFrames,
                        videoDurationMs = videoDurationMs,
                        videoWidth = videoWidth,
                        videoHeight = videoHeight,
                        splitPointRatio = splitPointRatio
                    )
                } finally {
                    inputSurface.unlockCanvasAndPost(canvas)
                }

                val res = drainCodec(
                    codec = codec,
                    muxer = muxer,
                    bufferInfo = bufferInfo,
                    muxerStarted = isMuxerStarted,
                    currentTrackIndex = trackIndex,
                    endOfStream = false,
                    overridePtsStartSample = samplesWritten,
                    frameDurationUs = frameDurationUs
                )
                isMuxerStarted = res.first
                trackIndex = res.second
                samplesWritten += res.third

                onProgress(0.1f + (frameIndex.toFloat() / totalFrames) * 0.8f)
            }

            codec.signalEndOfInputStream()
            val finalRes = drainCodec(
                codec = codec,
                muxer = muxer,
                bufferInfo = bufferInfo,
                muxerStarted = isMuxerStarted,
                currentTrackIndex = trackIndex,
                endOfStream = true,
                overridePtsStartSample = samplesWritten,
                frameDurationUs = frameDurationUs
            )
            isMuxerStarted = finalRes.first
            trackIndex = finalRes.second
            samplesWritten += finalRes.third

            return (isMuxerStarted && samplesWritten > 0)
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            frameCache.release()
            try { inputSurface?.release() } catch (_: Exception) {}
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

    private fun encodeVideoWithFrame(
        context: Context,
        project: NewsProject,
        retriever: MediaMetadataRetriever?,
        sourceImageBitmap: Bitmap?,
        totalFrames: Int,
        videoDurationMs: Long,
        videoWidth: Int,
        videoHeight: Int,
        frameRate: Int,
        iFrameIntervalSec: Int,
        minSampleStepUs: Long,
        bitRate: Int,
        splitPointRatio: Float?,
        candidate: EncoderCandidate,
        outputFile: File,
        isActiveCheck: () -> Boolean,
        onProgress: (Float) -> Unit
    ): Boolean {
        var codec: MediaCodec? = null
        var muxer: MediaMuxer? = null
        var isMuxerStarted = false
        var trackIndex = -1
        var samplesWritten = 0
        var frameBitmap: Bitmap? = null
        val frameCache = FrameSourceCache(minSampleStepUs)

        try {
            val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, videoWidth, videoHeight).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, candidate.colorFormat)
                setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
                setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, iFrameIntervalSec)
            }

            codec = if (candidate.codecName != null) {
                MediaCodec.createByCodecName(candidate.codecName)
            } else {
                MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
            }
            codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            codec.start()

            muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

            frameBitmap = Bitmap.createBitmap(videoWidth, videoHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(frameBitmap)
            val argbPixels = IntArray(videoWidth * videoHeight)
            val yuvBuffer = ByteArray(videoWidth * videoHeight * 3 / 2)

            val logoDrawable = ContextCompat.getDrawable(context, R.drawable.hk_news_logo)
            val logoSize = if (videoWidth <= 864) 110 else 160
            val logoBitmap = logoDrawable?.toBitmap(logoSize, logoSize)

            val bufferInfo = MediaCodec.BufferInfo()

            for (frameIndex in 0 until totalFrames) {
                if (!isActiveCheck()) return false
                renderCompositeFrame(
                    canvas = canvas,
                    project = project,
                    retriever = retriever,
                    frameCache = frameCache,
                    sourceImageBitmap = sourceImageBitmap,
                    logoBitmap = logoBitmap,
                    frameIndex = frameIndex,
                    totalFrames = totalFrames,
                    videoDurationMs = videoDurationMs,
                    videoWidth = videoWidth,
                    videoHeight = videoHeight,
                    splitPointRatio = splitPointRatio
                )

                // 3. Extract ARGB pixels
                frameBitmap.getPixels(argbPixels, 0, videoWidth, 0, 0, videoWidth, videoHeight)

                // 4. Acquire input buffer (bounded wait so last frame ALWAYS gets EOS queued)
                val isLastFrame = (frameIndex == totalFrames - 1)
                var inputBufferIndex = -1
                var waitTries = 0
                while (inputBufferIndex < 0 && waitTries < 60) {
                    inputBufferIndex = codec.dequeueInputBuffer(10_000)
                    if (inputBufferIndex < 0) {
                        val res = drainCodec(codec, muxer, bufferInfo, isMuxerStarted, trackIndex, false)
                        isMuxerStarted = res.first
                        trackIndex = res.second
                        samplesWritten += res.third
                        waitTries++
                    }
                }

                if (inputBufferIndex >= 0) {
                    val ptsUs = (frameIndex * 1_000_000L) / frameRate
                    val flags = if (isLastFrame) MediaCodec.BUFFER_FLAG_END_OF_STREAM else 0
                    var inputImage: Image? = null
                    if (candidate.useImageApi) {
                        inputImage = try { codec.getInputImage(inputBufferIndex) } catch (_: Exception) { null }
                    }

                    if (inputImage != null) {
                        fillYuvImage(inputImage, argbPixels, videoWidth, videoHeight)
                        try { inputImage.close() } catch (_: Exception) {}
                        val bufSize = codec.getInputBuffer(inputBufferIndex)?.capacity() ?: yuvBuffer.size
                        codec.queueInputBuffer(inputBufferIndex, 0, bufSize, ptsUs, flags)
                    } else {
                        if (candidate.colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar) {
                            encodeYUV420Planar(yuvBuffer, argbPixels, videoWidth, videoHeight)
                        } else {
                            encodeYUV420SP(yuvBuffer, argbPixels, videoWidth, videoHeight)
                        }
                        val inputBuffer = codec.getInputBuffer(inputBufferIndex)
                        inputBuffer?.clear()
                        val bytesToWrite = minOf(yuvBuffer.size, inputBuffer?.remaining() ?: yuvBuffer.size)
                        inputBuffer?.put(yuvBuffer, 0, bytesToWrite)
                        codec.queueInputBuffer(inputBufferIndex, 0, bytesToWrite, ptsUs, flags)
                    }
                }

                // 5. Drain encoded data into MediaMuxer
                val res = drainCodec(codec, muxer, bufferInfo, isMuxerStarted, trackIndex, false)
                isMuxerStarted = res.first
                trackIndex = res.second
                samplesWritten += res.third

                onProgress(0.1f + (frameIndex.toFloat() / totalFrames) * 0.8f)
            }

            // Drain remaining frames until EOS (with timeout guard)
            val finalRes = drainCodec(codec, muxer, bufferInfo, isMuxerStarted, trackIndex, true)
            isMuxerStarted = finalRes.first
            trackIndex = finalRes.second
            samplesWritten += finalRes.third

            return (isMuxerStarted && samplesWritten > 0)
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            frameCache.release()
            try { frameBitmap?.recycle() } catch (_: Exception) {}
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

    private fun renderCompositeFrame(
        canvas: Canvas,
        project: NewsProject,
        retriever: MediaMetadataRetriever?,
        frameCache: FrameSourceCache,
        sourceImageBitmap: Bitmap?,
        logoBitmap: Bitmap?,
        frameIndex: Int,
        totalFrames: Int,
        videoDurationMs: Long,
        videoWidth: Int,
        videoHeight: Int,
        splitPointRatio: Float? = null
    ) {
        val baseProgress = frameIndex.toFloat() / totalFrames.coerceAtLeast(1)
        val curveProgress = when (project.vnSpeedCurve) {
            "MONTAGE" -> if (baseProgress < 0.5f) baseProgress * 1.4f else 0.7f + (baseProgress - 0.5f) * 0.6f
            "HERO" -> if (baseProgress in 0.3f..0.7f) 0.3f + (baseProgress - 0.3f) * 0.35f else baseProgress
            "BULLET" -> if (baseProgress in 0.25f..0.75f) 0.25f + (baseProgress - 0.25f) * 0.2f else baseProgress
            "FLASH_IN" -> (baseProgress * baseProgress)
            "FLASH_OUT" -> 1f - (1f - baseProgress) * (1f - baseProgress)
            else -> baseProgress
        }.coerceIn(0f, 1f)

        val trimStart = project.videoTrimStart.coerceIn(0f, 0.95f)
        val trimEnd = project.videoTrimEnd.coerceIn(trimStart + 0.02f, 1.0f)
        val trimProgress = trimStart + (trimEnd - trimStart) * curveProgress
        val effectiveTimeUs = when {
            project.vnFreezeFrameActive -> ((trimStart + trimEnd) * 0.5f * videoDurationMs * 1000L).toLong()
            project.vnReversePlayback -> ((trimEnd - (trimEnd - trimStart) * curveProgress) * videoDurationMs * 1000L).toLong()
            else -> (trimProgress * videoDurationMs * 1000L).toLong()
        }

        val canvasBgColor = when (project.vnBackgroundStyle) {
            "NAVY_STUDIO" -> android.graphics.Color.parseColor("#0A192F")
            "CRIMSON_NEWS" -> android.graphics.Color.parseColor("#3E0B12")
            "GOLD_ROYAL" -> android.graphics.Color.parseColor("#3B2F08")
            "GRADIENT_DARK" -> android.graphics.Color.parseColor("#0D1B2A")
            else -> android.graphics.Color.BLACK
        }
        canvas.drawColor(canvasBgColor)

        var bgFrame: Bitmap? = null
        if (retriever != null) {
            bgFrame = frameCache.getFrame(retriever, effectiveTimeUs)
        } else if (sourceImageBitmap != null && !sourceImageBitmap.isRecycled) {
            bgFrame = sourceImageBitmap
        }

        val borderPx = project.vnBorderWidthDp * (videoWidth / 360f)
        val innerRect = RectF(
            borderPx,
            borderPx,
            (videoWidth - borderPx).coerceAtLeast(borderPx + 10f),
            (videoHeight - borderPx).coerceAtLeast(borderPx + 10f)
        )

        // Draw studio backdrop behind Chroma Key foreground if enabled
        if (project.chromaKeyEnabled || bgFrame == null) {
            drawStudioBackdrop(canvas, frameIndex, totalFrames, videoWidth, videoHeight)
        }

        // In/Out Animation & Split Transition curves (matching NewsOverlayFrame)
        val inT = if (baseProgress < 0.14f) (baseProgress / 0.14f).coerceIn(0f, 1f) else 1f
        val outT = if (baseProgress > 0.86f) ((1f - baseProgress) / 0.14f).coerceIn(0f, 1f) else 1f
        val inScale = if (project.inAnimation == "POP_UP") 0.55f + 0.45f * inT else 1f
        val inTx = if (project.inAnimation == "SLIDE_RIGHT") -(1f - inT) * videoWidth else 0f
        val inTy = if (project.inAnimation == "SLIDE_UP") (1f - inT) * videoHeight else 0f
        val outTx = if (project.outAnimation == "SLIDE_LEFT") -(1f - outT) * videoWidth else 0f
        val outTy = if (project.outAnimation == "SLIDE_DOWN") (1f - outT) * videoHeight else 0f
        val inRot = if (project.inAnimation == "SPIN_CLOCKWISE") (1f - inT) * -360f else 0f
        val inAlpha = if (project.inAnimation == "FADE_IN") inT else 1f
        val outAlpha = if (project.outAnimation == "FADE_OUT") outT else 1f
        val transCenter = splitPointRatio ?: 0.06f
        val distToTrans = kotlin.math.abs(baseProgress - transCenter)
        val splitWindow = if (project.activeTransition != "NONE" && distToTrans < 0.07f) {
            (1f - (distToTrans / 0.07f)).coerceIn(0f, 1f)
        } else 0f
        val transScale = when (project.activeTransition) {
            "ZOOM_BURST" -> 1f + splitWindow * 0.35f
            "CUBE_ROTATE" -> 1f - splitWindow * 0.25f
            "3D_NEWS_FLIP" -> 1f - splitWindow * 0.15f
            else -> 1f
        }
        val transRot = when (project.activeTransition) {
            "3D_NEWS_FLIP" -> splitWindow * 18f
            "CUBE_ROTATE" -> splitWindow * -14f
            else -> 0f
        }
        val transAlpha = if (project.activeTransition == "CROSSFADE") (1f - splitWindow * 0.65f) else 1f

        canvas.save()
        val fxPhase = ((frameIndex % 18) / 18f)
        val fxScale = when (project.vnMotionFx) {
            "ZOOM_PULSE" -> 1f + (kotlin.math.sin(fxPhase * Math.PI * 2).toFloat() * 0.04f + 0.04f)
            "EXPANSION" -> 1f + fxPhase * 0.12f
            "JITTER" -> if (frameIndex % 4 < 2) 1.04f else 1.0f
            else -> 1f
        } * inScale * transScale
        val wipeTx = if (project.activeTransition == "WIPE_RIGHT") splitWindow * videoWidth * 0.25f else 0f
        val fxTx = (if (project.vnMotionFx == "SHAKE") kotlin.math.sin(fxPhase * Math.PI * 4).toFloat() * 10f else 0f) + inTx + outTx + wipeTx
        val fxTy = (if (project.vnMotionFx == "SHAKE") kotlin.math.cos(fxPhase * Math.PI * 4).toFloat() * 7f else 0f) + inTy + outTy

        canvas.translate(videoWidth / 2f + fxTx, videoHeight / 2f + fxTy)
        canvas.rotate(project.vnRotationDegrees + inRot + transRot)
        canvas.scale(
            if (project.vnMirrorHorizontal) -fxScale else fxScale,
            if (project.vnFlipVertical) -fxScale else fxScale
        )
        canvas.translate(-videoWidth / 2f, -videoHeight / 2f)

        if (bgFrame != null) {
            val panZoomScale = if (project.panZoomLocked) {
                project.panZoomStartScale
            } else {
                project.panZoomStartScale + (project.panZoomEndScale - project.panZoomStartScale) * baseProgress
            }.coerceIn(1.0f, 2.5f)
            val effCropZoom = maxOf(
                project.videoCropZoom.coerceIn(1.0f, 3.0f),
                1f + maxOf(kotlin.math.abs(project.videoCropPanX), kotlin.math.abs(project.videoCropPanY)) * 0.45f
            )
            val totalZoom = (panZoomScale * effCropZoom).coerceIn(1.0f, 4.0f)

            val cLeft = project.videoCropLeft.coerceIn(0f, 0.4f)
            val cRight = project.videoCropRight.coerceIn(0f, 0.4f)
            val cTop = project.videoCropTop.coerceIn(0f, 0.4f)
            val cBottom = project.videoCropBottom.coerceIn(0f, 0.4f)

            val usableW = (bgFrame.width * (1f - cLeft - cRight)).coerceAtLeast(16f)
            val usableH = (bgFrame.height * (1f - cTop - cBottom)).coerceAtLeast(16f)
            val cropW = (usableW / totalZoom).toInt().coerceAtLeast(8)
            val cropH = (usableH / totalZoom).toInt().coerceAtLeast(8)

            val baseOriginX = bgFrame.width * cLeft
            val baseOriginY = bgFrame.height * cTop
            val maxShiftX = (usableW - cropW).coerceAtLeast(0f)
            val maxShiftY = (usableH - cropH).coerceAtLeast(0f)
            val panXFactor = (0.5f - project.videoCropPanX.coerceIn(-1f, 1f) * 0.5f).coerceIn(0f, 1f)
            val panYFactor = (0.5f - project.videoCropPanY.coerceIn(-1f, 1f) * 0.5f).coerceIn(0f, 1f)

            val cropX = (baseOriginX + maxShiftX * panXFactor).toInt().coerceIn(0, (bgFrame.width - cropW).coerceAtLeast(0))
            val cropY = (baseOriginY + maxShiftY * panYFactor).toInt().coerceIn(0, (bgFrame.height - cropH).coerceAtLeast(0))
            val srcRect = Rect(cropX, cropY, (cropX + cropW).coerceAtMost(bgFrame.width), (cropY + cropH).coerceAtMost(bgFrame.height))

            val cm = buildExportColorMatrix(project)
            val combinedAlpha = (project.vnVideoOpacity.coerceIn(0.2f, 1.0f) * inAlpha * outAlpha * transAlpha).coerceIn(0.05f, 1.0f)
            val framePaint = Paint().apply {
                colorFilter = android.graphics.ColorMatrixColorFilter(cm)
                alpha = (combinedAlpha * 255f).toInt()
            }

            if (project.chromaKeyEnabled) {
                val keyedBmp = applyChromaKeyMask(
                    src = bgFrame,
                    srcRect = srcRect,
                    dstW = videoWidth.coerceAtMost(640),
                    dstH = videoHeight.coerceAtMost(640),
                    keyHex = project.chromaKeyColor,
                    similarity = project.chromaKeySimilarity
                )
                canvas.drawBitmap(keyedBmp, null, innerRect, framePaint)
                keyedBmp.recycle()
            } else {
                canvas.drawBitmap(bgFrame, srcRect, innerRect, framePaint)
            }
        }
        canvas.restore()

        if (project.activeTransition == "GLITCH_TV" && splitWindow > 0.05f) {
            val gPaint1 = Paint().apply { color = 0x5500E5FF }
            val gPaint2 = Paint().apply { color = 0x55FF0055 }
            canvas.drawRect(0f, videoHeight * 0.3f, videoWidth.toFloat(), videoHeight * 0.36f, gPaint1)
            canvas.drawRect(0f, videoHeight * 0.62f, videoWidth.toFloat(), videoHeight * 0.67f, gPaint2)
        }

        // Preset & manual Crop matte bars (1:1, 4:5, 9:16, 4:3, 21:9)
        val pSide = when (project.videoCropPreset) {
            "1:1" -> 0.15f
            "4:5" -> 0.18f
            "9:16" -> 0.24f
            "4:3" -> 0.08f
            else -> 0f
        }
        val pVert = if (project.videoCropPreset == "21:9") 0.12f else 0f
        if (pSide > 0f || pVert > 0f) {
            val matte = Paint().apply { color = android.graphics.Color.BLACK }
            if (pSide > 0f) {
                val sidePx = videoWidth * pSide
                canvas.drawRect(0f, 0f, sidePx, videoHeight.toFloat(), matte)
                canvas.drawRect(videoWidth - sidePx, 0f, videoWidth.toFloat(), videoHeight.toFloat(), matte)
            }
            if (pVert > 0f) {
                val vertPx = videoHeight * pVert
                canvas.drawRect(0f, 0f, videoWidth.toFloat(), vertPx, matte)
                canvas.drawRect(0f, videoHeight - vertPx, videoWidth.toFloat(), videoHeight.toFloat(), matte)
            }
        }

        // Export Active VFX & Animation Filters + Flash Cut Motion FX
        drawExportAnimationFilter(canvas, project.activeAnimationFilter, frameIndex, totalFrames, videoWidth, videoHeight)
        drawExportVfxOverlay(canvas, project.activeVfx, frameIndex, videoWidth, videoHeight)
        if (project.vnMotionFx == "FLASH_CUT" && (frameIndex % 10) < 2) {
            canvas.drawColor(0x55FFFFFF)
        }

        when (project.vnMaskType) {
            "RECTANGLE" -> {
                val mattePaint = Paint().apply { color = android.graphics.Color.BLACK }
                val barH = videoHeight * 0.11f
                canvas.drawRect(0f, 0f, videoWidth.toFloat(), barH, mattePaint)
                canvas.drawRect(0f, videoHeight - barH, videoWidth.toFloat(), videoHeight.toFloat(), mattePaint)
            }
            "RADIAL", "LINEAR" -> {
                val vigPaint = Paint().apply {
                    shader = android.graphics.RadialGradient(
                        videoWidth / 2f, videoHeight / 2f, videoWidth * 0.65f,
                        intArrayOf(0x00000000, 0xBB000000.toInt()),
                        null, Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRect(0f, 0f, videoWidth.toFloat(), videoHeight.toFloat(), vigPaint)
            }
        }

        if (project.vnVignette > 0.03f) {
            val alphaHex = ((project.vnVignette * 210f).toInt().coerceIn(0, 210) shl 24)
            val vigPaint = Paint().apply {
                shader = android.graphics.RadialGradient(
                    videoWidth / 2f, videoHeight / 2f, videoWidth * 0.65f,
                    intArrayOf(0x00000000, alphaHex),
                    null, Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 0f, videoWidth.toFloat(), videoHeight.toFloat(), vigPaint)
        }

        if (borderPx > 1f) {
            val bColor = try {
                android.graphics.Color.parseColor(project.vnBorderColorHex)
            } catch (_: Exception) {
                android.graphics.Color.parseColor("#FFD54F")
            }
            val borderPaint = Paint().apply {
                color = bColor
                style = Paint.Style.STROKE
                strokeWidth = borderPx * 2f
            }
            canvas.drawRect(0f, 0f, videoWidth.toFloat(), videoHeight.toFloat(), borderPaint)
        }

        drawBroadcastOverlays(
            canvas = canvas,
            project = project,
            logoBitmap = logoBitmap,
            frameIndex = frameIndex,
            totalFrames = totalFrames,
            videoWidth = videoWidth,
            videoHeight = videoHeight
        )

        if (project.vnSubtitleText.isNotBlank()) {
            val subTextSize = if (videoWidth <= 864) 22f else 30f
            val subPaint = Paint().apply {
                color = if (project.vnSubtitleStyle == "YELLOW_BOLD") android.graphics.Color.parseColor("#FFD54F") else android.graphics.Color.WHITE
                textSize = subTextSize
                isFakeBoldText = true
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }
            val subText = project.vnSubtitleText.take(60)
            val textW = subPaint.measureText(subText)
            val cx = videoWidth / 2f
            val cy = videoHeight * 0.62f
            if (project.vnSubtitleStyle != "MINIMAL") {
                val bgPaint = Paint().apply {
                    color = if (project.vnSubtitleStyle == "NEWS_BAR") android.graphics.Color.parseColor("#DD8E0000") else android.graphics.Color.parseColor("#BB000000")
                }
                canvas.drawRoundRect(
                    RectF(cx - textW / 2f - 18f, cy - subTextSize - 4f, cx + textW / 2f + 18f, cy + 12f),
                    8f, 8f, bgPaint
                )
            }
            canvas.drawText(subText, cx, cy, subPaint)
        }
    }

    private fun drainCodec(
        codec: MediaCodec,
        muxer: MediaMuxer,
        bufferInfo: MediaCodec.BufferInfo,
        muxerStarted: Boolean,
        currentTrackIndex: Int,
        endOfStream: Boolean,
        overridePtsStartSample: Int = -1,
        frameDurationUs: Long = 0L
    ): Triple<Boolean, Int, Int> {
        var isStarted = muxerStarted
        var trackIdx = currentTrackIndex
        var samplesAdded = 0
        var emptyTries = 0

        while (emptyTries < 150) {
            val outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 10_000)
            if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                if (!endOfStream) break
                emptyTries++
            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                if (!isStarted) {
                    val newFormat = codec.outputFormat
                    trackIdx = muxer.addTrack(newFormat)
                    muxer.start()
                    isStarted = true
                }
            } else if (outputBufferIndex >= 0) {
                emptyTries = 0
                val encodedData = codec.getOutputBuffer(outputBufferIndex)
                if (encodedData != null && isStarted && trackIdx >= 0) {
                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                        bufferInfo.size = 0
                    }
                    if (bufferInfo.size > 0) {
                        if (overridePtsStartSample >= 0 && frameDurationUs > 0L) {
                            bufferInfo.presentationTimeUs = (overridePtsStartSample + samplesAdded) * frameDurationUs
                        }
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

    private fun tryMuxWithSourceAudio(
        context: Context,
        videoFile: File,
        sourceAudioUri: Uri,
        outputFile: File,
        trimStartMs: Long,
        trimEndMs: Long,
        trimmedDurationMs: Long
    ): Boolean {
        var videoExtractor: MediaExtractor? = null
        var audioExtractor: MediaExtractor? = null
        var muxer: MediaMuxer? = null
        var muxerStarted = false
        return try {
            videoExtractor = MediaExtractor().apply { setDataSource(videoFile.absolutePath) }
            audioExtractor = MediaExtractor().apply {
                if (sourceAudioUri.scheme == "file" && !sourceAudioUri.path.isNullOrBlank()) {
                    setDataSource(sourceAudioUri.path!!)
                } else {
                    setDataSource(context, sourceAudioUri, null)
                }
            }

            var vTrackIdx = -1
            var vFormat: MediaFormat? = null
            for (i in 0 until videoExtractor.trackCount) {
                val fmt = videoExtractor.getTrackFormat(i)
                if (fmt.getString(MediaFormat.KEY_MIME)?.startsWith("video/") == true) {
                    vTrackIdx = i
                    vFormat = fmt
                    break
                }
            }

            var aTrackIdx = -1
            var aFormat: MediaFormat? = null
            for (i in 0 until audioExtractor.trackCount) {
                val fmt = audioExtractor.getTrackFormat(i)
                val mime = fmt.getString(MediaFormat.KEY_MIME) ?: ""
                if (mime.startsWith("audio/mp4a-latm") || mime.startsWith("audio/aac")) {
                    aTrackIdx = i
                    aFormat = fmt
                    break
                }
            }

            if (vTrackIdx < 0 || vFormat == null || aTrackIdx < 0 || aFormat == null) {
                return false
            }

            muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            val muxVideoTrack = muxer.addTrack(vFormat)
            val muxAudioTrack = muxer.addTrack(aFormat)
            muxer.start()
            muxerStarted = true

            val buffer = ByteBuffer.allocate(512 * 1024)
            val info = MediaCodec.BufferInfo()

            videoExtractor.selectTrack(vTrackIdx)
            while (true) {
                val size = videoExtractor.readSampleData(buffer, 0)
                if (size < 0) break
                info.offset = 0
                info.size = size
                info.presentationTimeUs = videoExtractor.sampleTime
                @Suppress("DEPRECATION")
                info.flags = videoExtractor.sampleFlags
                muxer.writeSampleData(muxVideoTrack, buffer, info)
                videoExtractor.advance()
            }

            val startUs = trimStartMs.coerceAtLeast(0L) * 1000L
            val endUs = trimEndMs.coerceAtLeast(trimStartMs + 1000L) * 1000L
            val maxOutputUs = trimmedDurationMs.coerceAtLeast(1000L) * 1000L
            audioExtractor.selectTrack(aTrackIdx)
            if (startUs > 0L) {
                audioExtractor.seekTo(startUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
            }
            var firstAudioPts = -1L
            while (true) {
                val size = audioExtractor.readSampleData(buffer, 0)
                if (size < 0) break
                val pts = audioExtractor.sampleTime
                if (pts < startUs) {
                    audioExtractor.advance()
                    continue
                }
                if (pts > endUs) break
                if (firstAudioPts < 0L) firstAudioPts = pts
                val outPts = (pts - firstAudioPts).coerceAtLeast(0L)
                if (outPts > maxOutputUs) break
                info.offset = 0
                info.size = size
                info.presentationTimeUs = outPts
                @Suppress("DEPRECATION")
                info.flags = audioExtractor.sampleFlags
                muxer.writeSampleData(muxAudioTrack, buffer, info)
                audioExtractor.advance()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            try { videoExtractor?.release() } catch (_: Exception) {}
            try { audioExtractor?.release() } catch (_: Exception) {}
            try {
                if (muxerStarted) muxer?.stop()
                muxer?.release()
            } catch (_: Exception) {}
        }
    }

    internal fun buildExportColorMatrix(project: NewsProject): android.graphics.ColorMatrix {
        val cm = android.graphics.ColorMatrix()
        when (project.activeFilter) {
            "BW", "NOIR" -> cm.setSaturation(0f)
            "CINEMA" -> cm.postConcat(
                android.graphics.ColorMatrix(
                    floatArrayOf(
                        0.98f, 0.1f, 0.0f, 0f, 8f,
                        0.05f, 1.05f, 0.08f, 0f, 4f,
                        0.08f, 0.12f, 1.2f, 0f, 10f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            )
            "WARM" -> cm.postConcat(
                android.graphics.ColorMatrix(
                    floatArrayOf(
                        1.18f, 0.12f, 0.0f, 0f, 16f,
                        0.05f, 1.06f, 0.0f, 0f, 6f,
                        0.0f, 0.0f, 0.86f, 0f, -12f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            )
            "VINTAGE" -> cm.postConcat(
                android.graphics.ColorMatrix(
                    floatArrayOf(
                        0.393f, 0.769f, 0.189f, 0f, 0f,
                        0.349f, 0.686f, 0.168f, 0f, 0f,
                        0.272f, 0.534f, 0.131f, 0f, 0f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            )
            "NIGHT_VISION" -> cm.postConcat(
                android.graphics.ColorMatrix(
                    floatArrayOf(
                        0.1f, 0.1f, 0.1f, 0f, 0f,
                        0.4f, 1.2f, 0.4f, 0f, 40f,
                        0.1f, 0.1f, 0.1f, 0f, 0f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            )
            "VIVID" -> cm.setSaturation(1.6f)
            "DRAMATIC" -> cm.setSaturation(0.4f)
            "BREAKING_ALERT" -> cm.postConcat(
                android.graphics.ColorMatrix(
                    floatArrayOf(
                        1.4f, 0.1f, 0.1f, 0f, 15f,
                        0.1f, 1.0f, 0.1f, 0f, 0f,
                        0.1f, 0.1f, 0.9f, 0f, -10f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            )
            "COOL_STUDIO" -> cm.postConcat(
                android.graphics.ColorMatrix(
                    floatArrayOf(
                        0.9f, 0.0f, 0.1f, 0f, -10f,
                        0.0f, 1.0f, 0.1f, 0f, 0f,
                        0.1f, 0.2f, 1.3f, 0f, 25f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            )
            "GOLDEN_HOUR" -> cm.postConcat(
                android.graphics.ColorMatrix(
                    floatArrayOf(
                        1.2f, 0.2f, 0.0f, 0f, 20f,
                        0.1f, 1.1f, 0.0f, 0f, 10f,
                        0.0f, 0.0f, 0.8f, 0f, -20f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            )
            "CYBER" -> cm.postConcat(
                android.graphics.ColorMatrix(
                    floatArrayOf(
                        1.2f, 0.0f, 0.3f, 0f, 10f,
                        0.0f, 1.1f, 0.2f, 0f, 0f,
                        0.3f, 0.1f, 1.4f, 0f, 30f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            )
            "SAFFRON" -> cm.postConcat(
                android.graphics.ColorMatrix(
                    floatArrayOf(
                        1.3f, 0.1f, 0.0f, 0f, 25f,
                        0.1f, 1.0f, 0.0f, 0f, 5f,
                        0.0f, 0.0f, 0.7f, 0f, -15f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            )
            "CINE_TEAL" -> cm.postConcat(
                android.graphics.ColorMatrix(
                    floatArrayOf(
                        0.95f, 0.1f, 0.0f, 0f, 10f,
                        0.05f, 1.05f, 0.1f, 0f, 5f,
                        0.1f, 0.15f, 1.25f, 0f, 15f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            )
            "MOODY_NEWS" -> cm.postConcat(
                android.graphics.ColorMatrix(
                    floatArrayOf(
                        0.85f, 0.0f, 0.05f, 0f, -15f,
                        0.0f, 0.85f, 0.05f, 0f, -15f,
                        0.05f, 0.05f, 0.95f, 0f, -5f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            )
            "WARM_VLOG" -> cm.postConcat(
                android.graphics.ColorMatrix(
                    floatArrayOf(
                        1.2f, 0.1f, 0.0f, 0f, 15f,
                        0.05f, 1.08f, 0.0f, 0f, 8f,
                        0.0f, 0.0f, 0.85f, 0f, -10f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            )
            "FRESH_DAY" -> cm.postConcat(
                android.graphics.ColorMatrix(
                    floatArrayOf(
                        1.05f, 0.05f, 0.0f, 0f, 12f,
                        0.0f, 1.15f, 0.05f, 0f, 14f,
                        0.0f, 0.05f, 1.1f, 0f, 10f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            )
        }

        if (kotlin.math.abs(project.vnSaturation - 1.0f) > 0.02f) {
            val satCm = android.graphics.ColorMatrix().apply { setSaturation(project.vnSaturation.coerceIn(0f, 2f)) }
            cm.postConcat(satCm)
        }
        val c = (project.videoContrast + project.vnSharpen.coerceIn(0f, 1f) * 0.22f).coerceIn(0.5f, 2.5f)
        val b = project.videoBrightness * 255f
        val t = (128f * (1f - c)) + b
        val rWarm = if (project.vnWarmth > 0f) project.vnWarmth * 45f else 0f
        val bCool = if (project.vnWarmth < 0f) (-project.vnWarmth) * 45f else 0f
        cm.postConcat(
            android.graphics.ColorMatrix(
                floatArrayOf(
                    c, 0f, 0f, 0f, t + rWarm,
                    0f, c, 0f, 0f, t,
                    0f, 0f, c, 0f, t + bCool,
                    0f, 0f, 0f, 1f, 0f
                )
            )
        )
        return cm
    }

    private fun applyChromaKeyMask(
        src: Bitmap,
        srcRect: Rect,
        dstW: Int,
        dstH: Int,
        keyHex: String,
        similarity: Float
    ): Bitmap {
        val w = dstW.coerceAtLeast(16)
        val h = dstH.coerceAtLeast(16)
        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val tmpCanvas = Canvas(out)
        tmpCanvas.drawBitmap(src, srcRect, Rect(0, 0, w, h), null)

        val targetColor = try {
            android.graphics.Color.parseColor(keyHex)
        } catch (_: Exception) {
            android.graphics.Color.parseColor("#00C853")
        }
        val tr = android.graphics.Color.red(targetColor)
        val tg = android.graphics.Color.green(targetColor)
        val tb = android.graphics.Color.blue(targetColor)
        val thresholdSq = ((similarity.coerceIn(0.05f, 0.9f) * 260f).let { it * it }).toInt()
        val softEdgeSq = (thresholdSq * 1.45f).toInt().coerceAtLeast(thresholdSq + 1)

        val pixels = IntArray(w * h)
        out.getPixels(pixels, 0, w, 0, 0, w, h)
        for (i in pixels.indices) {
            val p = pixels[i]
            val pr = (p shr 16) and 0xFF
            val pg = (p shr 8) and 0xFF
            val pb = p and 0xFF
            val dr = pr - tr
            val dg = pg - tg
            val db = pb - tb
            val distSq = dr * dr + dg * dg + db * db
            if (distSq <= thresholdSq) {
                pixels[i] = 0x00000000
            } else if (distSq < softEdgeSq) {
                val alphaFrac = ((distSq - thresholdSq).toFloat() / (softEdgeSq - thresholdSq)).coerceIn(0f, 1f)
                val newA = (((p ushr 24) and 0xFF) * alphaFrac).toInt().coerceIn(0, 255)
                pixels[i] = (newA shl 24) or (p and 0x00FFFFFF)
            }
        }
        out.setPixels(pixels, 0, w, 0, 0, w, h)
        return out
    }

    private fun drawExportAnimationFilter(
        canvas: Canvas,
        animFilter: String,
        frameIndex: Int,
        totalFrames: Int,
        width: Int,
        height: Int
    ) {
        if (animFilter == "NONE") return
        val phase = ((frameIndex % 36) / 36f)
        when (animFilter) {
            "NEON_PULSE_WAVE" -> {
                val waveX = -width * 0.25f + (width * 1.5f * phase)
                val p = Paint().apply {
                    shader = LinearGradient(
                        waveX - 80f, 0f, waveX + 90f, height.toFloat(),
                        intArrayOf(0x00000000, 0x4400E5FF, 0x44FF1744, 0x00000000),
                        null, Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), p)
                val borderP = Paint().apply {
                    color = 0x8800E5FF.toInt()
                    style = Paint.Style.STROKE
                    strokeWidth = 6f
                }
                canvas.drawRect(3f, 3f, width - 3f, height - 3f, borderP)
            }
            "GOLDEN_SPARKLE" -> {
                val rng = kotlin.random.Random(108)
                val p = Paint().apply { color = android.graphics.Color.parseColor("#FFD54F") }
                for (i in 0..24) {
                    val bx = rng.nextFloat() * width
                    val by = ((rng.nextFloat() - phase * 0.6f + 1f) % 1f) * height
                    p.alpha = (110 + (120 * kotlin.math.abs(kotlin.math.sin((phase + i * 0.2) * Math.PI))).toInt()).coerceIn(60, 230)
                    canvas.drawCircle(bx, by, 3.5f + (i % 4) * 1.6f, p)
                }
            }
            "LIGHT_LEAK_SWEEP" -> {
                val lx = -width * 0.3f + width * 1.6f * phase
                val p = Paint().apply {
                    shader = android.graphics.RadialGradient(
                        lx, height * 0.25f, width * 0.55f,
                        intArrayOf(0x66FF6D00, 0x33FF1744, 0x00000000),
                        null, Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), p)
            }
            "VHS_RETRO_WAVE" -> {
                val barY = (phase * height) % height
                val barP = Paint().apply { color = 0x33FFFFFF }
                canvas.drawRect(0f, barY, width.toFloat(), barY + 18f, barP)
                val scanP = Paint().apply { color = 0x22000000; strokeWidth = 1.5f }
                var y = 0f
                while (y < height) {
                    canvas.drawLine(0f, y, width.toFloat(), y, scanP)
                    y += 8f
                }
            }
            "PRISM_AURORA" -> {
                val p = Paint().apply {
                    shader = LinearGradient(
                        0f, 0f, width.toFloat(), height.toFloat(),
                        intArrayOf(0x2A00E5FF, 0x30AA00FF, 0x2AFF4081, 0x28FFD54F),
                        null, Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), p)
            }
            "CYBER_MATRIX" -> {
                val cols = 14
                val colW = width.toFloat() / cols
                val p = Paint().apply { strokeWidth = 3f; color = 0x9900E676.toInt() }
                for (c in 0 until cols) {
                    val x = c * colW + colW * 0.5f
                    val dropY = ((phase * 1.4f + c * 0.19f) % 1f) * height
                    canvas.drawLine(x, (dropY - 70f).coerceAtLeast(0f), x, dropY, p)
                }
            }
            "FIRE_EMBERS" -> {
                val rng = kotlin.random.Random(77)
                val p = Paint()
                for (i in 0..26) {
                    val ex = (rng.nextFloat() * width + kotlin.math.sin((phase * 6.28 + i)).toFloat() * 14f).coerceIn(0f, width.toFloat())
                    val ey = height - (((rng.nextFloat() + phase * 1.3f) % 1f) * height)
                    p.color = if (i % 2 == 0) 0xCCFF6D00.toInt() else 0xCCFFD54F.toInt()
                    canvas.drawCircle(ex, ey, 3f + (i % 3) * 1.8f, p)
                }
            }
            "SNOW_FALL" -> {
                val rng = kotlin.random.Random(303)
                val p = Paint().apply { color = 0xBBFFFFFF.toInt() }
                for (i in 0..30) {
                    val sx = (rng.nextFloat() * width + phase * 35f * (if (i % 2 == 0) 1f else -1f) + width) % width
                    val sy = ((rng.nextFloat() + phase) % 1f) * height
                    canvas.drawCircle(sx, sy, 2.8f + (i % 3) * 1.2f, p)
                }
            }
        }
    }

    private fun drawExportVfxOverlay(
        canvas: Canvas,
        vfx: String,
        frameIndex: Int,
        width: Int,
        height: Int
    ) {
        when (vfx) {
            "SCANLINES" -> {
                val p = Paint().apply { color = 0x33000000; strokeWidth = 1.5f }
                var y = 0f
                while (y < height) {
                    canvas.drawLine(0f, y, width.toFloat(), y, p)
                    y += 6f
                }
            }
            "VIGNETTE" -> {
                val p = Paint().apply {
                    shader = android.graphics.RadialGradient(
                        width / 2f, height / 2f, width * 0.65f,
                        intArrayOf(0x00000000, 0x99000000.toInt()),
                        null, Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), p)
            }
            "FLASH_STROBE" -> {
                if ((frameIndex % 6) < 2) canvas.drawColor(0x38FF0000)
            }
            "EMERGENCY_SIREN" -> {
                canvas.drawColor(if ((frameIndex % 10) < 5) 0x33FF1744 else 0x332979FF)
            }
            "GLITCH" -> {
                val y1 = ((frameIndex % 20) / 20f) * height
                val p1 = Paint().apply { color = 0x3300E5FF }
                val p2 = Paint().apply { color = 0x33FF0055 }
                canvas.drawRect(0f, y1, width.toFloat(), y1 + 16f, p1)
                canvas.drawRect(0f, (y1 + 22f) % height, width.toFloat(), ((y1 + 22f) % height) + 8f, p2)
            }
            "RAIN_STORM" -> {
                val rng = kotlin.random.Random(42)
                val p = Paint().apply { color = 0x44B0BEC5; strokeWidth = 2f }
                val phase = (frameIndex % 15) / 15f
                for (i in 0..40) {
                    val sx = (rng.nextFloat() * width + phase * 120f) % width
                    val sy = (rng.nextFloat() * height + phase * 280f) % height
                    canvas.drawLine(sx, sy, sx - 7f, sy + 28f, p)
                }
            }
        }
    }

    /**
     * Real PCM Audio DSP Processor: decodes source audio -> applies Pitch shift,
     * Voice Changer Effects (DEEP_ANCHOR, FEMALE_ANCHOR, RADIO_MIC, ECHO_STUDIO,
     * CHIPMUNK, ROBOT, MEGAPHONE, SLOW_CINEMA), Gain Boost (up to 200%) & Fade In/Out ->
     * encodes back to AAC and muxes cleanly with the rendered MP4 video.
     */
    private fun tryProcessAndMuxAudio(
        context: Context,
        videoFile: File,
        sourceAudioUri: Uri,
        outputFile: File,
        trimStartMs: Long,
        trimEndMs: Long,
        trimmedDurationMs: Long,
        speedFactor: Float,
        project: NewsProject
    ): Boolean {
        var videoExtractor: MediaExtractor? = null
        var audioExtractor: MediaExtractor? = null
        var decoder: MediaCodec? = null
        var encoder: MediaCodec? = null
        var muxer: MediaMuxer? = null
        var muxerStarted = false

        return try {
            videoExtractor = MediaExtractor().apply { setDataSource(videoFile.absolutePath) }
            audioExtractor = MediaExtractor().apply {
                if (sourceAudioUri.scheme == "file" && !sourceAudioUri.path.isNullOrBlank()) {
                    setDataSource(sourceAudioUri.path!!)
                } else {
                    setDataSource(context, sourceAudioUri, null)
                }
            }

            var vTrackIdx = -1
            var vFormat: MediaFormat? = null
            for (i in 0 until videoExtractor.trackCount) {
                val fmt = videoExtractor.getTrackFormat(i)
                if (fmt.getString(MediaFormat.KEY_MIME)?.startsWith("video/") == true) {
                    vTrackIdx = i
                    vFormat = fmt
                    break
                }
            }

            var aTrackIdx = -1
            var aFormat: MediaFormat? = null
            for (i in 0 until audioExtractor.trackCount) {
                val fmt = audioExtractor.getTrackFormat(i)
                val mime = fmt.getString(MediaFormat.KEY_MIME) ?: ""
                if (mime.startsWith("audio/")) {
                    aTrackIdx = i
                    aFormat = fmt
                    break
                }
            }

            if (vTrackIdx < 0 || vFormat == null || aTrackIdx < 0 || aFormat == null) return false

            val audioMime = aFormat.getString(MediaFormat.KEY_MIME) ?: return false
            val sampleRate = if (aFormat.containsKey(MediaFormat.KEY_SAMPLE_RATE)) aFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE) else 44100
            val channelCount = if (aFormat.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) aFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT).coerceIn(1, 2) else 2

            decoder = MediaCodec.createDecoderByType(audioMime).apply {
                configure(aFormat, null, null, 0)
                start()
            }

            val encFormat = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, sampleRate, channelCount).apply {
                setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
                setInteger(MediaFormat.KEY_BIT_RATE, 128_000)
                setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 64 * 1024)
            }
            encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC).apply {
                configure(encFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
                start()
            }

            muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            val muxVideoTrack = muxer.addTrack(vFormat)
            var muxAudioTrack = -1

            val startUs = trimStartMs.coerceAtLeast(0L) * 1000L
            val endUs = trimEndMs.coerceAtLeast(trimStartMs + 1000L) * 1000L
            val totalOutputDurationUs = trimmedDurationMs.coerceAtLeast(1000L) * 1000L
            val maxAudioFrames = ((totalOutputDurationUs * sampleRate) / 1_000_000L).coerceAtLeast(sampleRate.toLong())

            audioExtractor.selectTrack(aTrackIdx)
            if (startUs > 0L) {
                audioExtractor.seekTo(startUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
            }

            val dspProcessor = PcmVoiceDspProcessor(
                sampleRate = sampleRate,
                channels = channelCount,
                voiceEffect = project.voiceChangerEffect,
                pitch = project.audioPitch,
                gain = (project.videoVolume * project.audioVolumeBoost).coerceIn(0f, 2.0f),
                fadeIn = project.audioFadeIn,
                fadeOut = project.audioFadeOut,
                totalDurationUs = totalOutputDurationUs
            )

            val decInfo = MediaCodec.BufferInfo()
            val encInfo = MediaCodec.BufferInfo()
            var decInputDone = false
            var decOutputDone = false
            var encOutputDone = false
            var encodedAudioSamples = 0L
            var pendingVideoCopied = false

            fun copyVideoIfMuxerReady() {
                if (muxerStarted && !pendingVideoCopied) {
                    val vBuf = ByteBuffer.allocate(512 * 1024)
                    val vInfo = MediaCodec.BufferInfo()
                    videoExtractor.selectTrack(vTrackIdx)
                    while (true) {
                        val sz = videoExtractor.readSampleData(vBuf, 0)
                        if (sz < 0) break
                        vInfo.offset = 0
                        vInfo.size = sz
                        vInfo.presentationTimeUs = videoExtractor.sampleTime
                        @Suppress("DEPRECATION")
                        vInfo.flags = videoExtractor.sampleFlags
                        muxer.writeSampleData(muxVideoTrack, vBuf, vInfo)
                        videoExtractor.advance()
                    }
                    pendingVideoCopied = true
                }
            }

            var safetyLoops = 0
            while (!encOutputDone && safetyLoops < 50_000) {
                safetyLoops++
                if (!decInputDone) {
                    val inIdx = decoder.dequeueInputBuffer(5_000)
                    if (inIdx >= 0) {
                        val inBuf = decoder.getInputBuffer(inIdx)
                        if (inBuf != null) {
                            val sz = audioExtractor.readSampleData(inBuf, 0)
                            val pts = audioExtractor.sampleTime
                            if (sz < 0 || pts > endUs || encodedAudioSamples >= maxAudioFrames) {
                                decoder.queueInputBuffer(inIdx, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                                decInputDone = true
                            } else {
                                decoder.queueInputBuffer(inIdx, 0, sz, pts, 0)
                                audioExtractor.advance()
                            }
                        }
                    }
                }

                if (!decOutputDone) {
                    val outIdx = decoder.dequeueOutputBuffer(decInfo, 5_000)
                    if (outIdx >= 0) {
                        val outBuf = decoder.getOutputBuffer(outIdx)
                        val isEos = (decInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0
                        if (outBuf != null && decInfo.size > 0 && decInfo.presentationTimeUs >= startUs && encodedAudioSamples < maxAudioFrames) {
                            val outPtsUs = (encodedAudioSamples * 1_000_000L) / sampleRate
                            val shortCount = decInfo.size / 2
                            val rawPcm = ShortArray(shortCount)
                            outBuf.position(decInfo.offset)
                            outBuf.order(java.nio.ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(rawPcm)
                            val pcm = if (kotlin.math.abs(speedFactor - 1.0f) > 0.03f) {
                                resampleInterleavedPcm(rawPcm, channelCount, speedFactor)
                            } else rawPcm
                            dspProcessor.processInPlace(pcm, outPtsUs)

                            var offsetShorts = 0
                            while (offsetShorts < pcm.size && encodedAudioSamples < maxAudioFrames) {
                                val encInIdx = encoder.dequeueInputBuffer(10_000)
                                if (encInIdx >= 0) {
                                    val encInBuf = encoder.getInputBuffer(encInIdx)
                                    if (encInBuf != null) {
                                        encInBuf.clear()
                                        val remainingAllowedShorts = ((maxAudioFrames - encodedAudioSamples) * channelCount).toInt().coerceAtLeast(0)
                                        val maxShorts = encInBuf.remaining() / 2
                                        val chunk = minOf(maxShorts, pcm.size - offsetShorts, remainingAllowedShorts)
                                        if (chunk <= 0) break
                                        encInBuf.order(java.nio.ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(pcm, offsetShorts, chunk)
                                        val ptsUs = (encodedAudioSamples * 1_000_000L) / sampleRate
                                        encoder.queueInputBuffer(encInIdx, 0, chunk * 2, ptsUs, 0)
                                        offsetShorts += chunk
                                        encodedAudioSamples += (chunk / channelCount)
                                    }
                                } else break
                            }
                        }
                        decoder.releaseOutputBuffer(outIdx, false)
                        if (isEos || encodedAudioSamples >= maxAudioFrames) {
                            decOutputDone = true
                            val encInIdx = encoder.dequeueInputBuffer(10_000)
                            if (encInIdx >= 0) {
                                val ptsUs = (encodedAudioSamples * 1_000_000L) / sampleRate
                                encoder.queueInputBuffer(encInIdx, 0, 0, ptsUs, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            }
                        }
                    }
                }

                val encOutIdx = encoder.dequeueOutputBuffer(encInfo, 5_000)
                if (encOutIdx == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    if (!muxerStarted) {
                        muxAudioTrack = muxer.addTrack(encoder.outputFormat)
                        muxer.start()
                        muxerStarted = true
                        copyVideoIfMuxerReady()
                    }
                } else if (encOutIdx >= 0) {
                    val encOutBuf = encoder.getOutputBuffer(encOutIdx)
                    if (encOutBuf != null && (encInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) == 0 && encInfo.size > 0 && muxerStarted && muxAudioTrack >= 0) {
                        encOutBuf.position(encInfo.offset)
                        encOutBuf.limit(encInfo.offset + encInfo.size)
                        muxer.writeSampleData(muxAudioTrack, encOutBuf, encInfo)
                    }
                    encoder.releaseOutputBuffer(encOutIdx, false)
                    if ((encInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        encOutputDone = true
                    }
                }
            }
            muxerStarted && pendingVideoCopied
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            try { decoder?.stop(); decoder?.release() } catch (_: Exception) {}
            try { encoder?.stop(); encoder?.release() } catch (_: Exception) {}
            try { videoExtractor?.release() } catch (_: Exception) {}
            try { audioExtractor?.release() } catch (_: Exception) {}
            try { if (muxerStarted) muxer?.stop(); muxer?.release() } catch (_: Exception) {}
        }
    }

    internal fun resampleInterleavedPcm(
        input: ShortArray,
        channels: Int,
        speedFactor: Float
    ): ShortArray {
        val ch = channels.coerceIn(1, 2)
        val inFrames = input.size / ch
        if (inFrames <= 1) return input
        val rate = speedFactor.coerceIn(0.25f, 3.0f)
        val outFrames = (inFrames / rate).toInt().coerceAtLeast(1)
        val output = ShortArray(outFrames * ch)
        for (outF in 0 until outFrames) {
            val srcPos = (outF * rate).coerceIn(0f, (inFrames - 1).toFloat())
            val i0 = srcPos.toInt()
            val i1 = (i0 + 1).coerceAtMost(inFrames - 1)
            val frac = srcPos - i0
            for (c in 0 until ch) {
                val s0 = input[i0 * ch + c].toFloat()
                val s1 = input[i1 * ch + c].toFloat()
                output[outF * ch + c] = (s0 + (s1 - s0) * frac).coerceIn(-32767f, 32767f).toInt().toShort()
            }
        }
        return output
    }

    internal class PcmVoiceDspProcessor(
        private val sampleRate: Int,
        private val channels: Int,
        private val voiceEffect: String,
        private val pitch: Float,
        private val gain: Float,
        private val fadeIn: Boolean,
        private val fadeOut: Boolean,
        private val totalDurationUs: Long
    ) {
        private val pitchBufSize = 2048
        private val halfBuf = pitchBufSize / 2
        private val pitchRing = Array(channels.coerceIn(1, 2)) { FloatArray(pitchBufSize) }
        private var writePos = 0
        private var phasor = 0f

        private val echoDelayLen = (sampleRate * 0.17f).toInt().coerceIn(512, 16000)
        private val echoRing = Array(channels.coerceIn(1, 2)) { FloatArray(echoDelayLen) }
        private var echoIdx = 0

        private var prevX = FloatArray(channels.coerceIn(1, 2))
        private var prevY = FloatArray(channels.coerceIn(1, 2))
        private var totalFramesProcessed = 0L

        fun processInPlace(samples: ShortArray, startPtsUs: Long) {
            val effPitch = pitch.coerceIn(0.5f, 1.8f)
            val doPitch = kotlin.math.abs(effPitch - 1.0f) > 0.03f
            val frames = samples.size / channels.coerceAtLeast(1)
            val fadeDurUs = 600_000L

            for (f in 0 until frames) {
                val curPtsUs = startPtsUs + ((f * 1_000_000L) / sampleRate)
                var env = gain
                if (fadeIn && curPtsUs < fadeDurUs) {
                    env *= (curPtsUs.toFloat() / fadeDurUs).coerceIn(0.05f, 1f)
                }
                if (fadeOut && totalDurationUs > fadeDurUs && curPtsUs > totalDurationUs - fadeDurUs) {
                    env *= ((totalDurationUs - curPtsUs).toFloat() / fadeDurUs).coerceIn(0.05f, 1f)
                }

                for (ch in 0 until channels) {
                    val idx = f * channels + ch
                    var x = samples[idx].toFloat()

                    // 1. Delay-line crossfaded pitch shifter (duration-preserving)
                    if (doPitch) {
                        val ring = pitchRing[ch]
                        ring[writePos] = x
                        val tap1 = (writePos - (phasor * halfBuf) + pitchBufSize) % pitchBufSize
                        val ph2 = (phasor + 0.5f) % 1.0f
                        val tap2 = (writePos - (ph2 * halfBuf) + pitchBufSize) % pitchBufSize
                        val i1 = tap1.toInt().coerceIn(0, pitchBufSize - 1)
                        val i2 = tap2.toInt().coerceIn(0, pitchBufSize - 1)
                        val w1 = 1f - kotlin.math.abs(2f * phasor - 1f)
                        x = ring[i1] * w1 + ring[i2] * (1f - w1)
                    }

                    // 2. Voice character FX (Classic + ChatGPT AI Voice Personas)
                    x = when (voiceEffect) {
                        "ROBOT" -> {
                            val mod = kotlin.math.sin(2.0 * Math.PI * 42.0 * totalFramesProcessed / sampleRate).toFloat()
                            x * mod * 1.15f
                        }
                        "RADIO_MIC", "MEGAPHONE" -> {
                            val hp = 0.85f * (prevY[ch] + x - prevX[ch])
                            prevX[ch] = x
                            prevY[ch] = hp
                            val norm = (hp / 32767f) * 2.2f
                            norm.coerceIn(-0.6f, 0.6f) * 32767f * 1.35f
                        }
                        "ECHO_STUDIO" -> {
                            val er = echoRing[ch]
                            val delayed = er[echoIdx]
                            val out = x * 0.78f + delayed * 0.42f
                            er[echoIdx] = out.coerceIn(-32767f, 32767f)
                            out
                        }
                        "CHATGPT_COVE", "CHATGPT_SOL" -> {
                            // Warm conversational AI low-shelf + soft studio compression & micro-room sheen
                            val warmLp = prevY[ch] * 0.42f + x * 0.58f
                            prevY[ch] = warmLp
                            val er = echoRing[ch]
                            val studioAir = er[(echoIdx + echoDelayLen / 4) % echoDelayLen]
                            val blended = (x * 0.55f + warmLp * 0.52f + studioAir * 0.14f)
                            er[echoIdx] = x.coerceIn(-32767f, 32767f)
                            kotlin.math.tanh((blended / 24576f).toDouble()).toFloat() * 26500f
                        }
                        "CHATGPT_BREEZE", "CHATGPT_SKY", "CHATGPT_JUNIPER" -> {
                            // Bright conversational vocal presence + crisp studio reflection
                            val hpAir = (x - prevX[ch]) * 0.35f
                            prevX[ch] = x
                            val er = echoRing[ch]
                            val microEcho = er[(echoIdx + echoDelayLen / 5) % echoDelayLen]
                            er[echoIdx] = x.coerceIn(-32767f, 32767f)
                            (x * 0.94f + hpAir + microEcho * 0.16f)
                        }
                        "CHATGPT_EMBER" -> {
                            // Punchy confident broadcast anchor compression + crisp presence
                            val hpPunch = (x - prevX[ch]) * 0.25f
                            prevX[ch] = x
                            kotlin.math.tanh(((x + hpPunch) / 21000f).toDouble()).toFloat() * 28500f
                        }
                        "DEEP_ANCHOR", "SLOW_CINEMA" -> {
                            val lp = prevY[ch] * 0.35f + x * 0.65f
                            prevY[ch] = lp
                            lp * 1.1f
                        }
                        else -> x
                    }

                    samples[idx] = (x * env).coerceIn(-32767f, 32767f).toInt().toShort()
                }

                if (doPitch) {
                    writePos = (writePos + 1) % pitchBufSize
                    phasor = (phasor + (1.0f - effPitch) / halfBuf) % 1.0f
                    if (phasor < 0f) phasor += 1.0f
                }
                if (voiceEffect == "ECHO_STUDIO" || voiceEffect.startsWith("CHATGPT_")) {
                    echoIdx = (echoIdx + 1) % echoDelayLen
                }
                totalFramesProcessed++
            }
        }
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

    private fun fillYuvImage(image: Image, argb: IntArray, width: Int, height: Int) {
        val planes = image.planes
        val yPlane = planes[0]
        val uPlane = planes[1]
        val vPlane = planes[2]

        val yBuf = yPlane.buffer
        val uBuf = uPlane.buffer
        val vBuf = vPlane.buffer

        val yRowStride = yPlane.rowStride
        val yPixelStride = yPlane.pixelStride
        val uRowStride = uPlane.rowStride
        val uPixelStride = uPlane.pixelStride
        val vRowStride = vPlane.rowStride
        val vPixelStride = vPlane.pixelStride

        var index = 0
        for (j in 0 until height) {
            val yRowOffset = j * yRowStride
            val uvRow = j shr 1
            val uRowOffset = uvRow * uRowStride
            val vRowOffset = uvRow * vRowStride

            for (i in 0 until width) {
                val c = argb[index++]
                val r = (c shr 16) and 0xff
                val g = (c shr 8) and 0xff
                val b = c and 0xff

                val y = ((66 * r + 129 * g + 25 * b + 128) shr 8) + 16
                yBuf.put(yRowOffset + i * yPixelStride, y.coerceIn(0, 255).toByte())

                if ((j and 1) == 0 && (i and 1) == 0) {
                    val uvCol = i shr 1
                    val u = ((-38 * r - 74 * g + 112 * b + 128) shr 8) + 128
                    val v = ((112 * r - 94 * g - 18 * b + 128) shr 8) + 128
                    uBuf.put(uRowOffset + uvCol * uPixelStride, u.coerceIn(0, 255).toByte())
                    vBuf.put(vRowOffset + uvCol * vPixelStride, v.coerceIn(0, 255).toByte())
                }
            }
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

                val y = ((66 * r + 129 * g + 25 * b + 128) shr 8) + 16
                val u = ((-38 * r - 74 * g + 112 * b + 128) shr 8) + 128
                val v = ((112 * r - 94 * g - 18 * b + 128) shr 8) + 128

                yuv420sp[yIndex++] = y.coerceIn(0, 255).toByte()
                if (j % 2 == 0 && i % 2 == 0) {
                    yuv420sp[uvIndex++] = u.coerceIn(0, 255).toByte()
                    yuv420sp[uvIndex++] = v.coerceIn(0, 255).toByte()
                }
            }
        }
    }

    private fun encodeYUV420Planar(yuv420p: ByteArray, argb: IntArray, width: Int, height: Int) {
        val frameSize = width * height
        val chromaSize = frameSize / 4
        var yIndex = 0
        var uIndex = frameSize
        var vIndex = frameSize + chromaSize
        var index = 0
        for (j in 0 until height) {
            for (i in 0 until width) {
                val c = argb[index++]
                val r = (c shr 16) and 0xff
                val g = (c shr 8) and 0xff
                val b = c and 0xff

                val y = ((66 * r + 129 * g + 25 * b + 128) shr 8) + 16
                val u = ((-38 * r - 74 * g + 112 * b + 128) shr 8) + 128
                val v = ((112 * r - 94 * g - 18 * b + 128) shr 8) + 128

                yuv420p[yIndex++] = y.coerceIn(0, 255).toByte()
                if (j % 2 == 0 && i % 2 == 0) {
                    yuv420p[uIndex++] = u.coerceIn(0, 255).toByte()
                    yuv420p[vIndex++] = v.coerceIn(0, 255).toByte()
                }
            }
        }
    }
}
