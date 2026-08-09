import dev.robin.privacify.R
import dev.robin.privacify.core.utils.AppContextProvider
package dev.robin.privacify.presentation.home

data class DashboardUiState(
	val privacyScore: Int = 100,
	val statusSubtitle: String = AppContextProvider.context.getString(R.string.dashboard_status_subtitle),
	val totalApps: Int = 0,
	val totalPermissions: Int = 0,
	val highRiskCount: Int = 0,
	val micAccessCount: Int = 0,
	val cameraAccessCount: Int = 0,
	val locationAccessCount: Int = 0,
	val lockdownEnabled: Boolean = false,
	val micDisabled: Boolean = false,
	val cameraDisabled: Boolean = false,
	val locationDisabled: Boolean = false,
	val isRooted: Boolean = false,
	val isScanning: Boolean = false,
	val shellType: String = "auto",
	val automationEnabled: Boolean = false
)

enum class QuickAction {
	Lockdown,
	MicKill,
	CameraKill,
	LocationKill
}
