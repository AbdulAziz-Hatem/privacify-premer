package dev.robin.privacify.presentation.onboarding

import androidx.compose.ui.res.stringResource

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.VideocamOff
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.robin.privacify.domain.onboarding.OnboardingStep
import dev.robin.privacify.ui.components.PrivacifyBadge
import dev.robin.privacify.ui.components.PrivacifyExpressiveCard
import dev.robin.privacify.ui.components.PrivacifyIconBox
import dev.robin.privacify.ui.theme.AutoGuardPrimary
import dev.robin.privacify.ui.theme.BlueVibrant
import dev.robin.privacify.ui.theme.GreenVibrant
import dev.robin.privacify.ui.theme.OrangeVibrant
import dev.robin.privacify.ui.theme.PurpleVibrant
import dev.robin.privacify.ui.theme.RedVibrant

@Composable
fun OnboardingRoute(
	onFinished: () -> Unit
) {
	val viewModel: OnboardingViewModel = viewModel(
		factory = OnboardingViewModel.Companion.factory(
			context = androidx.compose.ui.platform.LocalContext.current
		)
	)
	val state by viewModel.state.collectAsState()

	if (state.isCompleted) {
		onFinished()
		return
	}

	Surface(
		modifier = Modifier
			.fillMaxSize()
			.statusBarsPadding(),
		color = MaterialTheme.colorScheme.background
	) {
		Column(modifier = Modifier.fillMaxSize()) {
			Box(modifier = Modifier.weight(1f)) {
				AnimatedContent(
					targetState = state.step,
					transitionSpec = {
						val direction = if (targetState.ordinal > initialState.ordinal) 1 else -1
						slideInHorizontally(
							animationSpec = tween(350),
							initialOffsetX = { fullWidth -> direction * fullWidth }
						) + fadeIn(tween(250)) togetherWith
						slideOutHorizontally(
							animationSpec = tween(350),
							targetOffsetX = { fullWidth -> -direction * fullWidth / 3 }
						) + fadeOut(tween(200))
					},
					label = "onboarding_content"
				) { step ->
					when (step) {
						OnboardingStep.Welcome -> WelcomeStepContent(
							onContinue = viewModel::onWelcomeContinue
						)
						OnboardingStep.Acknowledgement -> AcknowledgementStepContent(
							onBack = viewModel::onBack,
							onContinue = viewModel::onAcknowledgementContinue
						)
						OnboardingStep.SystemCheck -> SystemCheckStepContent(
							state = state,
							onCheckPermissions = viewModel::checkPermissions,
							onBack = viewModel::onBack,
							onContinue = viewModel::onSystemCheckContinue
						)
						OnboardingStep.FeatureIntro -> FeatureIntroStepContent(
							onBack = viewModel::onBack,
							onContinue = viewModel::onFeatureIntroContinue
						)
					}
				}
			}
		}
	}
}

@Composable
private fun StepNavigation(
	showBack: Boolean,
	onBack: () -> Unit,
	buttonText: String,
	onContinue: () -> Unit,
	enabled: Boolean = true
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.navigationBarsPadding()
			.padding(horizontal = 24.dp)
			.padding(bottom = 16.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(16.dp)
	) {
		if (showBack) {
			OutlinedButton(
				onClick = onBack,
				modifier = Modifier.size(56.dp),
				shape = CircleShape,
				colors = ButtonDefaults.outlinedButtonColors(
					contentColor = MaterialTheme.colorScheme.onSurface
				),
				border = ButtonDefaults.outlinedButtonBorder
			) {
				Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.action_back))
			}
		}

		Button(
			onClick = onContinue,
			enabled = enabled,
			modifier = Modifier
				.weight(1f)
				.height(56.dp),
			shape = RoundedCornerShape(16.dp),
			colors = ButtonDefaults.buttonColors(
				containerColor = MaterialTheme.colorScheme.primary,
				contentColor = MaterialTheme.colorScheme.onPrimary
			)
		) {
			Text(
				text = buttonText,
				fontWeight = FontWeight.Black,
				fontSize = 16.sp
			)
			Spacer(Modifier.width(8.dp))
			Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null)
		}
	}
}

@Composable
private fun WelcomeStepContent(
	onContinue: () -> Unit
) {
	var scaled by remember { mutableStateOf(false) }
	var expanded by remember { mutableStateOf(false) }
	val languages = listOf(stringResource(R.string.onboarding_language_system), stringResource(R.string.onboarding_language_english), stringResource(R.string.onboarding_language_arabic))
	var selectedLanguage by remember { mutableStateOf(languages[0]) }

	Column(
		modifier = Modifier
			.fillMaxSize()
			.navigationBarsPadding()
			.padding(horizontal = 24.dp),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
			Column(horizontalAlignment = Alignment.CenterHorizontally) {
				Box(
					modifier = Modifier
						.size(120.dp)
						.clip(RoundedCornerShape(28.dp))
						.background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
						.scale(if (scaled) 0.85f else 1f)
						.clickable { scaled = !scaled },
					contentAlignment = Alignment.Center
				) {
					Icon(
						imageVector = Icons.Rounded.Shield,
						contentDescription = null,
						tint = MaterialTheme.colorScheme.primary,
						modifier = Modifier.size(60.dp)
					)
				}

				Spacer(Modifier.height(32.dp))

				Text(
					text = stringResource(R.string.onboarding_welcome),
					style = MaterialTheme.typography.headlineLarge,
					fontWeight = FontWeight(800),
					color = MaterialTheme.colorScheme.onBackground,
					textAlign = TextAlign.Center
				)

				Spacer(Modifier.height(8.dp))

				Text(
					text = stringResource(R.string.onboarding_by),
					style = MaterialTheme.typography.bodyLarge,
					color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
					textAlign = TextAlign.Center
				)

				Spacer(Modifier.height(32.dp))

				Row(
					modifier = Modifier
						.clip(RoundedCornerShape(16.dp))
						.background(MaterialTheme.colorScheme.surfaceContainerHighest)
						.clickable { expanded = true }
						.padding(horizontal = 16.dp, vertical = 12.dp),
					verticalAlignment = Alignment.CenterVertically
				) {
					Icon(
						imageVector = Icons.Rounded.Language,
						contentDescription = null,
						tint = MaterialTheme.colorScheme.onSurfaceVariant
					)
					Spacer(Modifier.width(12.dp))
					Text(
						text = selectedLanguage,
						fontWeight = FontWeight.ExtraBold,
						color = MaterialTheme.colorScheme.onSurface
					)
					Spacer(Modifier.weight(1f))
					Icon(
						imageVector = Icons.Rounded.ExpandMore,
						contentDescription = null,
						tint = MaterialTheme.colorScheme.onSurfaceVariant
					)
				}

				DropdownMenu(
					expanded = expanded,
					onDismissRequest = { expanded = false }
				) {
					languages.forEach { lang ->
						DropdownMenuItem(
							text = { Text(lang) },
							onClick = {
								selectedLanguage = lang
								expanded = false
							}
						)
					}
				}
			}
		}

		Button(
			onClick = onContinue,
			modifier = Modifier
				.fillMaxWidth()
				.height(56.dp),
			shape = RoundedCornerShape(16.dp),
			colors = ButtonDefaults.buttonColors(
				containerColor = MaterialTheme.colorScheme.primary,
				contentColor = MaterialTheme.colorScheme.onPrimary
			)
		) {
			Text(
				text = stringResource(R.string.onboarding_begin),
				fontWeight = FontWeight.Black,
				fontSize = 16.sp
			)
			Spacer(Modifier.width(8.dp))
			Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null)
		}

		Spacer(Modifier.height(16.dp))
	}
}

@Composable
private fun AcknowledgementStepContent(
	onBack: () -> Unit,
	onContinue: () -> Unit
) {
	Column(modifier = Modifier.fillMaxSize()) {
		Column(
			modifier = Modifier
				.weight(1f)
				.verticalScroll(rememberScrollState())
				.padding(horizontal = 24.dp)
		) {
			Spacer(Modifier.height(24.dp))

			Text(
				text = stringResource(R.string.onboarding_privacy_disclaimer),
				style = MaterialTheme.typography.headlineLarge,
				fontWeight = FontWeight(800),
				color = MaterialTheme.colorScheme.onBackground
			)

			Spacer(Modifier.height(12.dp))

			Text(
				text = stringResource(R.string.onboarding_disclaimer_body),
				style = MaterialTheme.typography.bodyLarge,
				color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
			)

			Spacer(Modifier.height(24.dp))

			PrivacifyExpressiveCard {
				Column(
					modifier = Modifier
						.fillMaxWidth()
						.padding(16.dp)
				) {
					Text(
						text = stringResource(R.string.onboarding_what_we_need),
						style = MaterialTheme.typography.titleMedium,
						fontWeight = FontWeight.Bold,
						color = MaterialTheme.colorScheme.onSurface
					)
					Spacer(Modifier.height(8.dp))
					Text(
						text = stringResource(R.string.onboarding_permissions_list),
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onSurfaceVariant
					)
				}
			}

			Spacer(Modifier.height(16.dp))

			PrivacifyExpressiveCard {
				Column(
					modifier = Modifier
						.fillMaxWidth()
						.padding(16.dp)
				) {
					Text(
						text = stringResource(R.string.onboarding_privacy_matters),
						style = MaterialTheme.typography.titleMedium,
						fontWeight = FontWeight.Bold,
						color = MaterialTheme.colorScheme.onSurface
					)
					Spacer(Modifier.height(8.dp))
					Text(
						text = stringResource(R.string.onboarding_privacy_statement),
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onSurfaceVariant
					)
				}
			}

			Spacer(Modifier.height(24.dp))
		}

		StepNavigation(
			showBack = true,
			onBack = onBack,
			buttonText = stringResource(R.string.onboarding_i_understand),
			onContinue = onContinue
		)
	}
}

@Composable
private fun SystemCheckStepContent(
	state: OnboardingUiState,
	onCheckPermissions: (Context) -> Unit,
	onBack: () -> Unit,
	onContinue: () -> Unit
) {
	val context = LocalContext.current
	val lifecycleOwner = LocalLifecycleOwner.current

	var showRootGuide by remember { mutableStateOf(false) }
	var showShizukuGuide by remember { mutableStateOf(false) }

	DisposableEffect(lifecycleOwner) {
		val observer = LifecycleEventObserver { _, event ->
			if (event == Lifecycle.Event.ON_RESUME) {
				onCheckPermissions(context)
			}
		}
		lifecycleOwner.lifecycle.addObserver(observer)
		onDispose {
			lifecycleOwner.lifecycle.removeObserver(observer)
		}
	}

	val notificationLauncher = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.RequestPermission()
	) {
		onCheckPermissions(context)
	}

	if (showRootGuide) {
		AlertDialog(
			onDismissRequest = { showRootGuide = false },
			title = {
				Text(
					text = stringResource(R.string.onboarding_grant_root),
					fontWeight = FontWeight.Bold
				)
			},
			text = {
				Text(
					text = stringResource(R.string.onboarding_root_body)
				)
			},
			confirmButton = {
				TextButton(onClick = { showRootGuide = false }) {
					Text(stringResource(R.string.onboarding_got_it))
				}
			}
		)
	}

	if (showShizukuGuide) {
		AlertDialog(
			onDismissRequest = { showShizukuGuide = false },
			title = {
				Text(
					text = stringResource(R.string.onboarding_grant_shizuku),
					fontWeight = FontWeight.Bold
				)
			},
			text = {
				Text(
					text = stringResource(R.string.onboarding_shizuku_body)
				)
			},
			confirmButton = {
				TextButton(onClick = { showShizukuGuide = false }) {
					Text(stringResource(R.string.onboarding_got_it))
				}
			}
		)
	}

	Column(modifier = Modifier.fillMaxSize()) {
		Column(
			modifier = Modifier
				.weight(1f)
				.verticalScroll(rememberScrollState())
				.padding(horizontal = 24.dp)
		) {
			Spacer(Modifier.height(24.dp))

			Text(
				text = stringResource(R.string.onboarding_required_access),
				style = MaterialTheme.typography.headlineLarge,
				fontWeight = FontWeight(800),
				color = MaterialTheme.colorScheme.onBackground
			)

			Spacer(Modifier.height(12.dp))

			Text(
				text = stringResource(R.string.onboarding_required_access_body),
				style = MaterialTheme.typography.bodyLarge,
				color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
			)

			Spacer(Modifier.height(24.dp))

			Column(
				modifier = Modifier
					.clip(MaterialTheme.shapes.extraLarge)
					.background(MaterialTheme.colorScheme.surfaceBright),
				verticalArrangement = Arrangement.spacedBy(0.dp)
			) {
				PermissionToggleItem(
					icon = Icons.Rounded.AdminPanelSettings,
					title = stringResource(R.string.perm_usage_access),
					subtitle = stringResource(R.string.perm_usage_access_desc),
					checked = state.usageAccessGranted,
					onClick = {
						if (!state.usageAccessGranted) {
							val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
							context.startActivity(intent)
						}
					}
				)

				if (Build.VERSION.SDK_INT >= 33) {
					PermissionToggleItem(
						icon = Icons.Rounded.Notifications,
						title = stringResource(R.string.perm_notifications),
						subtitle = stringResource(R.string.perm_notifications_desc),
						checked = state.notificationPermissionGranted,
						onClick = {
							if (!state.notificationPermissionGranted) {
								notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
							}
						}
					)
				}

				HorizontalDivider(
					modifier = Modifier.padding(horizontal = 16.dp),
					color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
				)

				PermissionToggleItem(
					icon = Icons.Rounded.Shield,
					title = stringResource(R.string.perm_root_access),
					subtitle = if (state.rootGranted) stringResource(R.string.perm_root_granted)
					           else stringResource(R.string.perm_root_requires),
					checked = state.rootGranted,
					isAdvanced = true,
					onClick = {
						if (!state.rootGranted) {
							showRootGuide = true
						}
					}
				)

				PermissionToggleItem(
					icon = Icons.Rounded.AdminPanelSettings,
					title = stringResource(R.string.perm_shizuku),
					subtitle = if (state.shizukuGranted) stringResource(R.string.perm_shizuku_granted)
					           else stringResource(R.string.perm_shizuku_requires),
					checked = state.shizukuGranted,
					isAdvanced = true,
					onClick = {
						if (!state.shizukuGranted) {
							showShizukuGuide = true
						}
					}
				)
			}

			Spacer(Modifier.height(24.dp))
		}

		StepNavigation(
			showBack = true,
			onBack = onBack,
			buttonText = stringResource(R.string.onboarding_all_set),
			onContinue = onContinue,
			enabled = state.usageAccessGranted
		)
	}
}

@Composable
private fun PermissionToggleItem(
	icon: ImageVector,
	title: String,
	subtitle: String,
	checked: Boolean,
	isAdvanced: Boolean = false,
	onClick: () -> Unit
) {
	val iconTint = if (checked) GreenVibrant else MaterialTheme.colorScheme.onSurfaceVariant
	val iconBg = if (checked) GreenVibrant.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceContainerHighest

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clickable { onClick() }
			.padding(horizontal = 16.dp, vertical = 12.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		PrivacifyIconBox(
			icon = icon,
			tint = iconTint,
			background = iconBg
		)
		Spacer(Modifier.width(16.dp))
		Column(Modifier.weight(1f)) {
			Row(verticalAlignment = Alignment.CenterVertically) {
				Text(
					text = title,
					style = MaterialTheme.typography.titleMedium,
					fontWeight = FontWeight.Bold,
					color = MaterialTheme.colorScheme.onSurface
				)
				if (isAdvanced) {
					Spacer(Modifier.width(8.dp))
					PrivacifyBadge(text = stringResource(R.string.badge_advanced), color = OrangeVibrant)
				}
			}
			Spacer(Modifier.height(2.dp))
			Text(
				text = subtitle,
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
		Spacer(Modifier.width(16.dp))
		Switch(
			checked = checked,
			onCheckedChange = { onClick() },
			colors = SwitchDefaults.colors(
				checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
				checkedTrackColor = MaterialTheme.colorScheme.primary,
				uncheckedThumbColor = MaterialTheme.colorScheme.outline,
				uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest
			)
		)
	}
}

private data class FeatureItem(
	val title: String,
	val description: String,
	val icon: ImageVector,
	val color: Color
)

@Composable
private fun FeatureIntroStepContent(
	onBack: () -> Unit,
	onContinue: () -> Unit
) {
	val features = listOf(
		FeatureItem(
			title = stringResource(R.string.feature_monitor_sensors),
			description = stringResource(R.string.feature_monitor_sensors_desc),
			icon = Icons.Rounded.VideocamOff,
			color = PurpleVibrant
		),
		FeatureItem(
			title = stringResource(R.string.feature_block_trackers),
			description = stringResource(R.string.feature_block_trackers_desc),
			icon = Icons.Rounded.WifiOff,
			color = BlueVibrant
		),
		FeatureItem(
			title = stringResource(R.string.home_lockdown_mode),
			description = stringResource(R.string.feature_lockdown_desc),
			icon = Icons.Rounded.Lock,
			color = RedVibrant
		),
		FeatureItem(
			title = stringResource(R.string.feature_auto_guard),
			description = stringResource(R.string.feature_auto_guard_desc),
			icon = Icons.Rounded.AutoAwesome,
			color = AutoGuardPrimary
		)
	)

	Column(modifier = Modifier.fillMaxSize()) {
		Column(
			modifier = Modifier
				.weight(1f)
				.verticalScroll(rememberScrollState())
				.padding(horizontal = 24.dp)
		) {
			Spacer(Modifier.height(24.dp))

			Text(
				text = stringResource(R.string.onboarding_feature_intro),
				style = MaterialTheme.typography.headlineLarge,
				fontWeight = FontWeight(800),
				color = MaterialTheme.colorScheme.onBackground
			)

			Spacer(Modifier.height(12.dp))

			Text(
				text = stringResource(R.string.onboarding_protect_life),
				style = MaterialTheme.typography.bodyLarge,
				color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
			)

			Spacer(Modifier.height(24.dp))

			Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
				features.forEach { feature ->
					PrivacifyExpressiveCard {
						Row(
							modifier = Modifier
								.fillMaxWidth()
								.padding(16.dp),
							verticalAlignment = Alignment.CenterVertically,
							horizontalArrangement = Arrangement.spacedBy(16.dp)
						) {
							PrivacifyIconBox(
								icon = feature.icon,
								tint = feature.color,
								background = feature.color.copy(alpha = 0.12f)
							)
							Column(modifier = Modifier.weight(1f)) {
								Text(
									text = feature.title,
									style = MaterialTheme.typography.titleMedium,
									fontWeight = FontWeight.Bold,
									color = MaterialTheme.colorScheme.onSurface
								)
								Spacer(Modifier.height(2.dp))
								Text(
									text = feature.description,
									style = MaterialTheme.typography.bodySmall,
									color = MaterialTheme.colorScheme.onSurfaceVariant
								)
							}
						}
					}
				}
			}

			Spacer(Modifier.height(24.dp))
		}

		StepNavigation(
			showBack = true,
			onBack = onBack,
			buttonText = stringResource(R.string.onboarding_let_me_in),
			onContinue = onContinue
		)
	}
}
