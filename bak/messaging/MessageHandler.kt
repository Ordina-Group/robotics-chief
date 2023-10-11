package nl.ordina.robotics.server.messaging

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class MessageHandler(val topic: String)
