package dev.robin.privacify.presentation.apps

import androidx.compose.ui.res.stringResource
import dev.robin.privacify.R

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.robin.privacify.domain.apps.AppPrivacyInfo
import dev.robin.privacify.domain.apps.AppRiskLevel
import dev.robin.privacify.presentation.apps.AppDetailBottomSheet
import dev.robin.privacify.ui.components.PrivacifyBadge
import dev.robin.privacify.ui.components.PrivacifyExpressiveCard
import dev.robin.privacify.ui.theme.GreenVibrant
import dev.robin.privacify.ui.theme.OrangeVibrant
import dev.robin.privacify.ui.theme.RedVibrant
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.layout.heightIn

@Composable
fun AppsScreen() {
	val context = LocalContext.current
	val viewModel: AppsViewModel = viewModel(factory = AppsViewModel.factory(context))
	val state by viewModel.state.collectAsState()
	var selectedApp by remember { mutableStateOf<AppPrivacyInfo?>(null) }

	selectedApp?.let { app ->
		AppDetailBottomSheet(
			packageName = app.packageName,
			onDismiss = { selectedApp = null }
		)
	}

	Surface(
		modifier = Modifier.fillMaxSize(),
		color = MaterialTheme.colorScheme.background
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(top = 16.dp)
		) {
			Column(
				modifier = Modifier.padding(horizontal = 16.dp)
			) {
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.SpaceBetween,
					verticalAlignment = Alignment.CenterVertically
				) {
					Text(
						text = stringResource(R.string.apps_permission_scanner),
						style = MaterialTheme.typography.headlineSmall,
						fontWeight = FontWeight.Black
					)
					PrivacifyBadge(
						text = stringResource(R.string.apps_count, state.apps.size),
						color = MaterialTheme.colorScheme.primary
					)
				}
				Spacer(modifier = Modifier.height(12.dp))
				OutlinedTextField(
					value = state.query,
					onValueChange = { viewModel.onQueryChanged(it) },
					modifier = Modifier.fillMaxWidth(),
					shape = MaterialTheme.shapes.large,
					singleLine = true,
					placeholder = {
						Text(text = stringResource(R.string.apps_search_hint))
					},
					leadingIcon = {
						Icon(
							imageVector = Icons.Filled.Search,
							contentDescription = null,
							tint = MaterialTheme.colorScheme.onSurfaceVariant
						)
					},
					colors = OutlinedTextFieldDefaults.colors(
						focusedBorderColor = MaterialTheme.colorScheme.primary,
						unfocusedBorderColor = MaterialTheme.colorScheme.outline,
						focusedContainerColor = MaterialTheme.colorScheme.surface,
						unfocusedContainerColor = MaterialTheme.colorScheme.surface
					)
				)
				Spacer(modifier = Modifier.height(12.dp))
			}
			LazyRow(
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 16.dp),
				horizontalArrangement = Arrangement.spacedBy(8.dp)
			) {
				item {
					FilterChip(
						label = stringResource(R.string.filter_all),
						selected = state.filter == RiskFilter.All,
						onClick = { viewModel.onFilterChanged(RiskFilter.All) }
					)
				}
				item {
					FilterChip(
						label = stringResource(R.string.filter_high_risk),
						selected = state.filter == RiskFilter.High,
						onClick = { viewModel.onFilterChanged(RiskFilter.High) }
					)
				}
				item {
					FilterChip(
						label = stringResource(R.string.filter_medium_risk),
						selected = state.filter == RiskFilter.Medium,
						onClick = { viewModel.onFilterChanged(RiskFilter.Medium) }
					)
				}
				item {
					FilterChip(
						label = stringResource(R.string.filter_safe),
						selected = state.filter == RiskFilter.Low,
						onClick = { viewModel.onFilterChanged(RiskFilter.Low) }
					)
				}
			}
			Spacer(modifier = Modifier.height(8.dp))
			LazyColumn(
				verticalArrangement = Arrangement.spacedBy(8.dp),
				modifier = Modifier
					.fillMaxSize()
					.padding(horizontal = 16.dp)
			) {
				items(
					items = state.filteredApps,
					key = { it.packageName }
				) 				{ app ->
					AppRow(
						app = app,
						onClick = { selectedApp = app }
					)
				}
				item {
					Spacer(modifier = Modifier.height(8.dp))
				}
			}
		}
	}
}

@Composable
private fun FilterChip(
	label: String,
	selected: Boolean,
	onClick: () -> Unit
) {
	val bgColor by animateColorAsState(
		targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceBright,
		label = "chip_bg"
	)
	Box(
		modifier = Modifier
			.clip(RoundedCornerShape(999.dp))
			.background(bgColor)
			.selectable(selected = selected, onClick = { onClick() }, role = Role.Tab)
			.padding(horizontal = 16.dp, vertical = 10.dp)
			.heightIn(min = 48.dp)
	) {
		Text(
			text = label,
			style = MaterialTheme.typography.labelLarge,
			fontWeight = if (selected) FontWeight.Black else FontWeight.SemiBold,
			color = if (selected) MaterialTheme.colorScheme.onPrimary
			else MaterialTheme.colorScheme.onSurface
		)
	}
}

@Composable
private fun AppRow(
	app: AppPrivacyInfo,
	onClick: () -> Unit
) {
	val riskColor = when (app.riskLevel) {
		AppRiskLevel.High -> RedVibrant
		AppRiskLevel.Medium -> OrangeVibrant
		AppRiskLevel.Low -> GreenVibrant
	}

	PrivacifyExpressiveCard(onClick = onClick) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(12.dp),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(12.dp)
		) {
		val context = LocalContext.current
		val iconPainter = remember(app.packageName) {
			try {
				val pm = context.packageManager
				val ai = pm.getApplicationInfo(app.packageName, 0)
				val drawable = pm.getApplicationIcon(ai)
				val w = drawable.intrinsicWidth.coerceAtLeast(1)
				val h = drawable.intrinsicHeight.coerceAtLeast(1)
				val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
				val canvas = Canvas(bitmap)
				drawable.setBounds(0, 0, w, h)
				drawable.draw(canvas)
				BitmapPainter(bitmap.asImageBitmap())
			} catch (_: Exception) {
				null
			}
		}
		Box(
			modifier = Modifier
				.size(40.dp)
				.clip(CircleShape)
				.background(riskColor.copy(alpha = 0.12f)),
			contentAlignment = Alignment.Center
		) {
			if (iconPainter != null) {
				Image(
					painter = iconPainter,
					contentDescription = app.appName,
					modifier = Modifier.size(32.dp).clip(CircleShape)
				)
			} else {
				Text(
					text = app.appName.firstOrNull()?.uppercase() ?: "",
					color = riskColor,
					style = MaterialTheme.typography.titleMedium,
					fontWeight = FontWeight.Black
				)
			}
		}
			Column(modifier = Modifier.weight(1f)) {
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.SpaceBetween,
					verticalAlignment = Alignment.CenterVertically
				) {
					Text(
						text = app.appName,
						style = MaterialTheme.typography.bodyLarge,
						fontWeight = FontWeight.Bold,
						maxLines = 1,
						overflow = TextOverflow.Ellipsis,
						modifier = Modifier.weight(1f, fill = false)
					)
					Spacer(modifier = Modifier.width(8.dp))
					RiskBadge(app)
				}
				Spacer(modifier = Modifier.height(4.dp))
				Text(
					text = app.permissionsSummary,
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis
				)
			}
		}
	}
}

@Composable
private fun RiskBadge(app: AppPrivacyInfo) {
	val (label, bg, fg) = when (app.riskLevel) {
		AppRiskLevel.High -> Triple(
			stringResource(R.string.filter_high_risk),
			RedVibrant.copy(alpha = 0.15f),
			RedVibrant
		)

		AppRiskLevel.Medium -> Triple(
			stringResource(R.string.risk_medium),
			OrangeVibrant.copy(alpha = 0.15f),
			OrangeVibrant
		)

		AppRiskLevel.Low -> Triple(
			stringResource(R.string.filter_safe),
			GreenVibrant.copy(alpha = 0.15f),
			GreenVibrant
		)
	}

	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier
			.clip(RoundedCornerShape(999.dp))
			.background(bg)
			.padding(horizontal = 8.dp, vertical = 4.dp)
	) {
		Box(
			modifier = Modifier
				.size(6.dp)
				.clip(CircleShape)
				.background(fg)
		)
		Spacer(modifier = Modifier.width(4.dp))
		Text(
			text = label,
			style = MaterialTheme.typography.labelSmall,
			fontWeight = FontWeight.Black,
			color = fg
		)
	}
}
