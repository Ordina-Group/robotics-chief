package nl.ordina.robotics.server.messaging

import org.springframework.aop.framework.AopProxyUtils
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.core.MethodIntrospector
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.stereotype.Component
import java.lang.reflect.Method
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.kotlinFunction

internal data class RegisteredHandler(
    val instance: Any,
    val method: KFunction<*>,
)

@Component
class MessageHandlerRegistry : BeanPostProcessor {
    private val handlers = mutableMapOf<String, RegisteredHandler>()

    internal fun resolveHandler(topic: String): RegisteredHandler? {
        return handlers[topic]
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any {
        val targetClass = AopProxyUtils.ultimateTargetClass(bean)

        if (AnnotationUtils.isCandidateClass(targetClass, MessageHandler::class.java)) {
            val methodAnnotations = getMethodsWithMessageHandlerAnnotation(targetClass)

            for ((method, annotation) in methodAnnotations) {
                val function = method.kotlinFunction
                    ?: throw IllegalStateException("Kotlin function for ${method.name} method is null")

                register(annotation.topic, bean, function)
            }
        }

        return bean
    }

    private fun register(topic: String, instance: Any, method: KFunction<*>) {
        handlers[topic] = RegisteredHandler(instance, method)
    }

    private fun getMethodsWithMessageHandlerAnnotation(targetType: Class<*>): Map<Method, MessageHandler> =
        MethodIntrospector.selectMethods<MessageHandler>(targetType) { method ->
            AnnotatedElementUtils.getMergedAnnotation(method, MessageHandler::class.java)
        }
}
