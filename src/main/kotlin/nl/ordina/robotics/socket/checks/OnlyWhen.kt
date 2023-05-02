package nl.ordina.robotics.socket.checks

internal fun String.onlyWhen(condition: Boolean): String? =
    if (condition) {
        this
    } else {
        null
    }
