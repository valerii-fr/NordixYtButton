package dev.nordix.yt.network.dnssd.publish

import dev.nordix.yt.domain.helpers.Constants.SERVICE_PORT
import dev.nordix.yt.domain.helpers.Constants.serviceType
import dev.nordix.yt.domain.helpers.NotificationHelper.notify
import java.net.InetAddress
import java.util.UUID
import javax.jmdns.JmDNS
import javax.jmdns.ServiceInfo

class DnssdPublisher {
    val host = InetAddress.getLocalHost()
    private val jmDNS: JmDNS = JmDNS.create(host, host.hostName)
    private val serviceInfo: ServiceInfo = ServiceInfo.create(
        serviceType,
        UUID.randomUUID().toString().take(8),
        SERVICE_PORT,
        "Nordix YouTrack button server"
    )

    fun start() {
        jmDNS.registerService(serviceInfo)
        notify("Nordix service was registered at ${host.hostAddress}")
    }

    fun stop() {
        jmDNS.unregisterService(serviceInfo)
    }

}
