package com.learnosity.buildaide

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.listCellRenderer.textListCellRenderer

/** Settings | Tools | Learnosity Build Aide: which regional server the AI clients connect to. */
class BuildAideConfigurable : BoundConfigurable("Learnosity Build Aide") {
    override fun createPanel() = panel {
        row("Region:") {
            comboBox(Region.entries, textListCellRenderer { it?.label })
                .bindItem({ RegionSettings.region }, { RegionSettings.region = it ?: Region.AMERICA })
                .comment("Choose the server nearest you for faster responses.")
        }
    }

    override fun apply() {
        super.apply()
        ApplicationManager.getApplication().executeOnPooledThread {
            service<BuildAideRegistrar>().updateServerUrls()
        }
    }
}
