package dev.nordix.yt.domain.activity

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import dev.nordix.yt.network.wss.server.WssService

class NordixYtProjectActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        project.service<WssService>()
    }
}
