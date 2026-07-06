package dev.robin.privacify.presentation.home

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
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MicOff
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.robin.privacify.ui.components.PrivacifyAutoGuardCard
import dev.robin.privacify.ui.components.SensorCard
import dev.robin.privacify.ui.theme.AmberVibrant
import dev.robin.privacify.ui.theme.BlueVibrant
import dev.robin.privacify.ui.theme.GreenVibrant
import dev.robin.privacify.ui.theme.LockdownRed
import dev.robin.privacify.ui.theme.MdSpacing
import dev.robin.privacify.ui.theme.OrangeVibrant
import dev.robin.privacify.ui.theme.RedVibrant
import dev.robin.privacify.ui.theme.ScoreGreen
import dev.robin.privacify.ui.theme.ScoreOrange
import dev.robin.privacify.ui.theme.ScoreRed

@Composable
fun HomeScreen() {
	val context = LocalContext.current
	val viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.factory(context))
	val state by viewModel.state.collectAsState()

	Surface(
		modifier = Modifier.fillMaxSize(),
		color = MaterialTheme.colorScheme.background
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.verticalScroll(rememberScrollState())
				.padding(horizontal = 20.dp, vertical = 16.dp),
			verticalArrangement = Arrangement.spacedBy(16.dp)
		) {
			PrivacyScoreCard(
				score = state.privacyScore,
				statusText = when {
					state.privacyScore >= 90 -> "Secure"
					state.privacyScore >= 75 -> "Moderate"
					else -> "At Risk"
				},
				subtitle = state.statusSubtitle
			)

			PrivacifyAutoGuardCard(
				enabled = state.automationEnabled,
				onToggle = { viewModel.onAutoGuardToggled(it) }
			)

			ProtectionSection(
				hasAccess = state.isRooted,
				lockdownActive = state.lockdownEnabled,
				shellType = state.shellType,
				micActive = state.micDisabled,
				cameraActive = state.cameraDisabled,
				locationActive = state.locationDisabled,
				onLockdownToggle = { viewModel.onQuickActionToggled(QuickAction.Lockdown) },
				onMicToggle = { viewModel.onQuickActionToggled(QuickAction.MicKill) },
				onCameraToggle = { viewModel.onQuickActionToggled(QuickAction.CameraKill) },
				onLocationToggle = { viewModel.onQuickActionToggled(QuickAction.LocationKill) }
			)

			QuickStatsCard(
				totalApps = state.totalApps,
				totalPermissions = state.totalPermissions,
				highRiskCount = state.highRiskCount
			)

			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.End,
				verticalAlignment = Alignment.CenterVertically
			) {
				if (state.isScanning) {
					CircularProgressIndicator(
						modifier = Modifier.size(20.dp),
						strokeWidth = 2.dp,
						color = MaterialTheme.colorScheme.primary
					)
					Spacer(modifier = Modifier.width(10.dp))
				}
				ScanNowButton(
					onClick = { viewModel.onScanNowClicked() }
				)
			}
		}
	}
}

@Composable
private fun PrivacyScoreCard(
	score: Int,
	statusText: String,
	subtitle: String
) {
	val statusColor = when {
		score >= 90 -> ScoreGreen
		score >= 75 -> ScoreOrange
		else -> ScoreRed
	}

	Card(
		modifier = Modifier.fillMaxWidth(),
		shape = MaterialTheme.shapes.extraLarge,
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
		)
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(MdSpacing.sm + MdSpacing.xxs),
			verticalAlignment = Alignment.CenterVertically
		) {
			Box(
				modifier = Modifier
					.size(64.dp)
					.clip(CircleShape)
					.background(statusColor.copy(alpha = 0.12f)),
				contentAlignment = Alignment.Center
			) {
				Text(
					text = score.toString(),
					style = MaterialTheme.typography.headlineLarge,
					fontWeight = FontWeight.ExtraBold,
					color = statusColor
				)
			}

			Spacer(modifier = Modifier.width(16.dp))

			Column(modifier = Modifier.weight(1f)) {
				Row(verticalAlignment = Alignment.CenterVertically) {
					Text(
						text = statusText,
						style = MaterialTheme.typography.titleMedium,
						fontWeight = FontWeight.Bold,
						color = MaterialTheme.colorScheme.onSurface
					)
					Spacer(modifier = Modifier.width(8.dp))
					Box(
						modifier = Modifier
							.size(8.dp)
							.clip(CircleShape)
							.background(statusColor)
					)
				}
				Spacer(modifier = Modifier.height(4.dp))
				Text(
					text = subtitle,
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
		}
	}
}

@Composable
private fun ProtectionSection(
	hasAccess: Boolean,
	lockdownActive: Boolean,
	shellType: String,
	micActive: Boolean,
	cameraActive: Boolean,
	locationActive: Boolean,
	onLockdownToggle: () -> Unit,
	onMicToggle: () -> Unit,
	onCameraToggle: () -> Unit,
	onLocationToggle: () -> Unit
) {
	Card(
		modifier = Modifier.fillMaxWidth(),
		shape = MaterialTheme.shapes.extraLarge,
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
		)
	) {
		Column(
			modifier = Modifier.padding(MdSpacing.sm),
			verticalArrangement = Arrangement.spacedBy(14.dp)
		) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				verticalAlignment = Alignment.CenterVertically
			) {
				Text(
					text = "PROTECTION",
					style = MaterialTheme.typography.labelMedium,
					fontWeight = FontWeight.Bold,
					color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
					modifier = Modifier.semantics { heading() }
				)
			}

			LockdownRow(
				hasAccess = hasAccess,
				isActive = lockdownActive,
				shellType = shellType,
				onToggle = onLockdownToggle
			)

			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.Center,
				verticalAlignment = Alignment.CenterVertically
			) {
				SensorCard(
					icon = Icons.Outlined.MicOff,
					title = "Mic",
					active = micActive,
					activeColor = RedVibrant,
					onClick = { if (hasAccess) onMicToggle() }
				)
				Spacer(modifier = Modifier.width(10.dp))
				SensorCard(
					icon = Icons.Outlined.CameraAlt,
					title = "Camera",
					active = cameraActive,
					activeColor = OrangeVibrant,
					onClick = { if (hasAccess) onCameraToggle() }
				)
				Spacer(modifier = Modifier.width(10.dp))
				SensorCard(
					icon = Icons.Outlined.LocationOn,
					title = "Location",
					active = locationActive,
					activeColor = AmberVibrant,
					onClick = { if (hasAccess) onLocationToggle() }
				)
			}
		}
	}
}

@Composable
private fun LockdownRow(
	hasAccess: Boolean,
	isActive: Boolean,
	shellType: String,
	onToggle: () -> Unit
) {
	Surface(
		modifier = Modifier
			.fillMaxWidth()
			.semantics {
				contentDescription = "Lockdown Mode, ${if (isActive) "Active" else "Inactive"}"
				stateDescription = if (isActive) "Active" else "Inactive"
			}
			.clickable(enabled = hasAccess) { onToggle() },
		shape = MaterialTheme.shapes.large,
		color = when {
			isActive -> LockdownRed
			hasAccess -> MaterialTheme.colorScheme.surfaceBright
			else -> MaterialTheme.colorScheme.surfaceBright.copy(alpha = 0.6f)
		}
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(14.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Icon(
				imageVector = if (isActive) Icons.Filled.Security else Icons.Outlined.Lock,
				contentDescription = null,
				tint = if (isActive) Color.White else MaterialTheme.colorScheme.primary,
				modifier = Modifier.size(22.dp)
			)
			Spacer(modifier = Modifier.width(12.dp))
			Column(modifier = Modifier.weight(1f)) {
				Text(
					text = "Lockdown Mode",
					style = MaterialTheme.typography.titleSmall,
					fontWeight = FontWeight.Bold,
					color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurface
				)
				Text(
					text = when {
						isActive -> "All sensors blocked · DND enabled"
						hasAccess -> "Instantly block all sensors"
						else -> "Requires ${if (shellType == "shizuku") "Shizuku" else "Root"} access"
					},
					style = MaterialTheme.typography.bodySmall,
					color = if (isActive) Color.White.copy(alpha = 0.8f)
					else MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
			if (isActive) {
				Icon(
					imageVector = Icons.Outlined.FlashOn,
					contentDescription = null,
					tint = Color.White.copy(alpha = 0.7f),
					modifier = Modifier.size(18.dp)
				)
			}
		}
	}
}

@Composable
private fun ScanNowButton(
	onClick: () -> Unit
) {
	Button(
		onClick = onClick,
		shape = MaterialTheme.shapes.large,
		modifier = Modifier
			.height(48.dp),
		colors = ButtonDefaults.buttonColors(
			containerColor = MaterialTheme.colorScheme.primary,
			contentColor = MaterialTheme.colorScheme.onPrimary
		),
		elevation = ButtonDefaults.buttonElevation(
			defaultElevation = 0.dp,
			pressedElevation = 2.dp
		)
	) {
		Icon(
			imageVector = Icons.Filled.Security,
			contentDescription = null,
			modifier = Modifier.size(16.dp)
		)
		Spacer(modifier = Modifier.width(6.dp))
		Text(
			text = "Scan Now",
			style = MaterialTheme.typography.labelLarge,
			fontWeight = FontWeight.Bold
		)
	}
}

@Composable
private fun QuickStatsCard(
	totalApps: Int,
	totalPermissions: Int,
	highRiskCount: Int
) {
	Card(
		modifier = Modifier.fillMaxWidth(),
		shape = MaterialTheme.shapes.extraLarge,
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
		)
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(MdSpacing.sm),
			horizontalArrangement = Arrangement.SpaceEvenly
		) {
			StatItem(
				icon = Icons.Outlined.Shield,
				value = "$totalApps",
				label = "Apps",
				color = BlueVibrant
			)
			StatItem(
				icon = Icons.Outlined.Shield,
				value = "$totalPermissions",
				label = "Permissions",
				color = OrangeVibrant
			)
			StatItem(
				icon = Icons.Outlined.Warning,
				value = "$highRiskCount",
				label = "At Risk",
				color = if (highRiskCount > 0) RedVibrant else GreenVibrant
			)
		}
	}
}

@Composable
private fun StatItem(
	icon: androidx.compose.ui.graphics.vector.ImageVector,
	value: String,
	label: String,
	color: androidx.compose.ui.graphics.Color
) {
	Column(
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Box(
			modifier = Modifier
				.size(40.dp)
				.clip(CircleShape)
				.background(color.copy(alpha = 0.12f)),
			contentAlignment = Alignment.Center
		) {
			Icon(
				imageVector = icon,
				contentDescription = null,
				tint = color,
				modifier = Modifier.size(20.dp)
			)
		}
		Spacer(modifier = Modifier.height(MdSpacing.xxs))
		Text(
			text = value,
			style = MaterialTheme.typography.titleLarge,
			fontWeight = FontWeight.ExtraBold,
			color = color
		)
		Text(
			text = label,
			style = MaterialTheme.typography.labelSmall,
			fontWeight = FontWeight.Bold,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)
	}
}
