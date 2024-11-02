package dev.nordix.yt.domain.helpers

object Constants {
    const val SERVICE_NAME = "_dev_nordix_yt"
    const val SERVICE_PROTOCOL = "_tcp"
    const val SERVICE_DOMAIN = "local"
    const val SERVICE_PORT = 11443
    val serviceType get() = "$SERVICE_NAME.$SERVICE_PROTOCOL.$SERVICE_DOMAIN."
}
