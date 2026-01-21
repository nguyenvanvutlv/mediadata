package com.nvv.mediadata.view.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nvv.mediadata.R
import com.nvv.mediadata.view.core.LegalBulletPoint
import com.nvv.mediadata.view.core.LegalImportantBox
import com.nvv.mediadata.view.core.LegalSectionTitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicy(
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.privacy_policy_title))
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Privacy Policy",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Last Updated: January 2026",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            LegalImportantBox(
                title = "Summary",
                content = "MediaData Player does not collect, store, or share any personal data. All operations happen locally on your device.",
                containerColor = Color(0xFFFFF3CD),
                contentColor = Color(0xFF856404)
            )

            LegalSectionTitle("1. Data Collection")
            Text(
                text = "MediaData Player is committed to protecting user privacy. We want to be clear about what data we do and do not collect:",
                style = MaterialTheme.typography.bodyMedium
            )
            LegalBulletPoint("We DO NOT collect: Personal information, User IDs, Location data, Browsing history, Financial information, Photos/Videos, or Contact lists.")
            LegalBulletPoint("What happens on your device: All media playback occurs locally, file management is done within your device storage, streaming URLs are processed in real-time, and settings are stored locally.")

            LegalImportantBox(
                title = "Content Disclaimer",
                content = "MediaData Player is a media player tool. It does not provide any media content or streaming services. Users are solely responsible for providing their own content and ensuring they have the legal right to view such content.",
                containerColor = Color(0xFFFFF3CD),
                contentColor = Color(0xFF856404)
            )

            LegalSectionTitle("2. Local Storage")
            Text(
                text = "The app requires access to your device storage solely for playing local media files you select, saving network media to user-designated folders, and storing app preferences.",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Important: You have full control over what files the app can access.",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )

            LegalSectionTitle("3. Internet Connectivity")
            Text(
                text = "The app requires internet permission for streaming content from user-provided URLs, casting to Google Cast devices, and saving network media to local storage.",
                style = MaterialTheme.typography.bodyMedium
            )
            LegalBulletPoint("What we DON'T do: We don't send data to our servers, track URLs, monitor streaming habits, or share activity with third parties.")

            LegalSectionTitle("4. Third-Party Services")
            Text(
                text = "MediaData Player uses Google Cast (for TV casting), Android DownloadManager (for saving files), and Media3/ExoPlayer (for playback). We do not use any analytics or advertising SDKs.",
                style = MaterialTheme.typography.bodyMedium
            )

            LegalSectionTitle("5. Permissions Explained")
            PermissionRowContent("INTERNET", "Required for streaming and saving network media")
            PermissionRowContent("FOREGROUND_SERVICE_MEDIA_PLAYBACK", "Maintains playback during casting and background operation to ensure uninterrupted playback")
            PermissionRowContent("POST_NOTIFICATIONS", "Shows save progress and playback controls")
            PermissionRowContent("Storage Access", "Read and write media files (user-granted folders only)")

            LegalSectionTitle("6. Data Security")
            LegalBulletPoint("All data remains on your device")
            LegalBulletPoint("Network streams use HTTPS when available")
            LegalBulletPoint("No cloud storage or external databases")
            LegalBulletPoint("No authentication or user accounts")

            LegalSectionTitle("7. Age Requirement and Children's Privacy")
            Text(
                text = "MediaData Player is intended for users at least 13 years of age. We do not knowingly collect personal information from children under 13.",
                style = MaterialTheme.typography.bodyMedium
            )

            LegalSectionTitle("8. Open Source")
            Text(
                text = "MediaData Player is open source (Apache License 2.0). You can review the source code on GitHub: https://github.com/nguyenvanvutlv/mediadata",
                style = MaterialTheme.typography.bodyMedium
            )

            LegalSectionTitle("9. Changes to This Policy")
            Text(
                text = "We may update this policy. Continued use of the app after changes constitutes acceptance of the updated policy.",
                style = MaterialTheme.typography.bodyMedium
            )

            LegalSectionTitle("10. User Rights")
            Text(
                text = "Since we don't collect data, there is no data to access, delete, or export. You have complete control through your device settings.",
                style = MaterialTheme.typography.bodyMedium
            )

            LegalSectionTitle("11. Contact Information")
            Text(
                text = "Developer: Vũ Nguyễn Văn\nEmail: nguyenvanvu.tlvnvv@gmail.com\nGitHub: https://github.com/nguyenvanvutlv",
                style = MaterialTheme.typography.bodyMedium
            )

            LegalSectionTitle("12. Legal Compliance")
            Text(
                text = "This app complies with Google Play Developer Program Policies, GDPR, CCPA, and COPPA.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "© 2026 MediaData Player. Licensed under Apache License 2.0.\nThis is a media player tool. Users are responsible for content they access.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun PermissionRowContent(name: String, purpose: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
        Text(text = purpose, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
