package dev.robin.privacify.pro

import dev.robin.privacify.core.TrackerBlockController
import dev.robin.privacify.pro.utils.ShellUtils

class RealTrackerBlockController : TrackerBlockController {
    override fun blockTrackers(enabled: Boolean) {
        if (enabled) {
            ShellUtils.runRootCommand("mount -o rw,remount /system")
            val cmd = """
                curl -s https://raw.githubusercontent.com/StevenBlack/hosts/master/hosts -o /data/local/tmp/hosts_adblock
                if [ -s /data/local/tmp/hosts_adblock ]; then
                    if [ ! -f /system/etc/hosts.bak ]; then
                        cp /system/etc/hosts /system/etc/hosts.bak
                    fi
                    cat /data/local/tmp/hosts_adblock > /system/etc/hosts
                    chmod 644 /system/etc/hosts
                fi
            """.trimIndent()
            ShellUtils.runRootCommand(cmd)
            ShellUtils.runRootCommand("mount -o ro,remount /system")
        } else {
            ShellUtils.runRootCommand("mount -o rw,remount /system")
            ShellUtils.runRootCommand("if [ -f /system/etc/hosts.bak ]; then cat /system/etc/hosts.bak > /system/etc/hosts; fi")
            ShellUtils.runRootCommand("mount -o ro,remount /system")
        }
    }
}
