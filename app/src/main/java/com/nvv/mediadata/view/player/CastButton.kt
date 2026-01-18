package com.nvv.mediadata.view.player

import android.graphics.Color
import android.view.ContextThemeWrapper
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.DrawableCompat
import androidx.mediarouter.app.MediaRouteButton
import com.google.android.gms.cast.framework.CastButtonFactory

@Composable
fun CastButton(
	modifier: Modifier = Modifier,
) {
	AndroidView(
		modifier = modifier,
		factory = { context ->
			val themedContext = ContextThemeWrapper(
				context,
				androidx.appcompat.R.style.Theme_AppCompat_NoActionBar
			)
			MediaRouteButton(themedContext).apply {
				CastButtonFactory.setUpMediaRouteButton(context, this)
				val drawable = androidx.core.content.ContextCompat.getDrawable(
					context,
					androidx.mediarouter.R.drawable.mr_button_light
				)
				if (drawable != null) {
					val wrappedDrawable = DrawableCompat.wrap(drawable.mutate())
					DrawableCompat.setTint(wrappedDrawable, Color.WHITE)
					setRemoteIndicatorDrawable(wrappedDrawable)
				}
			}
		},
		update = { _ ->
		}
	)
}
