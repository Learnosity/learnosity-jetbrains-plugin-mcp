package com.learnosity.buildaide

import com.intellij.ide.plugins.DynamicPluginListener
import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.openapi.components.service

private const val PLUGIN_ID = "com.learnosity.buildaide"

class BuildAideUninstallListener : DynamicPluginListener {
    override fun beforePluginUnload(pluginDescriptor: IdeaPluginDescriptor, isUpdate: Boolean) {
        if (isUpdate || pluginDescriptor.pluginId.idString != PLUGIN_ID) return
        service<BuildAideRegistrar>().unregister()
    }
}
