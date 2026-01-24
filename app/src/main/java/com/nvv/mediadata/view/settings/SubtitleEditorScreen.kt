package com.nvv.mediadata.view.settings

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.util.TypedValue
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Subtitles
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.alpha
import androidx.core.graphics.createBitmap
import androidx.media3.common.text.Cue
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.CaptionStyleCompat
import androidx.media3.ui.SubtitleView
import com.nvv.mediadata.data.provide.rememberContext
import com.nvv.mediadata.data.provide.rememberPlayerViewModel
import com.nvv.mediadata.data.viewmodel.Settings
import com.nvv.mediadata.ui.theme.Media3Typography
import java.util.Locale
import android.graphics.Color as AndroidColor


@kotlin.OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubtitleEditorScreen(
	navController: androidx.navigation.NavController
) {
	val context = rememberContext()
	val playerViewModel = rememberPlayerViewModel()
	val supportedLanguages = remember {
		Locale.getAvailableLocales()
			.filter { it.language.isNotEmpty() && it.displayLanguage.isNotEmpty() }
			.distinctBy { it.language }
			.map { locale ->
				val displayName = try {
					locale.getDisplayName(locale).replaceFirstChar {
						if (it.isLowerCase()) it.titlecase(locale) else it.toString()
					}
				} catch (e: Exception) {
					locale.displayLanguage
				}
				displayName to locale.language
			}
			.sortedBy { it.first }
	}
	var preferredLanguage by remember {
		mutableStateOf(Settings.getLanguages(context))
	}
	var subtitleSize by remember {
		mutableFloatStateOf(Settings.getSubtitleSize(context))
	}
	var verticalOffset by remember {
		mutableFloatStateOf(Settings.getSubtitlePosition(context) * 100f)
	}
	var textColor by remember {
		mutableIntStateOf(Settings.getColor(context).toArgb())
	}
	var outlineColor by remember {
		mutableIntStateOf(AndroidColor.BLACK)
	}
	var backgroundColor by remember {
		mutableIntStateOf(AndroidColor.TRANSPARENT)
	}
	var subtitleFont by remember {
		mutableStateOf(Settings.getSubtitleFont(context))
	}
	var isPreferredLanguageExpanded by remember { mutableStateOf(false) }
	var isFontExpanded by remember { mutableStateOf(false) }
	var isTextColorPickerOpen by remember { mutableStateOf(false) }
	var isOutlineColorPickerOpen by remember { mutableStateOf(false) }
	var isBackgroundColorPickerOpen by remember { mutableStateOf(false) }
	val colorOptions = listOf(
		"White" to androidx.compose.ui.graphics.Color.White,
		"Black" to androidx.compose.ui.graphics.Color.Black,
		"Red" to androidx.compose.ui.graphics.Color.Red,
		"Yellow" to androidx.compose.ui.graphics.Color.Yellow,
		"Blue" to androidx.compose.ui.graphics.Color.Blue,
		"Green" to androidx.compose.ui.graphics.Color.Green,
		"Magenta" to androidx.compose.ui.graphics.Color.Magenta,
		"Transparent" to androidx.compose.ui.graphics.Color.Transparent
	)
	LaunchedEffect(subtitleSize) {
		Settings.setSubtitleSize(context, subtitleSize)
	}
	LaunchedEffect(verticalOffset) {
		Settings.setSubtitlePosition(context, verticalOffset / 100f)
	}
	LaunchedEffect(textColor) {
		Settings.setColor(context, AndroidColor.valueOf(textColor))
		playerViewModel.updateSubtitleSettings()
	}
	LaunchedEffect(outlineColor) {
		Settings.setSubtitleOutlineColor(context, outlineColor)
		playerViewModel.updateSubtitleSettings()
	}
	LaunchedEffect(backgroundColor) {
		Settings.setSubtitleBackgroundColor(context, backgroundColor)
		playerViewModel.updateSubtitleSettings()
	}
	LaunchedEffect(subtitleFont) {
		Settings.setSubtitleFont(context, subtitleFont)
		playerViewModel.updateSubtitleSettings()
	}
	LaunchedEffect(subtitleSize) {
		Settings.setSubtitleSize(context, subtitleSize)
		playerViewModel.updateSubtitleSettings()
	}
	LaunchedEffect(verticalOffset) {
		Settings.setSubtitlePosition(context, verticalOffset / 100f)
		playerViewModel.updateSubtitleSettings()
	}
	Scaffold(
		topBar = {
			TopAppBar(
				title = {
					Row(
						verticalAlignment = Alignment.CenterVertically,
						horizontalArrangement = Arrangement.Start
					) {
						Icon(
							imageVector = Icons.Rounded.Subtitles,
							contentDescription = null,
							modifier = Modifier.size(24.dp)
						)
						Spacer(modifier = Modifier.width(8.dp))
						Text(
							text = "SUBTITLES",
							style = MaterialTheme.typography.titleLarge.copy(
								fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
							)
						)
					}
				},
				navigationIcon = {
					IconButton(onClick = { navController.popBackStack() }) {
						Icon(
							imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
							contentDescription = "Back"
						)
					}
				}
			)
		}
	) { paddingValues ->
		Surface(
			modifier = Modifier
				.fillMaxSize()
				.padding(paddingValues)
		) {
			Column(
				modifier = Modifier
					.fillMaxSize()
					.padding(horizontal = 16.dp)
					.verticalScroll(rememberScrollState())
			) {
				Spacer(modifier = Modifier.height(16.dp))
				Text(
					text = "Preferred Language",
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					modifier = Modifier.padding(bottom = 8.dp)
				)
				LanguageDropdown(
					selectedLanguage = preferredLanguage,
					languages = supportedLanguages,
					expanded = isPreferredLanguageExpanded,
					onExpandedChange = { isPreferredLanguageExpanded = it },
					onLanguageSelected = { code ->
						preferredLanguage = code
						Settings.setLanguages(context, code)
						isPreferredLanguageExpanded = false
					}
				)
				Spacer(modifier = Modifier.height(16.dp))
				SubtitlePreviewView(
					subtitleSize = subtitleSize,
					verticalOffset = verticalOffset,
					textColor = textColor,
					outlineColor = outlineColor,
					backgroundColor = backgroundColor,
					modifier = Modifier
						.fillMaxWidth()
						.height(200.dp)
				)
				Spacer(modifier = Modifier.height(24.dp))
				SettingRow(
					label = "Size",
					value = "${subtitleSize.toInt()}%",
					onDecrease = {
						if (subtitleSize > 10f) subtitleSize -= 5f
					},
					onIncrease = {
						if (subtitleSize < 200f) subtitleSize += 5f
					}
				)
				Spacer(modifier = Modifier.height(16.dp))
				SettingRow(
					label = "Vertical Offset",
					value = "${verticalOffset.toInt()}%",
					onDecrease = {
						if (verticalOffset > 0f) verticalOffset -= 5f
					},
					onIncrease = {
						if (verticalOffset < 50f) verticalOffset += 5f
					}
				)
				Spacer(modifier = Modifier.height(16.dp))
				Text(
					text = "Font",
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					modifier = Modifier.padding(bottom = 8.dp)
				)
				FontDropdown(
					selectedFont = subtitleFont,
					fonts = Media3Typography.FontFamilyType.entries.map {
						it.name.replace("_", " ") to it.name
					},
					expanded = isFontExpanded,
					onExpandedChange = { isFontExpanded = it },
					onFontSelected = { font ->
						subtitleFont = font
						Settings.setSubtitleFont(context, font)
						isFontExpanded = false
					}
				)
				Spacer(modifier = Modifier.height(16.dp))
				ColorPickerRow(
					label = "Text Color",
					color = textColor,
					onClick = { isTextColorPickerOpen = true }
				)
				Spacer(modifier = Modifier.height(16.dp))
				ColorPickerRow(
					label = "Outline Color",
					color = outlineColor,
					onClick = { isOutlineColorPickerOpen = true }
				)
				Spacer(modifier = Modifier.height(16.dp))
				ColorPickerRow(
					label = "Background Color",
					color = backgroundColor,
					onClick = { isBackgroundColorPickerOpen = true }
				)
				Spacer(modifier = Modifier.height(16.dp))
			}
		}
		ColorPickerDialog(
			colors = colorOptions,
			expanded = isTextColorPickerOpen,
			onDismiss = { isTextColorPickerOpen = false },
			onColorSelected = { color ->
				textColor = color.toArgb()
				isTextColorPickerOpen = false
			}
		)
		ColorPickerDialog(
			colors = colorOptions,
			expanded = isOutlineColorPickerOpen,
			onDismiss = { isOutlineColorPickerOpen = false },
			onColorSelected = { color ->
				outlineColor = color.toArgb()
				isOutlineColorPickerOpen = false
			}
		)
		ColorPickerDialog(
			colors = colorOptions,
			expanded = isBackgroundColorPickerOpen,
			onDismiss = { isBackgroundColorPickerOpen = false },
			onColorSelected = { color ->
				backgroundColor = color.toArgb()
				isBackgroundColorPickerOpen = false
			}
		)
	}
}

@Composable
fun FontDropdown(
	selectedFont: String,
	fonts: List<Pair<String, String>>,
	expanded: Boolean,
	onExpandedChange: (Boolean) -> Unit,
	onFontSelected: (String) -> Unit
) {
	val selectedDisplayName = fonts.find { it.second == selectedFont }?.first ?: "Default"
	Box {
		Card(
			modifier = Modifier
				.fillMaxWidth()
				.clickable { onExpandedChange(true) },
			colors = CardDefaults.cardColors(
				containerColor = MaterialTheme.colorScheme.surfaceVariant
			),
			shape = RoundedCornerShape(8.dp)
		) {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 16.dp, vertical = 12.dp),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically
			) {
				Text(
					text = selectedDisplayName,
					style = MaterialTheme.typography.bodyLarge
				)
				Icon(
					imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
					contentDescription = null,
					modifier = Modifier
						.size(16.dp)
						.rotate(90f)
				)
			}
		}

		DropdownMenu(
			expanded = expanded,
			onDismissRequest = { onExpandedChange(false) },
			modifier = Modifier.fillMaxWidth()
		) {
			fonts.forEach { (displayName, fontName) ->
				DropdownMenuItem(
					text = { Text(displayName) },
					onClick = {
						onFontSelected(fontName)
					}
				)
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageDropdown(
	selectedLanguage: String,
	languages: List<Pair<String, String>>,
	expanded: Boolean,
	onExpandedChange: (Boolean) -> Unit,
	onLanguageSelected: (String) -> Unit
) {
	val selectedDisplayName = languages.find { it.second == selectedLanguage }?.first ?: "None"
	Box {
		Card(
			modifier = Modifier
				.fillMaxWidth()
				.clickable { onExpandedChange(true) },
			colors = CardDefaults.cardColors(
				containerColor = MaterialTheme.colorScheme.surfaceVariant
			),
			shape = RoundedCornerShape(8.dp)
		) {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 16.dp, vertical = 12.dp),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically
			) {
				Text(
					text = selectedDisplayName,
					style = MaterialTheme.typography.bodyLarge
				)
				Icon(
					imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
					contentDescription = null,
					modifier = Modifier
						.size(16.dp)
						.rotate(90f)
				)
			}
		}

		DropdownMenu(
			expanded = expanded,
			onDismissRequest = { onExpandedChange(false) },
			modifier = Modifier.fillMaxWidth()
		) {
			languages.forEach { (displayName, code) ->
				DropdownMenuItem(
					text = { Text(displayName) },
					onClick = {
						onLanguageSelected(code)
					}
				)
			}
		}
	}
}

@Composable
fun SettingRow(
	label: String,
	value: String,
	onDecrease: () -> Unit,
	onIncrease: () -> Unit
) {
	Row(
		modifier = Modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically
	) {
		Text(
			text = label,
			style = MaterialTheme.typography.bodyLarge
		)
		Row(
			horizontalArrangement = Arrangement.Start,
			verticalAlignment = Alignment.CenterVertically
		) {
			IconButton(
				onClick = onDecrease,
				modifier = Modifier
					.size(40.dp)
					.background(
						MaterialTheme.colorScheme.surfaceVariant,
						CircleShape
					)
			) {
				Icon(
					imageVector = Icons.Rounded.Remove,
					contentDescription = "Decrease"
				)
			}
			Spacer(modifier = Modifier.width(8.dp))
			Text(
				text = value,
				style = MaterialTheme.typography.bodyLarge,
				modifier = Modifier.width(60.dp),
				textAlign = TextAlign.Center
			)
			Spacer(modifier = Modifier.width(8.dp))
			IconButton(
				onClick = onIncrease,
				modifier = Modifier
					.size(40.dp)
					.background(
						MaterialTheme.colorScheme.surfaceVariant,
						CircleShape
					)
			) {
				Icon(
					imageVector = Icons.Rounded.Add,
					contentDescription = "Increase"
				)
			}
		}
	}
}

@Composable
fun ColorPickerRow(
	label: String,
	color: Int,
	onClick: () -> Unit
) {
	Row(
		modifier = Modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically
	) {
		Text(
			text = label,
			style = MaterialTheme.typography.bodyLarge
		)
		Card(
			modifier = Modifier
				.size(48.dp, 32.dp)
				.clickable(onClick = onClick),
			colors = CardDefaults.cardColors(
				containerColor = androidx.compose.ui.graphics.Color(color)
			),
			shape = RoundedCornerShape(8.dp),
			border = if (color.alpha < 0.1f) {
				BorderStroke(
					1.dp,
					MaterialTheme.colorScheme.outline
				)
			} else null
		) {}
	}
}

@Composable
fun ColorPickerDialog(
	colors: List<Pair<String, androidx.compose.ui.graphics.Color>>,
	expanded: Boolean,
	onDismiss: () -> Unit,
	onColorSelected: (androidx.compose.ui.graphics.Color) -> Unit
) {
	DropdownMenu(
		expanded = expanded,
		onDismissRequest = onDismiss
	) {
		colors.forEach { (name, color) ->
			DropdownMenuItem(
				text = { Text(name) },
				leadingIcon = {
					Box(
						modifier = Modifier
							.size(24.dp)
							.background(
								color = color,
								shape = CircleShape
							)
					)
				},
				onClick = {
					onColorSelected(color)
				}
			)
		}
	}
}

@OptIn(UnstableApi::class)
@Composable
fun SubtitlePreviewView(
	subtitleSize: Float,
	verticalOffset: Float,
	textColor: Int,
	outlineColor: Int,
	backgroundColor: Int,
	modifier: Modifier = Modifier
) {
	val context = LocalContext.current
	val currentFont = remember { Settings.getSubtitleFont(context) }

	AndroidView(
		factory = { ctx ->
			val width = 1280
			val height = 720
			val bitmap = createBitmap(width, height)
			val canvas = Canvas(bitmap)
			val skyPaint = Paint().apply {
				shader = android.graphics.LinearGradient(
					0f, 0f, 0f, height * 0.6f,
					AndroidColor.rgb(135, 206, 250),
					AndroidColor.rgb(70, 130, 180),
					android.graphics.Shader.TileMode.CLAMP
				)
			}
			canvas.drawRect(0f, 0f, width.toFloat(), height * 0.6f, skyPaint)
			val groundPaint = Paint().apply {
				shader = android.graphics.LinearGradient(
					0f, height * 0.6f, 0f, height.toFloat(),
					AndroidColor.rgb(34, 139, 34),
					AndroidColor.rgb(85, 107, 47),
					android.graphics.Shader.TileMode.CLAMP
				)
			}
			canvas.drawRect(0f, height * 0.6f, width.toFloat(), height.toFloat(), groundPaint)
			val sunPaint = Paint().apply {
				color = AndroidColor.rgb(255, 255, 0)
				style = Paint.Style.FILL
			}
			canvas.drawCircle(width * 0.8f, height * 0.2f, 60f, sunPaint)
			val subtitleView = SubtitleView(ctx).apply {
				layoutParams = ViewGroup.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.MATCH_PARENT
				)
				setUserDefaultStyle()
				setUserDefaultTextSize()
			}

			// Set subtitle style
			val fontFamilyType = try {
				Media3Typography.FontFamilyType.valueOf(currentFont)
			} catch (e: Exception) {
				Media3Typography.FontFamilyType.DEFAULT
			}
			val typeface = Media3Typography.getTypeface(context, fontFamilyType) ?: Typeface.DEFAULT

			// CaptionStyleCompat: foregroundColor, backgroundColor (text bg), windowColor (area bg), edgeType, edgeColor, typeface
			// Only use windowColor for subtitle area background, not text background
			val windowColor = if (backgroundColor == AndroidColor.TRANSPARENT) {
				AndroidColor.TRANSPARENT
			} else {
				backgroundColor
			}
			val style = CaptionStyleCompat(
				textColor, // foregroundColor
				AndroidColor.TRANSPARENT, // No background for text characters
				windowColor, // windowColor (for subtitle area only)
				CaptionStyleCompat.EDGE_TYPE_OUTLINE,
				outlineColor, // edgeColor
				typeface
			)
			subtitleView.setStyle(style)
			subtitleView.setFixedTextSize(
				TypedValue.COMPLEX_UNIT_SP,
				subtitleSize
			)
			val bottomPadding = 0.05f + (verticalOffset / 100f) * 0.45f
			subtitleView.setBottomPaddingFraction(bottomPadding)
			val cue = Cue.Builder()
				.setText(android.text.SpannableString("This is a sample subtitles for preview"))
				.setPosition(0.5f) // Center horizontally
				.setPositionAnchor(Cue.ANCHOR_TYPE_MIDDLE)
				.setLine(1f - bottomPadding, Cue.LINE_TYPE_FRACTION) // Position from bottom
				.setLineAnchor(Cue.ANCHOR_TYPE_END)
				.setSize(0.9f) // 90% of screen width
				.setTextAlignment(android.text.Layout.Alignment.ALIGN_CENTER)
				.build()
			subtitleView.setCues(listOf(cue))
			android.widget.FrameLayout(ctx).apply {
				setBackgroundColor(AndroidColor.BLACK)
				addView(
					android.widget.ImageView(ctx).apply {
						setImageBitmap(bitmap)
						scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
						adjustViewBounds = true
					},
					ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.MATCH_PARENT
				)
				addView(subtitleView)
			}
		},
		update = { view ->
			val subtitleView = view.getChildAt(1) as? SubtitleView
			subtitleView?.let { sv ->
				val fontFamilyType = try {
					Media3Typography.FontFamilyType.valueOf(Settings.getSubtitleFont(context))
				} catch (e: Exception) {
					Media3Typography.FontFamilyType.DEFAULT
				}
				val typeface = Media3Typography.getTypeface(context, fontFamilyType) ?: Typeface.DEFAULT
				// CaptionStyleCompat: foregroundColor, backgroundColor (text bg), windowColor (area bg), edgeType, edgeColor, typeface
				// Only use windowColor for subtitle area background, not text background
				val windowColor = if (backgroundColor == AndroidColor.TRANSPARENT) {
					AndroidColor.TRANSPARENT
				} else {
					backgroundColor
				}
				val style = CaptionStyleCompat(
					textColor, // foregroundColor
					AndroidColor.TRANSPARENT, // No background for text characters
					windowColor, // windowColor (for subtitle area only)
					CaptionStyleCompat.EDGE_TYPE_OUTLINE,
					outlineColor, // edgeColor
					typeface
				)
				sv.setStyle(style)
				sv.setFixedTextSize(TypedValue.COMPLEX_UNIT_SP, subtitleSize)
				val bottomPadding = 0.05f + (verticalOffset / 100f) * 0.45f
				sv.setBottomPaddingFraction(bottomPadding)
				val cue = Cue.Builder()
					.setText(android.text.SpannableString("This is a sample subtitles for preview"))
					.setPosition(0.5f)
					.setPositionAnchor(Cue.ANCHOR_TYPE_MIDDLE)
					.setLine(1f - bottomPadding, Cue.LINE_TYPE_FRACTION)
					.setLineAnchor(Cue.ANCHOR_TYPE_END)
					.setSize(0.9f)
					.setTextAlignment(android.text.Layout.Alignment.ALIGN_CENTER)
					.build()
				sv.setCues(listOf(cue))
			}
		},
		modifier = modifier
			.clip(RoundedCornerShape(12.dp))
			.border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
	)
}
