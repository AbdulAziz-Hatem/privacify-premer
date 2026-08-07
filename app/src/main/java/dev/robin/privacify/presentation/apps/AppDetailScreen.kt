package dev.robin.privacify.presentation.apps

import androidx.compose.ui.res.stringResource

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Dangerous
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.robin.privacify.domain.apps.AppRiskLevel
import dev.robin.privacify.ui.components.PrivacifyBadge
import dev.robin.privacify.ui.components.PrivacifyDivider
import dev.robin.privacify.ui.components.PrivacifyExpressiveCard
import dev.robin.privacify.ui.components.PrivacifyIconBox
import dev.robin.privacify.ui.theme.ExpressiveExtraLargeIncreased
import dev.robin.privacify.ui.theme.ExpressiveLargeIncreased
import dev.robin.privacify.ui.theme.GreenVibrant
import dev.robin.privacify.ui.theme.OrangeVibrant
import dev.robin.privacify.ui.theme.RedVibrant


data class PermissionDetail(
	val name: String,
	val description: String,
	val isGranted: Boolean
)

data class AppDetailInfo(
	val packageName: String,
	val appName: String,
	val riskLevel: AppRiskLevel,
	val permissions: List<PermissionDetail>,
	val grantedCount: Int,
	val totalCount: Int
)

@Composable
fun AppDetailRoute(
	packageName: String
) {
	val context = LocalContext.current
	val viewModel: AppDetailViewModel = viewModel(
		factory = AppDetailViewModel.factory(packageName, context)
	)
	val app by viewModel.state.collectAsState()
	val isRooted by viewModel.isRooted.collectAsState()
	val actionResult by viewModel.actionResult.collectAsState()

	LaunchedEffect(actionResult) {
		actionResult?.let { msg ->
			Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
			viewModel.clearActionResult()
		}
	}

	val current = app
	if (current != null) {
		AppDetailScreen(
			app = current,
			isRooted = isRooted,
			onAction = { action ->
				when (action) {
					"revoke" -> viewModel.forceRevokePermissions()
					"freeze" -> viewModel.freezeApp()
					"sensors" -> viewModel.blockSensorAccess()
				}
			}
		)
	}
}

private fun calculateRiskFromDetails(
	perms: List<PermissionDetail>,
	requested: List<String>
): AppRiskLevel {
	val hasInternet = requested.any { it == android.Manifest.permission.INTERNET }
	val grantedNames = perms.filter { it.isGranted }.map { it.name.lowercase() }

	val hasMic = grantedNames.any { it.contains("record") || it.contains("audio") }
	val hasCamera = grantedNames.any { it.contains("camera") }
	val hasLocation = grantedNames.any { it.contains("location") }
	val hasContacts = grantedNames.any { it.contains("contacts") }
	val hasSms = grantedNames.any { it.contains("sms") }

	val sensitiveGranted = listOf(hasMic, hasCamera, hasLocation, hasContacts, hasSms).count { it }

	if (sensitiveGranted == 0) return AppRiskLevel.Low
	if (sensitiveGranted >= 3 && hasInternet) return AppRiskLevel.High
	if ((hasMic || hasCamera) && hasInternet) return AppRiskLevel.High
	return AppRiskLevel.Medium
}

@Composable
private fun AppDetailScreen(
	app: AppDetailInfo,
	isRooted: Boolean = false,
	onAction: (String) -> Unit
) {
	val riskColor = when (app.riskLevel) {
		AppRiskLevel.High -> RedVibrant
		AppRiskLevel.Medium -> OrangeVibrant
		AppRiskLevel.Low -> GreenVibrant
	}

	Surface(
		modifier = Modifier.fillMaxSize(),
		color = MaterialTheme.colorScheme.background
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.verticalScroll(rememberScrollState())
				.padding(horizontal = 16.dp, vertical = 16.dp),
			verticalArrangement = Arrangement.spacedBy(16.dp)
		) {
			Header(app, riskColor)
			ActivityInsights(app)
			PermissionsSection(app)
			AdvancedControls(isRooted = isRooted, onAction = onAction)
		}
	}
}

@Composable
private fun Header(
	app: AppDetailInfo,
	riskColor: Color
) {
	Column(
		modifier = Modifier.fillMaxWidth(),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Box(
			modifier = Modifier
				.padding(top = 8.dp, bottom = 12.dp)
				.clip(ExpressiveExtraLargeIncreased)
				.background(riskColor.copy(alpha = 0.15f))
				.padding(24.dp)
		) {
			Text(
				text = app.appName.firstOrNull()?.uppercase() ?: "",
				style = MaterialTheme.typography.headlineMedium,
				fontWeight = FontWeight.Black,
				color = riskColor
			)
		}
		Text(
			text = app.appName,
			style = MaterialTheme.typography.titleLarge,
			fontWeight = FontWeight.Black
		)
		Spacer(modifier = Modifier.height(4.dp))
		Text(
			text = app.packageName,
			style = MaterialTheme.typography.bodySmall,
			color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
			textAlign = TextAlign.Center
		)
		Spacer(modifier = Modifier.height(8.dp))
		PrivacifyBadge(
			text = when (app.riskLevel) {
				AppRiskLevel.High -> stringResource(R.string.risk_high_app)
				AppRiskLevel.Medium -> stringResource(R.string.risk_medium_app)
				AppRiskLevel.Low -> stringResource(R.string.risk_low_app)
			},
			color = riskColor
		)
	}
}

@Composable
private fun ActivityInsights(
	app: AppDetailInfo
) {
	Column(modifier = Modifier.fillMaxWidth()) {
		Text(
			text = stringResource(R.string.detail_activity_insights),
			style = MaterialTheme.typography.labelMedium,
			fontWeight = FontWeight.Black,
			color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
			modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
		)
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.spacedBy(12.dp)
		) {
			InsightCard(
				title = "${app.grantedCount}/${app.totalCount}",
				subtitle = stringResource(R.string.detail_permissions_granted),
				modifier = Modifier.weight(1f)
			)
			InsightCard(
				title = when (app.riskLevel) {
					AppRiskLevel.High -> stringResource(R.string.risk_elevated)
					AppRiskLevel.Medium -> stringResource(R.string.risk_moderate)
					AppRiskLevel.Low -> stringResource(R.string.risk_low)
				},
				subtitle = stringResource(R.string.detail_risk_level),
				modifier = Modifier.weight(1f)
			)
		}
	}
}

@Composable
private fun InsightCard(
	title: String,
	subtitle: String,
	modifier: Modifier = Modifier
) {
	PrivacifyExpressiveCard(
		modifier = modifier
	) {
		Column(
			modifier = Modifier.padding(16.dp)
		) {
			Text(
				text = title,
				style = MaterialTheme.typography.titleLarge,
				fontWeight = FontWeight.Black
			)
			Spacer(modifier = Modifier.height(4.dp))
			Text(
				text = subtitle,
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
	}
}

@Composable
private fun PermissionsSection(
	app: AppDetailInfo
) {
	Column(modifier = Modifier.fillMaxWidth()) {
		Text(
			text = stringResource(R.string.detail_permissions_header, app.grantedCount, app.totalCount),
			style = MaterialTheme.typography.labelMedium,
			fontWeight = FontWeight.Black,
			color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
			modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
		)
		PrivacifyExpressiveCard {
			Column {
				if (app.permissions.isEmpty()) {
					Box(
						modifier = Modifier
							.fillMaxWidth()
							.padding(24.dp),
						contentAlignment = Alignment.Center
					) {
						Text(
							text = stringResource(R.string.detail_no_sensitive_permissions),
							style = MaterialTheme.typography.bodySmall,
							color = MaterialTheme.colorScheme.onSurfaceVariant
						)
					}
				}
				app.permissions.forEachIndexed { index, perm ->
					PermissionRow(
						title = perm.name,
						description = perm.description,
						isGranted = perm.isGranted
					)
					if (index < app.permissions.lastIndex) {
						PrivacifyDivider(modifier = Modifier.padding(start = 56.dp))
					}
				}
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailBottomSheet(
	packageName: String,
	onDismiss: () -> Unit
) {
	val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
	val context = LocalContext.current
	val viewModel: AppDetailViewModel = viewModel(
		key = packageName,
		factory = AppDetailViewModel.factory(packageName, context)
	)
	val app by viewModel.state.collectAsState()
	val isRooted by viewModel.isRooted.collectAsState()
	val actionResult by viewModel.actionResult.collectAsState()

	LaunchedEffect(actionResult) {
		actionResult?.let { msg ->
			Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
			viewModel.clearActionResult()
		}
	}

	ModalBottomSheet(
		onDismissRequest = onDismiss,
		sheetState = sheetState,
		shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
		containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.verticalScroll(rememberScrollState())
				.padding(horizontal = 16.dp, vertical = 8.dp),
			verticalArrangement = Arrangement.spacedBy(16.dp)
		) {
			Spacer(modifier = Modifier.height(8.dp))

			val current = app
			if (current != null) {
				AppDetailContent(
					app = current,
					isRooted = isRooted,
					onAction = { action ->
						when (action) {
							"revoke" -> viewModel.forceRevokePermissions()
							"freeze" -> viewModel.freezeApp()
							"sensors" -> viewModel.blockSensorAccess()
						}
					}
				)
			}
			Spacer(modifier = Modifier.height(24.dp))
		}
	}
}

@Composable
private fun AppDetailContent(
	app: AppDetailInfo,
	isRooted: Boolean = false,
	onAction: (String) -> Unit
) {
	val riskColor = when (app.riskLevel) {
		AppRiskLevel.High -> RedVibrant
		AppRiskLevel.Medium -> OrangeVibrant
		AppRiskLevel.Low -> GreenVibrant
	}

	Header(app, riskColor)
	ActivityInsights(app)
	PermissionsSection(app)
	AdvancedControls(isRooted = isRooted, onAction = onAction)
}

@Composable
private fun PermissionRow(
	title: String,
	description: String,
	isGranted: Boolean
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 16.dp, vertical = 12.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Column(modifier = Modifier.weight(1f)) {
			Text(
				text = title,
				style = MaterialTheme.typography.bodyMedium,
				fontWeight = FontWeight.Bold,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
			Spacer(modifier = Modifier.height(2.dp))
			Text(
				text = description,
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
		Box(
			modifier = Modifier
				.clip(CircleShape)
				.background(
					if (isGranted) RedVibrant.copy(alpha = 0.12f)
					else GreenVibrant.copy(alpha = 0.12f)
				)
				.padding(horizontal = 12.dp, vertical = 6.dp)
		) {
			Text(
				text = if (isGranted) stringResource(R.string.state_granted) else stringResource(R.string.state_denied),
				style = MaterialTheme.typography.labelSmall,
				fontWeight = FontWeight.Bold,
				color = if (isGranted) RedVibrant else GreenVibrant
			)
		}
	}
}

@Composable
private fun AdvancedControls(
	isRooted: Boolean = false,
	onAction: (String) -> Unit
) {
	var showConfirmDialog by remember { mutableStateOf(false) }
	var pendingAction by remember { mutableStateOf("") }
	var pendingActionId by remember { mutableStateOf("") }

	if (showConfirmDialog) {
		AlertDialog(
			onDismissRequest = { showConfirmDialog = false },
			title = { Text(stringResource(R.string.dialog_confirm_root_action), fontWeight = FontWeight.Black) },
			text = { Text(stringResource(R.string.dialog_root_action_message, pendingAction)) },
			confirmButton = {
				TextButton(
					onClick = {
						showConfirmDialog = false
						onAction(pendingActionId)
					}
				) {
					Text(stringResource(R.string.action_execute), color = RedVibrant, fontWeight = FontWeight.Black)
				}
			},
			dismissButton = {
				TextButton(onClick = { showConfirmDialog = false }) {
					Text(stringResource(R.string.action_cancel))
				}
			}
		)
	}

	Column(modifier = Modifier.fillMaxWidth()) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 4.dp),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(
				text = stringResource(R.string.detail_advanced_controls),
				style = MaterialTheme.typography.labelMedium,
				fontWeight = FontWeight.Black,
				color = RedVibrant
			)
			Row(
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.spacedBy(4.dp)
			) {
				Box(
					modifier = Modifier
						.size(8.dp)
						.clip(CircleShape)
						.background(RedVibrant)
				)
				Text(
					text = stringResource(R.string.badge_root_only),
					style = MaterialTheme.typography.labelSmall,
					fontWeight = FontWeight.Black,
					color = RedVibrant
				)
			}
		}
		Spacer(modifier = Modifier.height(8.dp))
		Column(
			verticalArrangement = Arrangement.spacedBy(8.dp)
		) {
			AdvancedActionCard(
				icon = Icons.Outlined.Block,
				title = stringResource(R.string.action_force_revoke_permissions),
				description = if (isRooted) stringResource(R.string.action_force_revoke_desc_rooted) else stringResource(R.string.requires_root_access),
				enabled = isRooted,
				onClick = {
					pendingAction = stringResource(R.string.action_force_revoke_pending)
					pendingActionId = "revoke"
					showConfirmDialog = true
				}
			)
			AdvancedActionCard(
				icon = Icons.Outlined.Dangerous,
				title = stringResource(R.string.action_freeze_app),
				description = if (isRooted) stringResource(R.string.action_freeze_app_desc_rooted) else stringResource(R.string.requires_root_access),
				enabled = isRooted,
				onClick = {
					pendingAction = stringResource(R.string.action_freeze_pending)
					pendingActionId = "freeze"
					showConfirmDialog = true
				}
			)
			AdvancedActionCard(
				icon = Icons.Outlined.Sensors,
				title = stringResource(R.string.action_block_sensors),
				description = if (isRooted) stringResource(R.string.action_block_sensors_desc_rooted) else stringResource(R.string.requires_root_access),
				enabled = isRooted,
				onClick = {
					pendingAction = stringResource(R.string.action_block_sensors_pending)
					pendingActionId = "sensors"
					showConfirmDialog = true
				}
			)
		}
	}
}

@Composable
private fun AdvancedActionCard(
	icon: ImageVector,
	title: String,
	description: String,
	enabled: Boolean = true,
	onClick: () -> Unit
) {
	val alpha = if (enabled) 1f else 0.45f
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clip(ExpressiveLargeIncreased)
			.background(RedVibrant.copy(alpha = 0.06f * alpha))
			.clickable(enabled = enabled) { onClick() }
			.padding(horizontal = 16.dp, vertical = 12.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(12.dp)
	) {
		PrivacifyIconBox(
			icon = icon,
			tint = RedVibrant,
			background = RedVibrant.copy(alpha = 0.12f),
			iconSize = 20
		)
		Column(modifier = Modifier.weight(1f)) {
			Text(
				text = title,
				style = MaterialTheme.typography.bodyMedium,
				fontWeight = FontWeight.Black
			)
			Spacer(modifier = Modifier.height(2.dp))
			Text(
				text = description,
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
		Box(
			modifier = Modifier
				.clip(RoundedCornerShape(999.dp))
				.background(RedVibrant.copy(alpha = 0.12f))
				.padding(horizontal = 12.dp, vertical = 6.dp)
		) {
			Text(
				text = stringResource(R.string.detail_action),
				style = MaterialTheme.typography.labelSmall,
				fontWeight = FontWeight.Black,
				color = RedVibrant
			)
		}
	}
}
