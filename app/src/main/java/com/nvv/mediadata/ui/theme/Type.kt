package com.nvv.mediadata.ui.theme

import android.content.Context
import android.graphics.Typeface
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.nvv.mediadata.R
import timber.log.Timber


// Font families for Compose
val AndadaProFontFamily = FontFamily(
	Font(R.font.andadapro_regular, FontWeight.Normal),
	Font(R.font.andadapro_medium, FontWeight.Medium),
	Font(R.font.andadapro_semibold, FontWeight.SemiBold),
	Font(R.font.andadapro_bold, FontWeight.Bold),
	Font(R.font.andadapro_extrabold, FontWeight.ExtraBold),
	Font(R.font.andadapro_italic, FontWeight.Normal, FontStyle.Italic),
	Font(R.font.andadapro_bolditalic, FontWeight.Bold, FontStyle.Italic)
)

val AsapCondensedFontFamily = FontFamily(
	Font(R.font.asapcondensed_regular, FontWeight.Normal),
	Font(R.font.asapcondensed_light, FontWeight.Light),
	Font(R.font.asapcondensed_medium, FontWeight.Medium),
	Font(R.font.asapcondensed_semibold, FontWeight.SemiBold),
	Font(R.font.asapcondensed_bold, FontWeight.Bold),
	Font(R.font.asapcondensed_extrabold, FontWeight.ExtraBold),
	Font(R.font.asapcondensed_black, FontWeight.Black),
	Font(R.font.asapcondensed_italic, FontWeight.Normal, FontStyle.Italic),
	Font(R.font.asapcondensed_bolditalic, FontWeight.Bold, FontStyle.Italic)
)

val GoogleSansFlexFontFamily = FontFamily(
	Font(R.font.googlesansflex_24pt_regular, FontWeight.Normal),
	Font(R.font.googlesansflex_24pt_thin, FontWeight.Thin),
	Font(R.font.googlesansflex_24pt_extralight, FontWeight.ExtraLight),
	Font(R.font.googlesansflex_24pt_light, FontWeight.Light),
	Font(R.font.googlesansflex_24pt_medium, FontWeight.Medium),
	Font(R.font.googlesansflex_24pt_semibold, FontWeight.SemiBold),
	Font(R.font.googlesansflex_24pt_bold, FontWeight.Bold),
	Font(R.font.googlesansflex_24pt_extrabold, FontWeight.ExtraBold),
	Font(R.font.googlesansflex_24pt_black, FontWeight.Black)
)

val HahmletFontFamily = FontFamily(
	Font(R.font.hahmlet_regular, FontWeight.Normal),
	Font(R.font.hahmlet_thin, FontWeight.Thin),
	Font(R.font.hahmlet_medium, FontWeight.Medium),
	Font(R.font.hahmlet_semibold, FontWeight.SemiBold),
	Font(R.font.hahmlet_bold, FontWeight.Bold),
	Font(R.font.hahmlet_extrabold, FontWeight.ExtraBold),
	Font(R.font.hahmlet_black, FontWeight.Black)
)

val RobotoCondensedFontFamily = FontFamily(
	Font(R.font.robotocondensed_regular, FontWeight.Normal),
	Font(R.font.robotocondensed_thin, FontWeight.Thin),
	Font(R.font.robotocondensed_extralight, FontWeight.ExtraLight),
	Font(R.font.robotocondensed_light, FontWeight.Light),
	Font(R.font.robotocondensed_medium, FontWeight.Medium),
	Font(R.font.robotocondensed_semibold, FontWeight.SemiBold),
	Font(R.font.robotocondensed_bold, FontWeight.Bold),
	Font(R.font.robotocondensed_extrabold, FontWeight.ExtraBold),
	Font(R.font.robotocondensed_black, FontWeight.Black),
	Font(R.font.robotocondensed_italic, FontWeight.Normal, FontStyle.Italic),
	Font(R.font.robotocondensed_bolditalic, FontWeight.Bold, FontStyle.Italic)
)

val SpaceGroteskFontFamily = FontFamily(
	Font(R.font.spacegrotesk_regular, FontWeight.Normal),
	Font(R.font.spacegrotesk_light, FontWeight.Light),
	Font(R.font.spacegrotesk_medium, FontWeight.Medium),
	Font(R.font.spacegrotesk_semibold, FontWeight.SemiBold),
	Font(R.font.spacegrotesk_bold, FontWeight.Bold)
)

// Default Typography for Compose
val Typography = Typography(
	bodyLarge = TextStyle(
		fontFamily = FontFamily.Default,
		fontWeight = FontWeight.Normal,
		fontSize = 16.sp,
		lineHeight = 24.sp,
		letterSpacing = 0.5.sp
	)
)

// Typography for Media3 SubtitleView
object Media3Typography {
	enum class FontFamilyType {
		ANDADA_PRO,
		ASAP_CONDENSED,
		GOOGLE_SANS_FLEX,
		HAHMLET,
		ROBOTO_CONDENSED,
		SPACE_GROTESK,
		DEFAULT
	}

	fun getTypeface(context: Context, fontFamilyType: FontFamilyType): Typeface? {
		return try {
			if (fontFamilyType == FontFamilyType.DEFAULT) {
				Timber.tag("Media3Typography").d("Using DEFAULT font")
				return Typeface.DEFAULT
			}

			val fontResId = getFontResourceId(fontFamilyType)
			if (fontResId != null) {
				val typeface = getTypefaceFromResource(context, fontResId)
				if (typeface != null) {
					Timber.tag("Media3Typography").d("Successfully loaded font: $fontFamilyType")
					return typeface
				} else {
					Timber.tag("Media3Typography").w("Failed to load font: $fontFamilyType, falling back to DEFAULT")
					return Typeface.DEFAULT
				}
			} else {
				Timber.tag("Media3Typography").w("No resource ID for font: $fontFamilyType, using DEFAULT")
				return Typeface.DEFAULT
			}
		} catch (e: Exception) {
			Timber.tag("Media3Typography").e(e, "Error loading font: $fontFamilyType")
			Typeface.DEFAULT
		}
	}

	fun getTypefaceFromResource(context: Context, fontResId: Int): Typeface? {
		return try {
			val typeface = context.resources.getFont(fontResId)
			// Verify typeface is not null and not default
			if (typeface != Typeface.DEFAULT) {
				Timber.tag("Media3Typography").d("Successfully loaded font from resource: $fontResId")
				return typeface
			} else {
				Timber.tag("Media3Typography").w("Font resource $fontResId returned null or DEFAULT typeface")
				return null
			}
		} catch (e: Exception) {
			Timber.tag("Media3Typography").e(e, "Failed to load font from resource: $fontResId")
			null
		}
	}

	fun getFontResourceId(fontFamilyType: FontFamilyType): Int? {
		return when (fontFamilyType) {
			FontFamilyType.ANDADA_PRO -> R.font.andadapro_regular
			FontFamilyType.ASAP_CONDENSED -> R.font.asapcondensed_regular
			FontFamilyType.GOOGLE_SANS_FLEX -> R.font.googlesansflex_24pt_regular
			FontFamilyType.HAHMLET -> R.font.hahmlet_regular
			FontFamilyType.ROBOTO_CONDENSED -> R.font.robotocondensed_regular
			FontFamilyType.SPACE_GROTESK -> R.font.spacegrotesk_regular
			FontFamilyType.DEFAULT -> null
		}
	}
}