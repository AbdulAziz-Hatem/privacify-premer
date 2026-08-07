package dev.robin.privacify.presentation.lockdown

import androidx.compose.ui.res.stringResource
import dev.robin.privacify.R
import dev.robin.privacify.core.utils.AppContextProvider

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.GppGood
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.robin.privacify.ui.components.SensorCard
import dev.robin.privacify.ui.theme.GreenVibrant
import dev.robin.privacify.ui.theme.LockdownRed
import dev.robin.privacify.ui.theme.AmberVibrant
import dev.robin.privacify.ui.theme.MdSpacing
import dev.robin.privacify.ui.theme.OrangeVibrant
import dev.robin.privacify.ui.theme.RedVibrant
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode

@Composable
fun LockdownScreen(
	onBack: () -> Unit = {}
) {
	val viewModel: LockdownViewModel = viewModel(factory = LockdownViewModel.Factory)
	val state by viewModel.state.collectAsState()

	Surface(
		modifier = Modifier.fillMaxSize(),
		color = MaterialTheme.colorScheme.background
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.verticalScroll(rememberScrollState())
				.padding(horizontal = 16.dp, vertical = 16.dp)
		) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				verticalAlignment = Alignment.CenterVertically
			) {
				IconButton(onClick = onBack) {
					Icon(
						imageVector = Icons.AutoMirrored.Filled.ArrowBack,
						contentDescription = stringResource(R.string.action_back)
					)
				}
				Spacer(modifier = Modifier.width(8.dp))
				Text(
					text = stringResource(R.string.home_lockdown_mode),
					style = MaterialTheme.typography.titleLarge,
					fontWeight = FontWeight.Black,
					modifier = Modifier.semantics { heading() }
				)
			}

			Spacer(modifier = Modifier.height(MdSpacing.sm))

			PanicButton(
				activated = state.lockdownActive,
				onToggle = { viewModel.toggleLockdown() }
			)

			Spacer(modifier = Modifier.height(MdSpacing.sm))

			StatusBanner(active = state.lockdownActive)

			Spacer(modifier = Modifier.height(MdSpacing.sm))

			Text(
				text = stringResource(R.string.lockdown_sensor_controls),
				style = MaterialTheme.typography.labelMedium,
				fontWeight = FontWeight.Black,
				color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
				modifier = Modifier
					.padding(horizontal = 4.dp)
					.semantics { heading() }
			)

			Spacer(modifier = Modifier.height(MdSpacing.xs))

			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.spacedBy(MdSpacing.xs)
			) {
				SensorCard(
					icon = Icons.Outlined.Mic,
					title = stringResource(R.string.sensor_mic),
					active = state.micKilled,
					activeColor = RedVibrant,
					onClick = { viewModel.toggleMic() }
				)
				SensorCard(
					icon = Icons.Outlined.CameraAlt,
					title = stringResource(R.string.sensor_camera),
					active = state.cameraKilled,
					activeColor = OrangeVibrant,
					onClick = { viewModel.toggleCamera() }
				)
				SensorCard(
					icon = Icons.Outlined.LocationOn,
					title = stringResource(R.string.sensor_location),
					active = state.locationKilled,
					activeColor = AmberVibrant,
					onClick = { viewModel.toggleLocation() }
				)
			}

			Spacer(modifier = Modifier.height(MdSpacing.sm))

			Box(
				modifier = Modifier
					.fillMaxWidth()
					.clip(MaterialTheme.shapes.large)
					.background(RedVibrant.copy(alpha = 0.08f))
					.padding(MdSpacing.sm)
					.semantics {
						contentDescription = AppContextProvider.context.getString(R.string.lockdown_important_cd)
					}
			) {
				Row(
					horizontalArrangement = Arrangement.spacedBy(12.dp),
					verticalAlignment = Alignment.Top
				) {
					Box(
						modifier = Modifier
							.size(24.dp)
							.clip(CircleShape)
							.background(RedVibrant.copy(alpha = 0.2f)),
						contentAlignment = Alignment.Center
					) {
						Icon(
							imageVector = Icons.AutoMirrored.Filled.Help,
							contentDescription = null,
							tint = RedVibrant,
							modifier = Modifier.size(14.dp)
						)
					}
					Column {
						Text(
							text = stringResource(R.string.lockdown_important_title),
							style = MaterialTheme.typography.titleSmall,
							fontWeight = FontWeight.Black,
							color = RedVibrant
						)
						Spacer(modifier = Modifier.height(4.dp))
						Text(
							text = stringResource(R.string.lockdown_important_message),
							style = MaterialTheme.typography.bodySmall,
							color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
						)
					}
				}
			}

			Spacer(modifier = Modifier.height(MdSpacing.sm))
		}
	}
}

@Composable
private fun PanicButton(
	activated: Boolean,
	onToggle: () -> Unit
) {
	val bgColor by animateColorAsState(
		targetValue = if (activated) LockdownRed else MaterialTheme.colorScheme.primary,
		animationSpec = spring(
			stiffness = Spring.StiffnessMedium,
			dampingRatio = Spring.DampingRatioMediumBouncy
		), label = "panic_bg"
	)
	val scale by animateFloatAsState(
		targetValue = if (activated) 1.05f else 1f,
		animationSpec = spring(
			stiffness = Spring.StiffnessMediumLow,
			dampingRatio = Spring.DampingRatioMediumBouncy
		),
		label = "panic_scale"
	)

	Column(
		modifier = Modifier.fillMaxWidth(),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Box(
			modifier = Modifier
				.size(200.dp)
				.scale(scale)
				.clip(CircleShape)
				.background(
					Brush.radialGradient(
						colors = listOf(
							bgColor,
							bgColor.copy(alpha = 0.6f)
						)
					)
				)
				.clickable { onToggle() }
				.semantics {
					contentDescription = if (activated) AppContextProvider.context.getString(R.string.lockdown_deactivate_cd) else AppContextProvider.context.getString(R.string.lockdown_activate_cd)
					stateDescription = if (activated) AppContextProvider.context.getString(R.string.state_active) else AppContextProvider.context.getString(R.string.state_inactive)
					role = Role.Button
				},
			contentAlignment = Alignment.Center
		) {
			Column(
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				Icon(
					imageVector = if (activated) Icons.Outlined.GppGood else Icons.Outlined.Lock,
					contentDescription = null,
					tint = Color.White,
					modifier = Modifier.size(56.dp)
				)
				Spacer(modifier = Modifier.height(8.dp))
				Text(
					text = if (activated) stringResource(R.string.lockdown_activated) else stringResource(R.string.lockdown_button),
					style = MaterialTheme.typography.titleMedium,
					fontWeight = FontWeight.Black,
					color = Color.White
				)
			}
		}
		Spacer(modifier = Modifier.height(12.dp))
		Text(
			text = if (activated) stringResource(R.string.lockdown_tap_deactivate) else stringResource(R.string.lockdown_tap_activate),
			style = MaterialTheme.typography.bodySmall,
			color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
			textAlign = TextAlign.Center
		)
	}
}

@Composable
private fun StatusBanner(active: Boolean) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clip(MaterialTheme.shapes.extraLarge)
			.background(
				if (active) GreenVibrant.copy(alpha = 0.12f)
				else MaterialTheme.colorScheme.surfaceBright
			)
			.padding(MdSpacing.sm)
			.semantics {
				contentDescription = if (active) AppContextProvider.context.getString(R.string.lockdown_active_cd) else AppContextProvider.context.getString(R.string.lockdown_standard_cd)
			},
		horizontalArrangement = Arrangement.spacedBy(MdSpacing.xs),
		verticalAlignment = Alignment.CenterVertically
	) {
		Icon(
			imageVector = Icons.Outlined.Security,
			contentDescription = null,
			tint = if (active) GreenVibrant else MaterialTheme.colorScheme.onSurfaceVariant,
			modifier = Modifier.size(24.dp)
		)
		Column {
			Text(
				text = if (active) stringResource(R.string.lockdown_all_sensors_disabled) else stringResource(R.string.lockdown_standard_mode),
				style = MaterialTheme.typography.titleSmall,
				fontWeight = FontWeight.Black,
				color = if (active) GreenVibrant else MaterialTheme.colorScheme.onSurface,
				modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
			)
			Text(
				text = if (active) stringResource(R.string.lockdown_active_message)
				else stringResource(R.string.lockdown_standard_message),
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
			)
		}
	}
}
