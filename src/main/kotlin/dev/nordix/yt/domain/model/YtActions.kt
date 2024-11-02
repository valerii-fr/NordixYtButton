package dev.nordix.yt.domain.model

enum class YtActions(val actionId: String) {
    StopTrackerAction(
        actionId = "com.github.jk1.ytplugin.timeTracker.actions.StopTrackerAction"
    ),
    PauseTrackerAction(
        actionId = "com.github.jk1.ytplugin.timeTracker.actions.PauseTrackerAction"
    ),
    StartTrackerAction(
        actionId = "com.github.jk1.ytplugin.timeTracker.actions.StartTrackerAction"
    ),
}
