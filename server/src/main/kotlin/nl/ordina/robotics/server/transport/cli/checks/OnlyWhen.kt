package nl.ordina.robotics.server.transport.cli.checks

internal fun String.onlyWhen(condition: Boolean): String? =
    if (condition) {
        this
    } else {
        null
    }
