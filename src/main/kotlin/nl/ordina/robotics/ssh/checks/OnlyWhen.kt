package nl.ordina.robotics.ssh.checks

internal fun String.onlyWhen(condition: Boolean): String? =
    if (condition) {
        this
    } else {
        null
    }
