package nl.ordina.robotics.ssh

object Cmd {
    object Bluetooth {
        fun info(mac: String) = "bluetoothctl info $mac"

        const val paired = "bluetoothctl paired-devices"

        const val list = "bluetoothctl devices"

        const val scan = "bluetoothctl scan on"
    }

    object Git {
        fun clone(repo: String) = "git clone $repo"

        const val pull = "git pull"

        const val revision = "git --no-pager log --decorate --oneline -1"

        const val status = "git status"
    }

    object Networking {
        fun connectWifi(ssid: String, password: String) =
            "sudo nmcli device wifi connect $ssid password $password"

        const val ipAddresses = "ip -br -o -f inet addr show | awk '{ print \$3}'"
    }

    object Ros {
        const val buildInstall = "colcon build --symlink-install"

        const val stop = "pkill -INT -f ros2"

        const val sourceBash = "source /opt/ros/foxy/setup.bash"

        const val sourceLocalSetup = "source install/local_setup.bash"

        const val running = "pgrep -af ros2"

        const val mainCmdRunning = "ros2 launch -n"
    }

    object Unix {
        fun list(dir: String) = "ls -lah $dir"

        const val userInfo = "whoami"
    }
}

fun String.ignoreFailure() = "($this || true)"
