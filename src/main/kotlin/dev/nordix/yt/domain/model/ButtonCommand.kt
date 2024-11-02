package dev.nordix.yt.domain.model

import kotlinx.serialization.Serializable

@Serializable
sealed interface ButtonCommand : DeviceAction {

    @Serializable
    object LedOn : ButtonCommand {
        override val code: Int
            get() = ButtonCommandCodes.LED_ON.code
    }

    @Serializable
    object LedOff : ButtonCommand {
        override val code: Int
            get() = ButtonCommandCodes.LED_OFF.code
    }

    enum class ButtonCommandCodes(val code: Int) {
        LED_ON(2),
        LED_OFF(3)
    }

}
