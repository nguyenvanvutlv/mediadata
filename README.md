# Downloader Player

A powerful Android download manager and video player based on [Media3](https://github.com/androidx/media), compatible with Android 8+ and Android TV.

Downloader Player helps you download videos directly to your device as files, also features an advanced integrated player that supports almost all 
modern formats and seamless TV casting.

## Key Features

*   **Fast Video Downloader**: Quickly download videos directly to local storage and manage your download list with ease.
*   **Comprehensive Codec Support**: Built with the latest Media3 library, supporting high-end audio formats (AC3, DTS, TrueHD, etc.) and video codecs (H.264, H.265, AV1, etc.).
*   **Cast to TV**: Cast video and audio to your TV (Google Cast) with automatic format (MimeType) detection to ensure the best possible playback quality.
*   **Picture in Picture (PiP)**: Continue watching your videos in a small window while using other apps.
*   **Perfect Sync**: Optimized audio-to-video synchronization, with full support for Bluetooth earphones and speakers.
*   **Smart Controls**: Horizontal swipe to seek, and vertical swipe for brightness (left) and volume (right) adjustments.

## Supported Formats

*   **Video**: H.263, H.264 AVC, H.265 HEVC, MPEG-4 SP, VP8, VP9, AV1.
*   **Audio**: Vorbis, Opus, FLAC, ALAC, PCM/WAVE, MP1, MP2, MP3, AMR, AAC, AC-3, E-AC-3, DTS, DTS-HD, TrueHD, etc.
*   **Containers**: MP4, MOV, WebM, MKV, Ogg, MPEG-TS, FLV, AVI.
*   **Streaming**: DASH, HLS, SmoothStreaming, RTSP.
*   **Subtitles**: SRT, SSA/ASS, TTML, VTT, DVB.

## How to Use

### Download and Play
Downloader Manager is designed to handle direct streaming links. When you open a video link from a browser or another app, select Downloader Manager to start downloading or playing immediately.

### Casting to TV
Tap the Cast icon on the player to search for devices on the same Wi-Fi network. The app automatically analyzes the video's codec to ensure your TV can play both video and audio perfectly.

### Customize Subtitles
You can change subtitle size, color, and font in your device's system **Caption preferences** (usually under *Accessibility*). Long-press the subtitle button on the player for quick access to these settings.

## Permissions
*   **INTERNET**: Required to download and stream videos.
*   **FOREGROUND_SERVICE_MEDIA_PLAYBACK**: Required to maintain playback in the background or during Casting.
*   **WRITE_EXTERNAL_STORAGE**: Required to save downloaded video files to your device.

## Technology
This project is built on Jetpack Media3 (ExoPlayer), taking full advantage of hardware and software decoding capabilities on modern Android devices.
