package dev.robin.privacify.pro

import android.util.Log
import dev.robin.privacify.core.PermissionAutomationController

class RealPermissionAutomationController : PermissionAutomationController {

    private var enabled = false
    private val tag = "RealPermAutoCtrl"

    override fun automatePermissions(enabled: Boolean) {
        this.enabled = enabled
        Log.d(tag, "Permission automation ${if (enabled) "enabled" else "disabled"}")
    }

    override fun isEnabled(): Boolean = enabled
}
