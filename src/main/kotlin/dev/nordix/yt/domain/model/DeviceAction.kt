package dev.nordix.yt.domain.model

import kotlinx.serialization.Serializable

@Serializable
sealed interface DeviceAction {
    val code: Int
}
