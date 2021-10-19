package org.jenkinsci.plugins.pluginusage.analyzer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import hudson.PluginWrapper;
import hudson.model.FreeStyleProject;
import hudson.model.Job;
import hudson.model.Project;
import hudson.tasks.BuildWrapper;
import hudson.tasks.Builder;

class ProjectAnalyzer extends AbstractProjectAnalyzer {

    @Override
    protected Set<PluginWrapper> getPluginsFromBuilders(Job<?,?> item) {
        Set<PluginWrapper> plugins = new HashSet<>();

        if (item instanceof Project) {
            Project<?,?> project = (Project<?,?>) item;
            List<Builder> builders = project.getBuilders();
            for (Builder builder : builders) {
                plugins.addAll(getPluginsFromBuilder(builder));
            }

            for (BuildWrapper buildWrapper : project.getBuildWrappersList()) {
                plugins.add(getPluginFromClass(buildWrapper.getDescriptor().clazz));
            }

        }
        return plugins;
    }
}
