# Publish an Image as a video stream

1. Edit MainActivity.kt and add the token, session id and apikey
2. Run the app and join from Vonage Video playground to see the published stream.

## How it works

1. This sample uses a preloaded image. Vonage.png is added to the project resources
2. At run time, we convert this image into a Bitmap and then to an Int array
3. We use this Int array as our video source and publish using custom video capturer.
4. We have used frame rate = 1 as no motion is needed.
