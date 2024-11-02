package dev.nordix.yt.domain.helpers

import dev.nordix.yt.domain.model.ButtonAction
import dev.nordix.yt.domain.model.ButtonCommand
import dev.nordix.yt.domain.model.DeviceAction
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

object JsonHelper {

    val json = Json {
        serializersModule = SerializersModule {
            polymorphic(DeviceAction::class) {
                polymorphic(ButtonAction::class) {
                    subclass(ButtonAction.ButtonClick::class, ButtonAction.ButtonClick.serializer())
                    subclass(ButtonAction.ButtonLongClick::class, ButtonAction.ButtonLongClick.serializer())
                }
                polymorphic(ButtonCommand::class) {
                    subclass(ButtonCommand.LedOn::class, ButtonCommand.LedOn.serializer())
                    subclass(ButtonCommand.LedOff::class, ButtonCommand.LedOff.serializer())
                }
            }
        }
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = true
        encodeDefaults = true
        classDiscriminator = "type"
    }

}
