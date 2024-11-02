package dev.nordix.yt.domain.model

import kotlinx.serialization.Serializable

@Serializable
sealed interface ButtonAction : DeviceAction {

    @Serializable
    object ButtonClick : ButtonAction {
        override val code: Int = ButtonActionCodes.BUTTON_CLICK.code
    }

    @Serializable
    object ButtonDoubleClick : ButtonAction {
        override val code: Int = ButtonActionCodes.BUTTON_DOUBLE_CLICK.code
    }

    @Serializable
    object ButtonLongClick : ButtonAction {
        override val code: Int = ButtonActionCodes.BUTTON_LONG_CLICK.code
    }

    enum class ButtonActionCodes(val code: Int) {
        BUTTON_CLICK(0),
        BUTTON_DOUBLE_CLICK(0),
        BUTTON_LONG_CLICK(1),
    }

}
