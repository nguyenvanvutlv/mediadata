# MediaData Player

A professional media player application for Android, built on [Media3](https://github.com/androidx/media) framework. Compatible with Android 8+.

## Overview

MediaData Player is a **technical tool** designed for playing local media files and streaming content from legitimate sources. This application provides advanced playback capabilities while maintaining full compliance with content policies and copyright regulations.

**Important Notice**: This app is a media player only. It does not host, provide, or facilitate access to any copyrighted content. Users are solely responsible for ensuring they have legal rights to access and play any content through this application.

## Key Features

### Media Playback
- **Advanced Player**: Built on Media3 (ExoPlayer) with hardware acceleration support
- **Multiple Format Support**: Comprehensive codec compatibility for modern media formats
- **Picture-in-Picture**: Continue watching in a small window
- **Subtitle Support**: Customizable text, size, color, and position

### Casting & Connectivity
- **Google Cast Integration**: Stream to compatible TV devices on your network
- **Automatic Format Detection**: Optimized codec delivery for best playback quality
- **Network Stream Playback**: Play content from direct media URLs (user-provided)

### User Controls
- **Gesture Controls**: Swipe horizontally to seek
- **Playback Speed**: Adjustable playback rates
- **Audio/Subtitle Selection**: Easy track switching during playback

### Local File Management
- **File Browser**: Access and organize media stored on your device
- **Storage Management**: Save network media to user-designated folders
- **Format Recognition**: Automatic media type detection and metadata parsing
- **File Operations**: Delete and manage your local media collection

## Supported Media Formats

### Video Codecs
H.263, H.264 (AVC), H.265 (HEVC), MPEG-4 SP, VP8, VP9, AV1

### Audio Codecs
Vorbis, Opus, FLAC, ALAC, PCM/WAVE, MP1, MP2, MP3, AMR, AAC, AC-3, E-AC-3, DTS, DTS-HD, TrueHD

### Containers
MP4, MOV, WebM, MKV, Ogg, MPEG-TS, MPEG-PS, FLV, AVI

### Streaming Protocols
DASH, HLS, SmoothStreaming, RTSP

## How to Use

### Playing Local Files
1. Navigate to the "Local Files" section
2. Select the folder containing your media
3. Tap any file to begin playback

### Network Streaming
1. Go to "Network Stream" section
2. Enter a direct media URL (must be a valid stream link)
3. Tap "Play" to start streaming

**Note**: You must have legal authorization to access any network content. This app does not provide, recommend, or facilitate access to any content sources.

### Saving Network Media
1. Go to "Save Media" section
2. Enter the media URL
3. Optionally provide a custom filename
4. Select save location (one-time folder permission)
5. Tap "Save Media" to save to local storage

**Note**: Only save content you have legal rights to. This is a management tool for your personal media library.

### Casting to TV
1. Ensure your device and TV are on the same Wi-Fi network
2. Tap the Cast icon in the player
3. Select your target device from the list
4. Playback will transfer to your TV automatically

### Customizing Subtitles
Access your device's **Caption preferences** under System Settings > Accessibility to customize:
- Text size and color
- Background opacity
- Font style
- Edge type

Long-press the subtitle button in the player for quick access.

## Required Permissions

- **FOREGROUND_SERVICE_MEDIA_PLAYBACK**: Maintains playback during casting and background operation
- **POST_NOTIFICATIONS**: Shows save progress and playback control notifications
- **Storage Access**: Required only for reading/writing user-selected local media files (folders you explicitly grant access to)

## Privacy & Data Policy

**MediaData Player respects your privacy**:
- **No Data Collection**: We do not collect, store, or transmit any personal information
- **No Tracking**: No analytics, advertisements, or third-party tracking SDKs
- **Local Processing**: All operations occur on your device
- **No Content Storage**: We do not host or provide any media content
- **Transparent Permissions**: Only essential permissions required for core functionality

For complete details, see our [Privacy Policy](https://nguyenvanvutlv.github.io/mediadata/privacy-policy.html) and [Terms of Service](https://nguyenvanvutlv.github.io/mediadata/terms-of-service.html).

## User Responsibility

By using MediaData Player, you acknowledge and agree that:

1. **Legal Compliance**: You are responsible for ensuring you have legal rights to all content accessed through this application
2. **Copyright Respect**: You will not use this tool to access, download, or distribute copyrighted material without proper authorization
3. **Content Source**: You must provide your own legitimate content sources and streaming URLs
4. **No Liability**: The developer is not responsible for user actions or content accessed through this application

## Technology Stack

- **Media3 (ExoPlayer)**: Google's advanced media playback library
- **Jetpack Compose**: Modern Android UI toolkit
- **Kotlin**: Primary development language
- **Hardware Acceleration**: Leverages device codecs for efficient playback
- **Material Design 3**: Follows latest design guidelines
- **PRDownloader**: Robust file downloading engine

## Compliance & Policies

This application is designed to comply with:
- Google Play Developer Program Policies
- Digital Millennium Copyright Act (DMCA)
- International copyright laws and regulations
- User privacy protection standards

## System Requirements

- **Minimum**: Android 8.0 (API 26)
- **Recommended**: Android 10+ for optimal performance
- **Storage**: Variable based on cached content
- **Internet**: Required for network streaming features

## Support & Contact

For technical support, feature requests, or policy inquiries:
- **GitHub Issues**: [Issues](https://github.com/nguyenvanvutlv/mediadata/issues)
- **Email**: nguyenvanvu.tlvnvv@gmail.com
- **Privacy Policy**: [PrivacyPolicy](https://nguyenvanvutlv.github.io/mediadata/privacy-policy.html)
- **Terms of Service**: [TermsOfService](https://nguyenvanvutlv.github.io/mediadata/terms-of-service.html)

## License

Copyright (C) 2026  Vũ Nguyễn Văn

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.

### Third-Party Libraries

This project uses the following open-source libraries:
- **Media3 (ExoPlayer)** - Apache License 2.0
- **Jetpack Compose** - Apache License 2.0
- **Kotlin** - Apache License 2.0
- **PRDownloader** - Apache License 2.0

---

**Disclaimer**: MediaData Player is a media playback tool. It does not host, provide, endorse, or facilitate access to any specific content. All content accessed through this application is provided by users themselves. The developers bear no responsibility for content accessed, downloaded, or shared through this application. Users must comply with all applicable laws and respect intellectual property rights.
