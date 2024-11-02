package dev.nordix.yt.domain.helpers

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications

object NotificationHelper {

    private const val NOTIFICATION_GROUP_ID = "Nordix Notifications"
    private const val TITLE = "Nordix"
    private val notificationGroup = NotificationGroupManager.getInstance().getNotificationGroup(NOTIFICATION_GROUP_ID)

    fun notify(message: String) {
        val notification = notificationGroup.createNotification(
            TITLE,
            message,
            NotificationType.INFORMATION
        )
        Notifications.Bus.notify(notification)
    }

}
