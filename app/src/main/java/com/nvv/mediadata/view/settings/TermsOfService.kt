package com.nvv.mediadata.view.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.nvv.mediadata.view.core.LegalSubSectionTitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsOfService(
	navController: NavController
) {
	Scaffold(
		topBar = {
			TopAppBar(
				title = {
					Text(stringResource(R.string.terms_title))
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
				text = "Terms of Service",
				style = MaterialTheme.typography.headlineMedium,
				fontWeight = FontWeight.Bold,
				color = Color(0xFFD32F2F)
			)
			Text(
				text = "Last Updated: January 2026",
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				modifier = Modifier.padding(vertical = 8.dp)
			)

			LegalImportantBox(
				title = "Important",
				content = "By using MediaData Player, you agree to these terms and acknowledge that you are solely responsible for the content you access and save using this application.",
				containerColor = Color(0xFFFFEBEE),
				contentColor = Color(0xFFB71C1C)
			)

			LegalSectionTitle("1. Acceptance of Terms")
			Text(
				text = "Welcome to MediaData Player. By installing, accessing, or using this application, you agree to be bound by these Terms of Service. If you do not agree, do not use the application.",
				style = MaterialTheme.typography.bodyMedium
			)

			LegalSectionTitle("2. Age Requirement")
			Text(
				text = "You must be at least 13 years of age to use MediaData Player. If you are under the age of majority, your parent or guardian must agree to these Terms on your behalf.",
				style = MaterialTheme.typography.bodyMedium
			)

			LegalSectionTitle("3. Description of Service")
			Text(
				text = "MediaData Player is a technical tool designed for:",
				style = MaterialTheme.typography.bodyMedium
			)
			LegalBulletPoint("Playing local media files stored on your device")
			LegalBulletPoint("Streaming content from user-provided network URLs")
			LegalBulletPoint("Saving network media to user-designated local storage")
			LegalBulletPoint("Casting media to compatible devices (Google Cast)")

			LegalImportantBox(
				title = "What This App Is NOT",
				content = "Not a content provider, not a streaming service, not a torrent client, and not affiliated with any content platforms.",
				containerColor = Color(0xFFE3F2FD),
				contentColor = Color(0xFF0D47A1)
			)

			LegalSectionTitle("4. User Responsibilities")
			LegalSubSectionTitle("4.1 Legal Compliance")
			Text("You are responsible for ensuring you have the legal right to all content you access, stream, or save. You must comply with all applicable copyright laws.")

			LegalSubSectionTitle("4.2 Prohibited Activities")
			Text("You agree NOT to use this app for: Copyright infringement, Piracy, Distribution of copyrighted content without permission, Circumvention of DRM, or Illegal activities.")

			LegalSubSectionTitle("4.3 Content Sources")
			Text("You must provide your own legitimate content URLs. The app does not recommend, provide, or endorse any content sources.")

			LegalSectionTitle("5. Intellectual Property")
			LegalBulletPoint("App Ownership: Owned by Vũ Nguyễn Văn, licensed under GNU General Public License v3.0 (GPL-3.0).")
			LegalBulletPoint("Third-Party Rights: We respect intellectual property and expect users to do the same.")
			LegalBulletPoint("DMCA: Contact us at nguyenvanvu.tlvnvv@gmail.com for concerns about misuse.")

			LegalSectionTitle("6. Disclaimers")
			Text("The application is provided 'AS IS' without warranties of any kind. We are NOT responsible for content you access, legality of sources, or copyright status of saved content.")

			LegalSectionTitle("7. Limitation of Liability")
			Text("To the maximum extent permitted by law, the developer shall NOT be liable for direct, indirect, or consequential damages arising from user misuse or illegal activity.")

			LegalSectionTitle("8. Indemnification")
			Text("You agree to indemnify and hold harmless the developer from any claims arising from your use or misuse of the application.")

			LegalSectionTitle("9. Termination")
			Text("We reserve the right to discontinue the app or refuse service to users violating these Terms.")

			LegalSectionTitle("10. Privacy")
			Text("Your use is also governed by our Privacy Policy. We do not collect personal data.")

			LegalSectionTitle("11. License")
			Text("MediaData Player is licensed under the GNU General Public License v3.0 (GPL-3.0). See LICENSE file for full terms. Source code is available at: https://github.com/nguyenvanvutlv/mediadata")

			LegalSectionTitle("12. Contact")
			Text("Questions? Contact us at: nguyenvanvu.tlvnvv@gmail.com")

			LegalImportantBox(
				title = "Final Reminder",
				content = "This app is a tool, not a content service. You are responsible for ensuring legal rights, complying with laws, and using the app ethically.",
				containerColor = Color(0xFFFFEBEE),
				contentColor = Color(0xFFB71C1C)
			)

			Spacer(modifier = Modifier.height(24.dp))
			HorizontalDivider()
			Spacer(modifier = Modifier.height(16.dp))
			Text(
				text = "© 2026 MediaData Player. Licensed under GNU General Public License v3.0 (GPL-3.0).\nBy using this app, you agree to these Terms of Service.",
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				modifier = Modifier.fillMaxWidth()
			)
			Spacer(modifier = Modifier.height(32.dp))
		}
	}
}
