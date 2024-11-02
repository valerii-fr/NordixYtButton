package dev.nordix.yt.network.wss.server

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import java.time.Duration

fun Application.configureSockets() {

    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(3)
        timeout = Duration.ofSeconds(5)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    install(ContentNegotiation)

}
