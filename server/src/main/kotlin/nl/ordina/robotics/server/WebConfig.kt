package nl.ordina.robotics.server

import nl.ordina.robotics.server.messaging.WebsocketController
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.http.CacheControl
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.ResourceHandlerRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.util.concurrent.TimeUnit

@Configuration
@EnableWebFlux
@EnableScheduling
class WebConfig : WebFluxConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/**")
            .addResourceLocations("/", "classpath:/")
            .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS))
    }

    @Bean
    fun handlerMapping(controller: WebsocketController): HandlerMapping {
        val mapping = mapOf("/connect" to controller)

        return SimpleUrlHandlerMapping(mapping, -1)
    }

    @Bean
    fun handlerAdapter() = WebSocketHandlerAdapter()
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
