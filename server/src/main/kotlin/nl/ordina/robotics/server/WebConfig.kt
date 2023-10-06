package nl.ordina.robotics.server

import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.http.CacheControl
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.util.concurrent.TimeUnit

@Configuration
@EnableWebMvc
@EnableScheduling
class WebConfig : WebMvcConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/**")
            .addResourceLocations("/", "classpath:/")
            .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS))
    }
}

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig : WebSocketMessageBrokerConfigurer {
    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/connect")
    }

//    override fun configureMessageConverters(messageConverters: MutableList<MessageConverter>): Boolean {
//        messageConverters.add(
//            KotlinSerializationJsonMessageConverter(),
//        )
//
//        return true
//    }

    override fun configureMessageBroker(config: MessageBrokerRegistry) {
        config.setApplicationDestinationPrefixes("/")
//        config.enableStompBrokerRelay("/")
//        config.enableSimpleBroker("/topic", "/queue")
    }
}

@Controller
class IndexController {
    @GetMapping("/")
    fun serveIndexHtml(): Mono<ResponseEntity<Resource>> = try {
        val resource: Resource = ClassPathResource("index.html") // Adjust the path as needed

        ResponseEntity.ok()
            .contentType(MediaType.TEXT_HTML)
            .body(resource)
            .toMono()
    } catch (e: Exception) {
        Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build())
    }
}
