import os
import re

base_dir = "/data/data/com.termux/files/home/privacify"

# 1. Clean build.gradle.kts
bg_path = os.path.join(base_dir, "app/build.gradle.kts")
with open(bg_path, "r") as f:
    bg_content = f.read()

bg_content = re.sub(r'val isProBuild =.*?\n\}\n', '', bg_content, flags=re.DOTALL)
bg_content = re.sub(r'\t?sourceSets \{[\s\S]*?\n\t?\}\n', '', bg_content)
bg_content = re.sub(r'tasks\.register\("assemblePro.*?\n\}\n', '', bg_content, flags=re.DOTALL)
bg_content = re.sub(r'tasks\.register\("installPro.*?\n\}\n', '', bg_content, flags=re.DOTALL)

with open(bg_path, "w") as f:
    f.write(bg_content)


# 2. Update RealTrackerBlockController.kt
rtb_path = os.path.join(base_dir, "app/src/main/java/dev/robin/privacify/pro/RealTrackerBlockController.kt")
rtb_content = """package dev.robin.privacify.pro

import dev.robin.privacify.core.TrackerBlockController
import dev.robin.privacify.pro.utils.ShellUtils

class RealTrackerBlockController : TrackerBlockController {
    override fun blockTrackers(enabled: Boolean) {
        if (enabled) {
            ShellUtils.runRootCommand("mount -o rw,remount /system")
            val cmd = \"\"\"
                curl -s https://raw.githubusercontent.com/StevenBlack/hosts/master/hosts -o /data/local/tmp/hosts_adblock
                if [ -s /data/local/tmp/hosts_adblock ]; then
                    if [ ! -f /system/etc/hosts.bak ]; then
                        cp /system/etc/hosts /system/etc/hosts.bak
                    fi
                    cat /data/local/tmp/hosts_adblock > /system/etc/hosts
                    chmod 644 /system/etc/hosts
                fi
            \"\"\".trimIndent()
            ShellUtils.runRootCommand(cmd)
            ShellUtils.runRootCommand("mount -o ro,remount /system")
        } else {
            ShellUtils.runRootCommand("mount -o rw,remount /system")
            ShellUtils.runRootCommand("if [ -f /system/etc/hosts.bak ]; then cat /system/etc/hosts.bak > /system/etc/hosts; fi")
            ShellUtils.runRootCommand("mount -o ro,remount /system")
        }
    }
}
"""
with open(rtb_path, "w") as f:
    f.write(rtb_content)


# 3. Update AutoGuardService.kt
ags_path = os.path.join(base_dir, "app/src/main/java/dev/robin/privacify/core/autoguard/AutoGuardService.kt")
with open(ags_path, "r") as f:
    ags_content = f.read()

comm_func = """
    private fun isCommunicationApp(pkg: String?): Boolean {
        if (pkg == null) return false
        val commApps = setOf("com.whatsapp", "org.telegram.messenger", "com.tencent.mm", "com.viber.voip", "com.facebook.orca", "com.discord")
        return commApps.contains(pkg) || pkg.contains("dialer") || pkg.contains("telecom") || pkg.contains("messaging")
    }
"""
if "isCommunicationApp" not in ags_content:
    last_brace_idx = ags_content.rfind("}")
    ags_content = ags_content[:last_brace_idx] + comm_func + "\n}\n"

ags_content = ags_content.replace(
    "val micBlock = micOn && !callActive",
    "val isMicApp = isCommunicationApp(foreground) || callActive\n        val micBlock = micOn && !isMicApp"
)
ags_content = ags_content.replace(
    "val cameraBlock = cameraOn && !(cameraInUse && isCameraApp(foreground))",
    "val cameraBlock = cameraOn && !isCameraApp(foreground)"
)

with open(ags_path, "w") as f:
    f.write(ags_content)


# 4. Clean SettingsScreen.kt
ss_path = os.path.join(base_dir, "app/src/main/java/dev/robin/privacify/presentation/settings/SettingsScreen.kt")
with open(ss_path, "r") as f:
    ss_content = f.read()

def remove_update_row(content):
    import re
    # Try to find the Check for Updates SettingsRow block
    match = re.search(r'\t*SettingsRow\(\s*title = (stringResource\(R\.string\.settings_check_updates.*?\)|\"Check for Updates\").*?\n\t*\)\n\t*PrivacifyDivider\(.*?start = 56\.dp\)\n', content, flags=re.DOTALL)
    if match:
        content = content.replace(match.group(0), "")
    
    # Remove ProDialog
    content = re.sub(r'\t*var showUpdateProDialog.*?mutableStateOf\(false\).*?\n', '', content)
    content = re.sub(r'\t*if \(showUpdateProDialog\)\s*\{\s*PrivacifyProDialog\([\s\S]*?\}\n', '', content)
    return content

ss_content = remove_update_row(ss_content)

with open(ss_path, "w") as f:
    f.write(ss_content)

print("Refactor script executed.")
