package dev.robin.privacify.presentation.settings

import androidx.compose.ui.res.stringResource
import dev.robin.privacify.R

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PowerSettingsNew
import androidx.compose.material.icons.outlined.Radar
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import android.content.Context
import android.content.Intent
import dev.robin.privacify.core.provider.ProFeature
import dev.robin.privacify.ui.components.PrivacifyAutoGuardCard
import dev.robin.privacify.ui.components.PrivacifyBadge
import dev.robin.privacify.ui.components.PrivacifyChip
import dev.robin.privacify.ui.components.PrivacifyExpressiveCard
import dev.robin.privacify.ui.components.PrivacifyIconBox
import dev.robin.privacify.ui.components.PrivacifyProDialog
import dev.robin.privacify.ui.components.PrivacifySectionHeader
import dev.robin.privacify.ui.components.PrivacifyStatusIndicator
import dev.robin.privacify.ui.components.PrivacifyDivider
import dev.robin.privacify.ui.components.PrivacifySwitch
import dev.robin.privacify.ui.components.PrivacifyWarningBanner
import dev.robin.privacify.ui.theme.AutoGuardPrimary
import dev.robin.privacify.ui.theme.BlueVibrant
import dev.robin.privacify.ui.theme.GreenVibrant
import dev.robin.privacify.ui.theme.PurpleVibrant
import dev.robin.privacify.ui.theme.RedVibrant
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.role

@Composable
fun SettingsScreen(onNavigateToExemptions: () -> Unit = {}) {
	val context = LocalContext.current
	val viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.factory(context))
	val state by viewModel.state.collectAsState()

	val lifecycleOwner = LocalLifecycleOwner.current
	DisposableEffect(lifecycleOwner) {
		val observer = LifecycleEventObserver { _, event ->
			if (event == Lifecycle.Event.ON_RESUME) {
				viewModel.refreshRuntimeStatus()
			}
		}
		lifecycleOwner.lifecycle.addObserver(observer)
		onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
	}

	Surface(
		modifier = Modifier.fillMaxSize(),
		color = MaterialTheme.colorScheme.background
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.verticalScroll(rememberScrollState())
				.padding(horizontal = 16.dp),
			verticalArrangement = Arrangement.spacedBy(16.dp)
		) {
			Spacer(modifier = Modifier.height(8.dp))

			Text(
				text = stringResource(R.string.nav_settings),
				style = MaterialTheme.typography.headlineLarge,
				fontWeight = FontWeight.Black,
				modifier = Modifier.padding(horizontal = 4.dp)
			)

		ProtectionSection(
			enabled = state.automationEnabled,
			onToggle = viewModel::onAutomationChanged,
			autostartEnabled = state.autostartEnabled,
			onAutostartToggle = viewModel::onAutostartChanged,
			onNavigateToExemptions = onNavigateToExemptions
		)

			GeneralSection(
				state = state,
				onNotificationsChanged = viewModel::onNotificationsChanged,
				onScanFrequencyClick = viewModel::onScanFrequencyClicked,
				onThemeClick = viewModel::onThemeClicked
			)
		AdvancedSection(
				context = context,
				state = state,
				onShellTypeChange = viewModel::setShellType,
				onRefreshRuntimeStatus = { viewModel.refreshRuntimeStatus() },
				onRequestShizukuPermission = { viewModel.requestShizukuPermission() }
			)
			AboutSection()
			Footer()

			Spacer(modifier = Modifier.height(16.dp))
		}
	}
}

@Composable
private fun ProtectionSection(
	enabled: Boolean,
	onToggle: (Boolean) -> Unit,
	autostartEnabled: Boolean,
	onAutostartToggle: (Boolean) -> Unit,
	onNavigateToExemptions: () -> Unit
) {
	var showAutostartProDialog by remember { mutableStateOf(false) }
	val isPro = ProFeature.isAutoGuardAvailable()

	val context = LocalContext.current
	val prefs = remember { context.getSharedPreferences("privacify_prefs", Context.MODE_PRIVATE) }
	var micEnabled by remember { mutableStateOf(prefs.getBoolean("auto_guard_mic_enabled", true)) }
	var cameraEnabled by remember { mutableStateOf(prefs.getBoolean("auto_guard_camera_enabled", true)) }
	var locationEnabled by remember { mutableStateOf(prefs.getBoolean("auto_guard_location_enabled", false)) }

	Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 4.dp),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(
				text = stringResource(R.string.settings_section_protection),
				style = MaterialTheme.typography.labelMedium,
				fontWeight = FontWeight.Black,
				color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
			)
			PrivacifyBadge(text = stringResource(R.string.badge_featured), color = AutoGuardPrimary)
		}
		PrivacifyAutoGuardCard(
			enabled = enabled,
			onToggle = onToggle
		)
		if (enabled && isPro) {
			PrivacifyExpressiveCard {
				Column {
					SettingsRow(
						title = stringResource(R.string.perm_microphone),
						subtitle = stringResource(R.string.settings_mic_subtitle),
						icon = Icons.Outlined.Mic,
						iconTint = MaterialTheme.colorScheme.error,
						iconBackground = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
						trailing = {
							PrivacifySwitch(
								checked = micEnabled,
								onCheckedChange = { enabled ->
									micEnabled = enabled
									prefs.edit().putBoolean("auto_guard_mic_enabled", enabled).apply()
								}
							)
						}
					)
					PrivacifyDivider()
					SettingsRow(
						title = stringResource(R.string.perm_camera),
						subtitle = stringResource(R.string.settings_camera_subtitle),
						icon = Icons.Outlined.Videocam,
						iconTint = MaterialTheme.colorScheme.primary,
						iconBackground = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
						trailing = {
							PrivacifySwitch(
								checked = cameraEnabled,
								onCheckedChange = { enabled ->
									cameraEnabled = enabled
									prefs.edit().putBoolean("auto_guard_camera_enabled", enabled).apply()
								}
							)
						}
					)
					PrivacifyDivider()
					SettingsRow(
						title = stringResource(R.string.perm_location),
						subtitle = stringResource(R.string.settings_location_subtitle),
						icon = Icons.Outlined.LocationOn,
						iconTint = MaterialTheme.colorScheme.tertiary,
						iconBackground = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
						trailing = {
							PrivacifySwitch(
								checked = locationEnabled,
								onCheckedChange = { enabled ->
									locationEnabled = enabled
									prefs.edit().putBoolean("auto_guard_location_enabled", enabled).apply()
								}
							)
						}
					)
					PrivacifyDivider()
					SettingsRow(
						title = stringResource(R.string.settings_face_unlock),
						subtitle = stringResource(R.string.settings_face_unlock_subtitle),
						icon = Icons.Outlined.Lock,
						iconTint = MaterialTheme.colorScheme.secondary,
						iconBackground = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
						trailing = {
							PrivacifySwitch(
								checked = prefs.getBoolean("auto_guard_face_unlock_enabled", true),
								onCheckedChange = { enabled ->
									prefs.edit().putBoolean("auto_guard_face_unlock_enabled", enabled).apply()
								}
							)
						}
					)
				}
			}
			PrivacifyExpressiveCard {
				SettingsRow(
					title = stringResource(R.string.settings_toast_notifications),
					subtitle = stringResource(R.string.settings_toast_subtitle),
					icon = Icons.Outlined.Notifications,
					iconTint = AutoGuardPrimary,
					iconBackground = AutoGuardPrimary.copy(alpha = 0.12f),
					trailing = {
						PrivacifySwitch(
							checked = prefs.getBoolean("auto_guard_toast_enabled", true),
							onCheckedChange = { enabled ->
								prefs.edit().putBoolean("auto_guard_toast_enabled", enabled).apply()
							}
						)
					}
				)
			}
			PrivacifyExpressiveCard {
				SettingsRow(
					title = stringResource(R.string.exemptions_title),
					subtitle = stringResource(R.string.settings_exempted_subtitle),
					icon = Icons.Outlined.Shield,
					iconTint = MaterialTheme.colorScheme.tertiary,
					iconBackground = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
					onClick = onNavigateToExemptions,
					trailing = {
						Icon(
							Icons.Outlined.Shield,
							contentDescription = null,
							tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
							modifier = Modifier.size(20.dp)
						)
					}
				)
			}
		}
		PrivacifyExpressiveCard {
			SettingsRow(
				title = stringResource(R.string.settings_autostart),
				subtitle = stringResource(R.string.settings_autostart_subtitle),
				icon = Icons.Outlined.PowerSettingsNew,
				iconTint = AutoGuardPrimary,
				iconBackground = AutoGuardPrimary.copy(alpha = 0.12f),
				trailing = {
					PrivacifySwitch(
						checked = autostartEnabled,
						onCheckedChange = { newValue ->
							if (isPro) {
								onAutostartToggle(newValue)
							} else {
								showAutostartProDialog = true
							}
						}
					)
				}
			)
		}
	}

	if (showAutostartProDialog) {
		PrivacifyProDialog(
			featureName = stringResource(R.string.settings_autostart),
			description = stringResource(R.string.settings_autostart_desc),
			onDismiss = { showAutostartProDialog = false }
		)
	}
}

@Composable
private fun SettingsRow(
	title: String,
	subtitle: String? = null,
	icon: ImageVector,
	iconTint: Color,
	iconBackground: Color,
	onClick: (() -> Unit)? = null,
	trailing: @Composable (() -> Unit)? = null
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.then(
				if (onClick != null) Modifier.semantics(mergeDescendants = true) { role = Role.Button }.clickable { onClick() }
				else Modifier
			)
			.padding(horizontal = 16.dp, vertical = 14.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		PrivacifyIconBox(
			icon = icon,
			tint = iconTint,
			background = iconBackground
		)
		Spacer(modifier = Modifier.width(16.dp))
		Column(modifier = Modifier.weight(1f)) {
			Text(
				text = title,
				style = MaterialTheme.typography.titleMedium,
				fontWeight = FontWeight.Bold
			)
			if (subtitle != null) {
				Spacer(modifier = Modifier.height(2.dp))
				Text(
					text = subtitle,
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
		}
		if (trailing != null) {
			Spacer(modifier = Modifier.width(12.dp))
			trailing()
		}
	}
}

@Composable
private fun GeneralSection(
	state: SettingsUiState,
	onNotificationsChanged: (Boolean) -> Unit,
	onScanFrequencyClick: () -> Unit,
	onThemeClick: () -> Unit
) {
	Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
		PrivacifySectionHeader(title = stringResource(R.string.settings_section_general))
		PrivacifyExpressiveCard {
			Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
				SettingsRow(
					title = stringResource(R.string.perm_notifications),
					subtitle = stringResource(R.string.settings_notifications_subtitle),
					icon = Icons.Outlined.Notifications,
					iconTint = BlueVibrant,
					iconBackground = BlueVibrant.copy(alpha = 0.12f),
					trailing = {
						PrivacifySwitch(
							checked = state.notificationsEnabled,
							onCheckedChange = onNotificationsChanged
						)
					}
				)
				PrivacifyDivider(modifier = Modifier.padding(start = 56.dp))
				SettingsRow(
					title = stringResource(R.string.settings_scan_frequency),
					subtitle = state.scanFrequencyLabel,
					icon = Icons.Outlined.Radar,
					iconTint = PurpleVibrant,
					iconBackground = PurpleVibrant.copy(alpha = 0.12f),
					onClick = onScanFrequencyClick
				)
				PrivacifyDivider(modifier = Modifier.padding(start = 56.dp))
				SettingsRow(
					title = stringResource(R.string.settings_theme),
					subtitle = state.themeLabel,
					icon = Icons.Outlined.DarkMode,
					iconTint = MaterialTheme.colorScheme.tertiary,
					iconBackground = MaterialTheme.colorScheme.tertiaryContainer,
					onClick = onThemeClick
				)
			}
		}
	}
}

@Composable
private fun AdvancedSection(
	context: android.content.Context,
	state: SettingsUiState,
	onShellTypeChange: (String) -> Unit,
	onRefreshRuntimeStatus: () -> Unit,
	onRequestShizukuPermission: () -> Unit
) {
	val shellOptions = listOf("Auto", "Root", "Shizuku")

	Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 4.dp),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(
				text = stringResource(R.string.settings_section_advanced),
				style = MaterialTheme.typography.labelMedium,
				fontWeight = FontWeight.Black,
				color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
			)
			Row(
				horizontalArrangement = Arrangement.spacedBy(8.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				PrivacifyStatusIndicator(
					status = if (state.rootAvailable) "Root" else "No Root",
					color = if (state.rootAvailable) GreenVibrant else RedVibrant
				)
				if (state.shizukuStatus.isNotEmpty()) {
					val shizukuColor = when {
						state.shizukuReady -> GreenVibrant
						state.shizukuStatus == "Unknown" -> MaterialTheme.colorScheme.onSurfaceVariant
						else -> RedVibrant
					}
					PrivacifyStatusIndicator(
						status = if (state.shizukuReady) "Shizuku" else state.shizukuStatus,
						color = shizukuColor
					)
				}
				PrivacifyChip(text = "Root/Shizuku", color = PurpleVibrant)
			}
		}

		PrivacifyExpressiveCard {
			Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.padding(horizontal = 16.dp, vertical = 14.dp)
				) {
					Row(
						verticalAlignment = Alignment.CenterVertically,
						horizontalArrangement = Arrangement.spacedBy(12.dp)
					) {
						PrivacifyIconBox(
							icon = Icons.Outlined.Terminal,
							tint = PurpleVibrant,
							background = PurpleVibrant.copy(alpha = 0.12f)
						)
						Column(modifier = Modifier.weight(1f)) {
							Text(
								text = stringResource(R.string.settings_shell_type),
								style = MaterialTheme.typography.titleMedium,
								fontWeight = FontWeight.Bold
							)
							Spacer(modifier = Modifier.height(6.dp))
							Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
								shellOptions.forEach { option ->
									val isSelected = option == state.shellTypeLabel
									Box(
										modifier = Modifier
											.clip(RoundedCornerShape(999.dp))
											.background(
												if (isSelected) PurpleVibrant
												else MaterialTheme.colorScheme.surfaceBright
											)
											.clickable {
												val newValue = when (option) {
													"Root" -> "root"
													"Shizuku" -> "shizuku"
													else -> "auto"
												}
												onShellTypeChange(newValue)
												if (option == "Shizuku" && state.shizukuStatus != "Ready") {
													onRequestShizukuPermission()
												}
												onRefreshRuntimeStatus()
											}
											.padding(horizontal = 16.dp, vertical = 8.dp)
									) {
										Text(
											text = option,
											style = MaterialTheme.typography.labelLarge,
											fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
											color = if (isSelected) Color.White
											else MaterialTheme.colorScheme.onSurfaceVariant
										)
									}
								}
							}
							if (state.activeShellMethod.isNotEmpty()) {
								Spacer(modifier = Modifier.height(4.dp))
								Text(
									text = stringResource(R.string.settings_active, state.activeShellMethod),
									style = MaterialTheme.typography.bodySmall,
									color = if (state.activeShellMethod == "Root") GreenVibrant else PurpleVibrant,
									fontWeight = FontWeight.Medium
								)
							}
						}
					}
				}
			}
		}

		PrivacifyWarningBanner(
			text = stringResource(R.string.settings_requires_privileges)
		)

		PrivacifyExpressiveCard {
			Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
				if (state.automationEnabled) {
					SettingsRow(
						title = stringResource(R.string.settings_battery_optimization),
						subtitle = if (state.batteryOptimizationGranted) stringResource(R.string.settings_battery_unrestricted)
						else stringResource(R.string.settings_battery_disable),
						icon = Icons.Outlined.Shield,
						iconTint = if (state.batteryOptimizationGranted) GreenVibrant else PurpleVibrant,
						iconBackground = (if (state.batteryOptimizationGranted) GreenVibrant else PurpleVibrant).copy(alpha = 0.12f),
						onClick = {
							try {
								val intent = android.content.Intent().apply {
									action = android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
									data = android.net.Uri.parse("package:${context.packageName}")
								}
								context.startActivity(intent)
							} catch (e: Exception) {
								try {
									val intent = android.content.Intent().apply {
										action = android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
									}
									context.startActivity(intent)
								} catch (e2: Exception) {
									android.util.Log.e("SettingsScreen", "Failed to open battery settings", e2)
								}
							}
						}
					)
				}
			}
		}
	}
}

@Composable
private fun AboutSection() {
	val context = LocalContext.current
	val uriHandler = LocalUriHandler.current
	var showUpdateProDialog by remember { mutableStateOf(false) }
	val versionName = remember {
		try {
			context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
		} catch (_: Exception) { "1.0.0" }
	}

	Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
		PrivacifySectionHeader(title = stringResource(R.string.settings_section_about))
		PrivacifyExpressiveCard {
			Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
				SettingsRow(
					title = stringResource(R.string.settings_open_source),
					subtitle = stringResource(R.string.settings_open_source_subtitle),
					icon = Icons.Outlined.Code,
					iconTint = MaterialTheme.colorScheme.primary,
					iconBackground = MaterialTheme.colorScheme.primaryContainer,
					onClick = {
						try {
							uriHandler.openUri("https://github.com/robinsrk/privacify")
						} catch (_: Exception) {}
					}
				)
				PrivacifyDivider(modifier = Modifier.padding(start = 56.dp))
				SettingsRow(
					title = stringResource(R.string.settings_check_updates),
					subtitle = stringResource(R.string.settings_check_updates_subtitle),
					icon = Icons.Outlined.CloudDownload,
					iconTint = MaterialTheme.colorScheme.primary,
					iconBackground = MaterialTheme.colorScheme.primaryContainer,
					onClick = {
						if (ProFeature.isAutoGuardAvailable()) {
							try {
								val intent = Intent().apply {
									setClassName(context, "dev.robin.privacify.pro.update.UpdateActivity")
									putExtra("manual_check", true)
									addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
								}
								context.startActivity(intent)
							} catch (_: Exception) {}
						} else {
							showUpdateProDialog = true
						}
					}
				)
				PrivacifyDivider(modifier = Modifier.padding(start = 56.dp))
				SettingsRow(
					title = stringResource(R.string.settings_version),
					subtitle = versionName,
					icon = Icons.Outlined.Info,
					iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
					iconBackground = MaterialTheme.colorScheme.surfaceBright
				)
			}
		}
	}

	if (showUpdateProDialog) {
		PrivacifyProDialog(
			featureName = stringResource(R.string.settings_auto_update),
			description = stringResource(R.string.settings_auto_update_desc),
			onDismiss = { showUpdateProDialog = false }
		)
	}
}

@Composable
private fun Footer() {
	Column(
		modifier = Modifier.fillMaxWidth(),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Text(
			text = stringResource(R.string.settings_control_center),
			style = MaterialTheme.typography.bodySmall,
			color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
		)

	}
}
