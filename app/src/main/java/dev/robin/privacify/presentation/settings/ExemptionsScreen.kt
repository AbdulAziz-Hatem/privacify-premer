package dev.robin.privacify.presentation.settings

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.robin.privacify.ui.theme.ExpressiveExtraLargeIncreased
import dev.robin.privacify.ui.theme.MdSpacing

private const val PREFS_NAME = "privacify_prefs"

private fun exemptionKey(sensorType: String) = "auto_guard_exemption_$sensorType"

private fun getExemptionSet(context: Context, sensorType: String): Set<String> {
	val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
	return prefs.getStringSet(exemptionKey(sensorType), emptySet()) ?: emptySet()
}

private fun addExemption(context: Context, sensorType: String, pkg: String) {
	val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
	val current = prefs.getStringSet(exemptionKey(sensorType), emptySet())?.toMutableSet() ?: mutableSetOf()
	current.add(pkg)
	prefs.edit().putStringSet(exemptionKey(sensorType), current).apply()
}

private fun removeExemption(context: Context, sensorType: String, pkg: String) {
	val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
	val current = prefs.getStringSet(exemptionKey(sensorType), emptySet())?.toMutableSet() ?: mutableSetOf()
	current.remove(pkg)
	prefs.edit().putStringSet(exemptionKey(sensorType), current).apply()
}

private data class SensorTab(val type: String, val label: String, val icon: ImageVector)

private val sensorTabs = listOf(
	SensorTab("MIC", "Microphone", Icons.Outlined.Mic),
	SensorTab("CAMERA", "Camera", Icons.Outlined.Videocam),
	SensorTab("LOCATION", "Location", Icons.Outlined.LocationOn),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExemptionsScreen(onBack: () -> Unit) {
	val context = LocalContext.current
	var selectedTabIndex by remember { mutableIntStateOf(0) }
	var showAppPicker by remember { mutableStateOf(false) }
	var refreshTrigger by remember { mutableIntStateOf(0) }

	val currentSensor = sensorTabs[selectedTabIndex].type
	val exemptions = remember(currentSensor, refreshTrigger) {
		getExemptionSet(context, currentSensor)
	}

	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text("Exempted Apps", fontWeight = FontWeight.Bold) },
				navigationIcon = {
					IconButton(onClick = onBack) {
						Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
					}
				}
			)
		},
		floatingActionButton = {
			SmallFloatingActionButton(onClick = { showAppPicker = true }) {
				Icon(Icons.Filled.Add, contentDescription = "Add app")
			}
		}
	) { padding ->
		Column(modifier = Modifier.fillMaxSize().padding(padding)) {
			TabRow(
				selectedTabIndex = selectedTabIndex,
				containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
				contentColor = MaterialTheme.colorScheme.primary,
				indicator = { tabPositions ->
					TabRowDefaults.SecondaryIndicator(
						modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
						height = 3.dp,
						color = MaterialTheme.colorScheme.primary
					)
				}
			) {
				sensorTabs.forEachIndexed { index, tab ->
					Tab(
						selected = selectedTabIndex == index,
						onClick = { selectedTabIndex = index },
						selectedContentColor = MaterialTheme.colorScheme.primary,
						unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
						text = {
							Text(
								text = tab.label,
								fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
								style = MaterialTheme.typography.labelLarge
							)
						},
						icon = { Icon(tab.icon, contentDescription = null, modifier = Modifier.size(20.dp)) }
					)
				}
			}

			if (exemptions.isEmpty()) {
				Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
					Column(horizontalAlignment = Alignment.CenterHorizontally) {
						Text(
							text = "No exemptions",
							style = MaterialTheme.typography.titleMedium,
							fontWeight = FontWeight.Bold,
							color = MaterialTheme.colorScheme.onSurface
						)
						Spacer(modifier = Modifier.height(MdSpacing.xxs))
						Text(
							text = "Tap + to add apps exempt from\n${sensorTabs[selectedTabIndex].label.lowercase()} blocking",
							style = MaterialTheme.typography.bodyMedium,
							color = MaterialTheme.colorScheme.onSurfaceVariant,
							textAlign = TextAlign.Center
						)
					}
				}
			} else {
				LazyColumn(
					modifier = Modifier.fillMaxSize(),
					contentPadding = PaddingValues(MdSpacing.sm),
					verticalArrangement = Arrangement.spacedBy(MdSpacing.xs)
				) {
					items(exemptions.toList(), key = { it }) { pkg ->
						ExemptionItem(
							packageName = pkg,
							onRemove = {
								removeExemption(context, currentSensor, pkg)
								refreshTrigger++
							}
						)
					}
				}
			}
		}
	}

	if (showAppPicker) {
		AppPickerDialog(
			currentExemptions = getExemptionSet(context, currentSensor),
			onSelect = { pkg ->
				addExemption(context, currentSensor, pkg)
				refreshTrigger++
				showAppPicker = false
			},
			onDismiss = { showAppPicker = false }
		)
	}
}

@Composable
private fun ExemptionItem(packageName: String, onRemove: () -> Unit) {
	val context = LocalContext.current
	val appDisplayName = remember(packageName) {
		try {
			val pm = context.packageManager
			val ai = pm.getApplicationInfo(packageName, 0)
			pm.getApplicationLabel(ai).toString()
		} catch (_: Exception) {
			packageName
		}
	}
	val iconPainter = remember(packageName) {
		try {
			val pm = context.packageManager
			val ai = pm.getApplicationInfo(packageName, 0)
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

	Surface(
		shape = MaterialTheme.shapes.extraLarge,
		color = MaterialTheme.colorScheme.surfaceContainerHigh
	) {
		Row(
			modifier = Modifier.fillMaxWidth().padding(MdSpacing.sm),
			verticalAlignment = Alignment.CenterVertically
		) {
			if (iconPainter != null) {
				Icon(
					painter = iconPainter,
					contentDescription = null,
					modifier = Modifier.size(40.dp).clip(CircleShape),
					tint = MaterialTheme.colorScheme.onSurface
				)
			} else {
				Box(
					modifier = Modifier
						.size(40.dp)
						.clip(CircleShape)
						.background(MaterialTheme.colorScheme.primaryContainer),
					contentAlignment = Alignment.Center
				) {
					Text(
						text = appDisplayName.take(1).uppercase(),
						style = MaterialTheme.typography.titleMedium
					)
				}
			}
			Spacer(modifier = Modifier.width(MdSpacing.sm))
			Column(modifier = Modifier.weight(1f)) {
				Text(
					text = appDisplayName,
					style = MaterialTheme.typography.bodyLarge,
					fontWeight = FontWeight.Bold,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis
				)
				Text(
					text = packageName,
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis
				)
			}
			IconButton(onClick = onRemove) {
				Icon(
					Icons.Filled.Close,
					contentDescription = "Remove exemption",
					tint = MaterialTheme.colorScheme.error
				)
			}
		}
	}
}

private data class PickerAppInfo(
	val packageName: String,
	val displayName: String,
	val iconPainter: androidx.compose.ui.graphics.painter.Painter?
)

@Composable
private fun AppPickerDialog(
	currentExemptions: Set<String>,
	onSelect: (String) -> Unit,
	onDismiss: () -> Unit
) {
	val context = LocalContext.current
	var searchQuery by remember { mutableStateOf("") }

	val allApps = remember {
		try {
			val pm = context.packageManager
			val intent = Intent(Intent.ACTION_MAIN).apply {
				addCategory(Intent.CATEGORY_LAUNCHER)
			}
			val packages = pm.queryIntentActivities(intent, 0)
				.map { it.activityInfo.packageName }
				.distinct()
				.sorted()
			packages.mapNotNull { pkg ->
				if (pkg in currentExemptions) return@mapNotNull null
				try {
					val ai = pm.getApplicationInfo(pkg, 0)
					val name = pm.getApplicationLabel(ai).toString()
					val drawable = pm.getApplicationIcon(ai)
					val w = drawable.intrinsicWidth.coerceAtLeast(1)
					val h = drawable.intrinsicHeight.coerceAtLeast(1)
					val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
					val canvas = Canvas(bitmap)
					drawable.setBounds(0, 0, w, h)
					drawable.draw(canvas)
					PickerAppInfo(pkg, name, BitmapPainter(bitmap.asImageBitmap()))
				} catch (_: Exception) {
					PickerAppInfo(pkg, pkg, null)
				}
			}
		} catch (_: Exception) {
			emptyList()
		}
	}

	val filteredApps = remember(searchQuery, allApps) {
		if (searchQuery.isBlank()) allApps
		else allApps.filter {
			it.displayName.contains(searchQuery, ignoreCase = true) ||
			it.packageName.contains(searchQuery, ignoreCase = true)
		}
	}

	AlertDialog(
		onDismissRequest = onDismiss,
		shape = ExpressiveExtraLargeIncreased,
		title = {
			Text("Add App", fontWeight = FontWeight.Bold)
		},
		text = {
			Column {
				OutlinedTextField(
					value = searchQuery,
					onValueChange = { searchQuery = it },
					placeholder = { Text("Search apps...") },
					modifier = Modifier.fillMaxWidth(),
					singleLine = true,
					shape = MaterialTheme.shapes.large
				)
				Spacer(modifier = Modifier.height(MdSpacing.xs))
				if (filteredApps.isEmpty()) {
					Text(
						text = "No apps found",
						color = MaterialTheme.colorScheme.onSurfaceVariant,
						modifier = Modifier.padding(vertical = MdSpacing.sm)
					)
				} else {
					Box(modifier = Modifier.fillMaxWidth().height(360.dp)) {
						Column(
							modifier = Modifier
								.fillMaxSize()
								.verticalScroll(rememberScrollState()),
							verticalArrangement = Arrangement.spacedBy(MdSpacing.xxs)
						) {
							filteredApps.forEach { app ->
								PickerItem(
									displayName = app.displayName,
									packageName = app.packageName,
									iconPainter = app.iconPainter,
									onClick = { onSelect(app.packageName) }
								)
							}
						}
					}
				}
			}
		},
		confirmButton = {},
		dismissButton = {
			TextButton(onClick = onDismiss) {
				Text("Cancel", fontWeight = FontWeight.Bold)
			}
		}
	)
}

@Composable
private fun PickerItem(
	displayName: String,
	packageName: String,
	iconPainter: androidx.compose.ui.graphics.painter.Painter?,
	onClick: () -> Unit
) {
	Surface(
		modifier = Modifier
			.fillMaxWidth()
			.clickable(onClick = onClick),
		shape = MaterialTheme.shapes.large,
		color = MaterialTheme.colorScheme.surfaceContainerHigh
	) {
		Row(
			modifier = Modifier.padding(horizontal = MdSpacing.sm, vertical = MdSpacing.xs),
			verticalAlignment = Alignment.CenterVertically
		) {
			if (iconPainter != null) {
				Icon(
					painter = iconPainter,
					contentDescription = null,
					modifier = Modifier.size(32.dp).clip(CircleShape),
					tint = MaterialTheme.colorScheme.onSurface
				)
				Spacer(modifier = Modifier.width(MdSpacing.sm))
			}
			Column {
				Text(
					text = displayName,
					style = MaterialTheme.typography.bodyLarge,
					fontWeight = FontWeight.Medium,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis
				)
				Text(
					text = packageName,
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis
				)
			}
		}
	}
}
