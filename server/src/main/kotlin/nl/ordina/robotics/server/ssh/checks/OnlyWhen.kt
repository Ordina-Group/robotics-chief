package nl.ordina.robotics.server.ssh.checks

internal fun String.onlyWhen(condition: Boolean): String? =
    if (condition) {
        this
    } else {
        null
    }
