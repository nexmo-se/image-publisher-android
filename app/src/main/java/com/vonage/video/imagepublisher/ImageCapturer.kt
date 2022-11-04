package com.vonage.video.imagepublisher

import android.R
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.opentok.android.BaseVideoCapturer
import android.os.Handler;
import android.util.Log

class ImageCapturer : BaseVideoCapturer(){
    private var rawBitMap: Bitmap? = null
    private var frame: IntArray? = null
    private var mHandler: Handler = Handler()
    private var fps: Int = 1
    private var capturing: Boolean = false
    override fun init() {

    }
    fun setBitmap(img: Bitmap){
        rawBitMap = img
        val width = rawBitMap!!.width
        val height = rawBitMap!!.height
        frame = IntArray(width * height)
        rawBitMap!!.getPixels(frame,0, width,0,0, width, height);
    }
    override fun startCapture(): Int {
        mHandler.postDelayed(newFrame, 1000L / fps);
        capturing = true
        return 0
    }

    val newFrame = object : Runnable {
        override fun run() {
            Log.d("CAPTURER","Sending frame")
            provideIntArrayFrame(frame, ARGB,rawBitMap!!.width,rawBitMap!!.height,0,false)
            mHandler.postDelayed(this,1000L / fps)
        }
    }

    override fun stopCapture(): Int {
        mHandler.removeCallbacks(newFrame);
        capturing = false
        return 0
    }

    override fun destroy() {

    }

    override fun isCaptureStarted(): Boolean {
        return capturing
    }

    override fun getCaptureSettings(): CaptureSettings {

        val settings:CaptureSettings = CaptureSettings()
        settings.height = rawBitMap!!.height
        settings.width = rawBitMap!!.width
        settings.fps = fps
        settings.expectedDelay = 0
        settings.format = ARGB
        settings.mirrorInLocalRender = false

        return settings
    }

    override fun onPause() {

    }

    override fun onResume() {

    }

}