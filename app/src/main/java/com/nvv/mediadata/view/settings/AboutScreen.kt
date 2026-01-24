package com.nvv.mediadata.view.settings

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.nvv.mediadata.BuildConfig
import com.nvv.mediadata.R
import com.nvv.mediadata.view.core.ItemNavigation

@Composable
fun AboutScreen(
	navController: NavController
) {
	val context = LocalContext.current
	val appName = stringResource(R.string.app_name)
	val privacyPolicyTitle = stringResource(R.string.privacy_policy_title)
	val termsTitle = stringResource(R.string.terms_title)

	Surface(
		modifier = Modifier.fillMaxSize()
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.verticalScroll(rememberScrollState())
				.padding(16.dp),
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			Spacer(Modifier.height(10.dp))
			Text(
				text = appName,
				style = MaterialTheme.typography.headlineMedium,
				fontWeight = FontWeight.Bold,
				color = MaterialTheme.colorScheme.onSurface
			)
			Spacer(Modifier.height(8.dp))
			Text(
				text = "Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
			Spacer(Modifier.height(8.dp))
			Card(
				modifier = Modifier.fillMaxWidth(),
				colors = CardDefaults.cardColors(
					containerColor = MaterialTheme.colorScheme.primaryContainer
				)
			) {
				Text(
					text = stringResource(R.string.short_detail),
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onPrimaryContainer,
					textAlign = TextAlign.Center,
					modifier = Modifier.padding(16.dp)
				)
			}
			Spacer(Modifier.height(32.dp))
			ItemNavigation(
				leading = {
					Icon(
						imageVector = Icons.Rounded.PrivacyTip,
						contentDescription = null,
						tint = MaterialTheme.colorScheme.primary
					)
				},
				trailing = {
					Icon(
						imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
						contentDescription = null
					)
				},
				headline = {
					Text(
						text = privacyPolicyTitle,
						style = MaterialTheme.typography.titleMedium
					)
				},
				supportingContent = {
					Text(
						text = stringResource(R.string.privacy_policy_support_content),
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onSurfaceVariant
					)
				}
			) {
				navController.navigate("settings/privacy")
			}
			Spacer(Modifier.height(8.dp))
			ItemNavigation(
				leading = {
					Icon(
						imageVector = Icons.Rounded.Gavel,
						contentDescription = null,
						tint = MaterialTheme.colorScheme.primary
					)
				},
				trailing = {
					Icon(
						imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
						contentDescription = null
					)
				},
				headline = {
					Text(
						text = termsTitle,
						style = MaterialTheme.typography.titleMedium
					)
				},
				supportingContent = {
					Text(
						text = stringResource(R.string.term_support_content),
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onSurfaceVariant
					)
				}
			) {
				navController.navigate("settings/terms")
			}

			Spacer(Modifier.height(8.dp))

			ItemNavigation(
				leading = {
					Icon(
						imageVector = Icons.Rounded.Code,
						contentDescription = null,
						tint = MaterialTheme.colorScheme.primary
					)
				},
				trailing = {
					Icon(
						imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
						contentDescription = null
					)
				},
				headline = {
					Text(
						text = "Open Source Licenses",
						style = MaterialTheme.typography.titleMedium
					)
				},
				supportingContent = {
					Text(
						text = "GPL-3.0 • View on GitHub",
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onSurfaceVariant
					)
				}
			) {
				val githubUrl = "https://github.com/nguyenvanvutlv/mediadata"
				val intent = Intent(Intent.ACTION_VIEW, githubUrl.toUri())
				context.startActivity(intent)
			}
			Spacer(Modifier.height(32.dp))
			Card(
				modifier = Modifier.fillMaxWidth(),
				colors = CardDefaults.cardColors(
					containerColor = MaterialTheme.colorScheme.surfaceVariant
				)
			) {
				Column(
					modifier = Modifier.padding(16.dp)
				) {
					Text(
						text = "Built with",
						style = MaterialTheme.typography.titleSmall,
						color = MaterialTheme.colorScheme.onSurfaceVariant
					)
					Spacer(Modifier.height(8.dp))
					Text(
						text = "• Media3 (ExoPlayer)\n• Jetpack Compose\n• Kotlin\n• Google Cast\n• Material Design 3",
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onSurfaceVariant
					)
				}
			}
			Spacer(Modifier.height(24.dp))
			Text(
				text = "© 2026 Vũ Nguyễn Văn",
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
			Text(
				text = "Licensed under GNU General Public License v3.0 (GPL-3.0)",
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
			Spacer(Modifier.height(32.dp))
		}
	}
}
