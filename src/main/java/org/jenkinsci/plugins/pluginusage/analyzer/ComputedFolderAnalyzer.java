package org.jenkinsci.plugins.pluginusage.analyzer;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.cloudbees.hudson.plugins.folder.computed.ComputedFolder;
import hudson.PluginWrapper;
import hudson.model.Item;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import jenkins.model.Jenkins;

class ComputedFolderAnalyzer extends AbstractProjectAnalyzer {
    @Override
    protected Set<PluginWrapper> getPluginsFromBuilders(Item item) {
        Set<PluginWrapper> plugins = new HashSet<>();

        if (Jenkins.get().getPlugin("cloudbees-folder") == null){
            return plugins;
        }

        if (item instanceof ComputedFolder) {
            ComputedFolder folder = (ComputedFolder) item;
            plugins.add(getPluginFromClass(folder.getDescriptor().clazz));

            Map<TriggerDescriptor, Trigger<?>> triggers = folder.getTriggers();
            for (Map.Entry<TriggerDescriptor,Trigger<?>> entry : triggers.entrySet()) {
                plugins.add(getPluginFromClass(entry.getKey().clazz));
            }
        }
        return plugins;
    }
}
