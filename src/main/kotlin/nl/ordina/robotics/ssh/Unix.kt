package nl.ordina.robotics.ssh

object Cmd {
    object Bluetooth {
        fun connect(mac: String) = "bluetoothctl connect $mac"

        fun disconnect(mac: String) = "bluetoothctl disconnect $mac"

        fun info(mac: String) = "bluetoothctl info $mac"

        const val paired = "bluetoothctl paired-devices"

        const val list = "bluetoothctl devices"

        const val scan = "bluetoothctl scan on"
    }

    object Git {
        fun clone(repo: String, directory: String) = "git clone $repo $directory"

        const val pull = "git pull"

        const val revision = "git --no-pager log --decorate --oneline -1"

        const val status = "git status"
    }

    object Networking {
        fun connectWifi(ssid: String, password: String) =
            "sudo nmcli device wifi connect '$ssid' password '$password'"

        const val connectionInfo = "iwconfig wlan0"

        const val ipAddresses = "ip -br -o -f inet addr show | awk '{ print \$3}'"
    }

    object Ros {
        const val buildInstall = "colcon build --symlink-install"

        const val stop = "pkill -INT -f ros2"

        const val sourceBash = "source /opt/ros/foxy/setup.bash"

        const val sourceLocalSetup = "source install/local_setup.bash"

        const val running = "pgrep -af ros2"

        const val mainCmdRunning = "ros2 launch -n"

        fun listTopics(domainId: Int) = "ROS_DOMAIN_ID=$domainId ros2 topic list"

        fun topicInfo(domainId: Int, topicId: String) = "ROS_DOMAIN_ID=$domainId ros2 topic info $topicId"

        fun subscribeTopic(domainId: Int, topicId: String) = "ROS_DOMAIN_ID=$domainId ros2 topic echo $topicId"

        fun launch(domainId: Int) = "ROS_DOMAIN_ID=$domainId ros2 launch -n robot_app gamepad_launch.py gamepad_type:=playstation &"
    }

    object Unix {
        const val And = " && "

        fun cd(dir: String) = "cd $dir"

        fun list(dir: String) = "ls -lah $dir"

        fun addToBashRc(command: String) = "echo \"$command\" >> ~/.bashrc"

        const val userInfo = "whoami"

        const val osInfo = "lsb_release -a"
    }
}

fun String.ignoreFailure() = "($this || true)"
