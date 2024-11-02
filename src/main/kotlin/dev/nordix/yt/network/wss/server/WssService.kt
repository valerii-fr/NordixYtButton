package dev.nordix.yt.network.wss.server

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import dev.nordix.yt.domain.helpers.CertHelper
import dev.nordix.yt.domain.helpers.Constants.SERVICE_PORT
import dev.nordix.yt.domain.helpers.JsonHelper
import dev.nordix.yt.domain.helpers.NotificationHelper.notify
import dev.nordix.yt.domain.model.ButtonAction
import dev.nordix.yt.domain.model.ButtonCommand
import dev.nordix.yt.domain.model.DeviceAction
import dev.nordix.yt.domain.model.YtActions
import dev.nordix.yt.network.dnssd.publish.DnssdPublisher
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.embeddedServer
import io.ktor.server.engine.sslConnector
import io.ktor.server.netty.Netty
import io.ktor.server.routing.routing
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@Service(Service.Level.PROJECT)
class WssService(private val project: Project) : Disposable {

    private val store: PropertiesComponent = PropertiesComponent.getInstance(project)
    private val isRunningFlow = MutableStateFlow(store.getBoolean("timeTracker.isRunning"))
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val publisher: DnssdPublisher = DnssdPublisher()
    private val actionManager = ActionManager.getInstance()

    private val isRunning get() = store.getBoolean("timeTracker.isRunning")

    val keyStore = CertHelper.generateSelfSignedCertificate()

    init {
        scope.launch {
            while (true) {
                isRunningFlow.value = isRunning
                delay(500)
            }
        }
    }

    val env = applicationEngineEnvironment {
        sslConnector(
            keyStore = keyStore,
            keyAlias = CertHelper.KEY_ALIAS,
            keyStorePassword = { CertHelper.STORE_PASSWORD.toCharArray() },
            privateKeyPassword = { CertHelper.KEY_PASSWORD.toCharArray() }
        ) {
            port = SERVICE_PORT
            keyStorePath = null
        }
        module {
            configureSockets()

            routing {
                webSocket("/ws") {

                    scope.launch {
                        isRunningFlow
                            .onEach { isRunning ->
                                val initialCommand = if (isRunning) ButtonCommand.LedOn else ButtonCommand.LedOff

                                send(Frame.Text(
                                    JsonHelper.json.encodeToString(DeviceAction.serializer(), initialCommand)
                                ))
                            }
                            .launchIn(this)
                    }

                    notify("Button is connected at ${call.request.local.remoteAddress}")
                    val initialCommand = if (isRunning) ButtonCommand.LedOn else ButtonCommand.LedOff

                    send(Frame.Text(
                        JsonHelper.json.encodeToString(DeviceAction.serializer(), initialCommand)
                    ))

                    try {
                        for (frame in incoming) {
                            when (frame) {
                                is Frame.Text -> {
                                    val running = isRunning
                                    val action: ButtonAction = JsonHelper.json.decodeFromString(frame.data.decodeToString())
                                    val selector = when (action) {
                                        ButtonAction.ButtonClick -> if (running) {
                                                YtActions.StopTrackerAction.actionId
                                            } else {
                                                YtActions.StartTrackerAction.actionId
                                            }
                                        ButtonAction.ButtonLongClick -> if (running) {
                                                YtActions.PauseTrackerAction.actionId
                                            } else {
                                                YtActions.StartTrackerAction.actionId
                                            }

                                        ButtonAction.ButtonDoubleClick -> if (running) {
                                            YtActions.PauseTrackerAction.actionId
                                        } else {
                                            YtActions.StartTrackerAction.actionId
                                        }
                                    }
                                    val responseCommand = executeAction(selector, running)
                                    send(Frame.Text(
                                        JsonHelper.json.encodeToString(DeviceAction.serializer(), responseCommand)
                                    ))
                                }
                                else -> { }
                            }
                        }
                        notify("Button at ${call.request.local.remoteAddress} is disconnected")
                    } catch (_: ClosedReceiveChannelException) {
                        println("Button is disconnected: ${closeReason.await()}")
                    } catch (e: Throwable) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private val server = embeddedServer(
        factory = Netty,
        environment = env
    )

    private fun executeAction(actionId: String, running: Boolean) : ButtonCommand {
        actionManager.getAction(actionId)?.let { action ->
            val ctx = SimpleDataContext.getProjectContext(project)
            action.actionPerformed(
                AnActionEvent.createFromAnAction(action, null, "", ctx)
            )
        } ?: run { notify("Action $actionId not found") }
        return if(running) {
            ButtonCommand.LedOff
        } else {
            ButtonCommand.LedOn
        }
    }

    override fun dispose() {
        server.stop(
            gracePeriodMillis = 500L,
            timeoutMillis =  1500L
        )
        publisher.stop()
    }

    init {
        publisher.start()
        scope.launch {
            server.start(wait = true)
        }
    }
}
