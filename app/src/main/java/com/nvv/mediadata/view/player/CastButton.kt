package com.nvv.mediadata.view.player

import android.view.ContextThemeWrapper
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
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
				androidx.appcompat.R.style.Theme_AppCompat_DayNight_NoActionBar
			)
			MediaRouteButton(themedContext).apply {
				CastButtonFactory.setUpMediaRouteButton(context, this)
			}
		},
		update = { _ ->
			// No update needed for basic Cast button
		}
	)
}
