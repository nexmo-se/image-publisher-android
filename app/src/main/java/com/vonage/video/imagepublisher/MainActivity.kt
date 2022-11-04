package com.vonage.video.imagepublisher

import android.content.ContentValues.TAG
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.opentok.*
import com.opentok.android.*


class MainActivity : AppCompatActivity() {
    private var token: String = "T1==cGFydG5lcl9pZD00NjE4MzQ1MiZzaWc9Yzk1ZDhmYzM4ZjY2ZmYxZjMwZWEyNTVlNDVlMDQzNjJlOGMwMDI4MDpzZXNzaW9uX2lkPTFfTVg0ME5qRTRNelExTW41LU1UWTJOelEyTVRVeE5ETXpPSDVPVlhkamFWbHVVRTlqZUVOb1pHdDVLM1pwZDJKWmExTi1mZyZjcmVhdGVfdGltZT0xNjY3NDYxNTE0Jm5vbmNlPTAuMjY4NDc2NjY0NjczMTY3MzMmcm9sZT1tb2RlcmF0b3ImZXhwaXJlX3RpbWU9MTY2ODA2NjMxNCZpbml0aWFsX2xheW91dF9jbGFzc19saXN0PQ=="
    private var sessionId: String = "1_MX40NjE4MzQ1Mn5-MTY2NzQ2MTUxNDMzOH5OVXdjaVluUE9jeENoZGt5K3Zpd2JZa1N-fg"
    private var apiKey: String = "46183452"
    private var pubLayout: FrameLayout? = null
    private var subLayout: FrameLayout? = null
    private var pub: Publisher? = null
    private var sub: Subscriber? = null
    private var session: Session? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        pubLayout = findViewById(R.id.publisherLayout)
        subLayout = findViewById(R.id.subscriberLayout)

        initializeSession(apiKey,sessionId,token)
    }

    private fun initializeSession(apiKey: String, sessionId: String, token: String) {
        Log.i(TAG, "apiKey: $apiKey")
        Log.i(TAG, "sessionId: $sessionId")
        Log.i(TAG, "token: $token")
        com.opentok.android.OpenTokConfig.setWebRTCLogs(true)
        com.opentok.android.OpenTokConfig.setOTKitLogs(true)
        com.opentok.android.OpenTokConfig.setJNILogs(true)
        session = Session.Builder(this, apiKey, sessionId).build().also {
            it.setSessionListener(sessionListener)
            it.connect(token)
        }
    }

    private val sessionListener: Session.SessionListener = object : Session.SessionListener {
        override fun onConnected(session: Session) {
            Log.d(TAG, "onConnected: Connected to session: ${session.sessionId}")
            var imageCapturer: ImageCapturer = ImageCapturer()
            /* load the image in to a bitmap */
            val rawImage = BitmapFactory.decodeResource(
                this@MainActivity.resources,
                R.drawable.vonage
            )
            imageCapturer.setBitmap(rawImage)

            pub = Publisher.Builder(this@MainActivity).capturer(imageCapturer).build()
            pub?.publishAudio = false
            pub?.setPublisherListener(publisherListerner)
            pub?.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);
            pubLayout?.addView(pub?.view)

            session.publish(pub)
        }

        override fun onDisconnected(session: Session) {
            Log.d(TAG, "onDisconnected: Disconnected from session: ${session.sessionId}")
        }

        override fun onStreamReceived(session: Session, stream: Stream) {
            Log.d(TAG, "onStreamReceived: New Stream Received ${stream.streamId} in session: ${session.sessionId}")
            if (sub == null) {
                sub = Subscriber.Builder(this@MainActivity, stream).build().also {
                    it.renderer?.setStyle(
                        BaseVideoRenderer.STYLE_VIDEO_SCALE,
                        BaseVideoRenderer.STYLE_VIDEO_FILL
                    )

                    it.setSubscriberListener(subscriberListener)
                }

                session.subscribe(sub)
                subLayout?.addView(sub?.view)
            }
        }

        override fun onStreamDropped(session: Session, stream: Stream) {
            Log.d(TAG, "onStreamDropped: Stream Dropped: ${stream.streamId} in session: ${session.sessionId}")
            if (sub != null) {
                sub = null
                subLayout?.removeAllViews()
            }
        }

        override fun onError(session: Session, opentokError: OpentokError) {
            finishWithMessage("Session error: ${opentokError.message}")
        }
    }

    var publisherListerner: PublisherKit.PublisherListener = object: PublisherKit.PublisherListener{
        override fun onStreamCreated(p0: PublisherKit?, p1: Stream?) {
            Log.i("PUBLISHERLOG","Stream created")
        }

        override fun onStreamDestroyed(p0: PublisherKit?, p1: Stream?) {
            Log.i("PUBLISHERLOG","Stream destroyed")
        }

        override fun onError(p0: PublisherKit?, p1: OpentokError?) {
           Log.e("PUBLISHERLOG", "Error message: " + p1?.message + " Code:" + p1?.errorCode)
        }

    }
    var subscriberListener: SubscriberKit.SubscriberListener = object : SubscriberKit.SubscriberListener {
        override fun onConnected(subscriberKit: SubscriberKit) {
            Log.d(TAG, "onConnected: Subscriber connected. Stream: ${subscriberKit.stream.streamId}")
        }

        override fun onDisconnected(subscriberKit: SubscriberKit) {
            Log.d(TAG, "onDisconnected: Subscriber disconnected. Stream: ${subscriberKit.stream.streamId}")
        }

        override fun onError(subscriberKit: SubscriberKit, opentokError: OpentokError) {
            finishWithMessage("SubscriberKit onError: ${opentokError.message}")
        }
    }

    private fun finishWithMessage(message: String) {
        Log.e(TAG, message)
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        finish()
    }
}