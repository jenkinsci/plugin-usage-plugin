package org.jenkinsci.plugins.pluginusage.analyzer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import hudson.PluginWrapper;
import hudson.matrix.MatrixProject;
import hudson.model.Job;
import hudson.tasks.BuildWrapper;
import hudson.tasks.Builder;
import jenkins.model.Jenkins;

class MatrixProjectAnalyzer extends AbstractProjectAnalyzer {

    @Override
    protected Set<PluginWrapper> getPluginsFromBuilders(Job<?, ?> item) {
        Set<PluginWrapper> plugins = new HashSet<>();

        if (Jenkins.get().getPlugin("matrix-project") == null){
            return plugins;
        }

        if (item instanceof MatrixProject){
            plugins.add(getPluginFromClass(MatrixProject.DescriptorImpl.class));

            MatrixProject matrixProject = (MatrixProject) item;
            final List<Builder> builders = matrixProject.getBuilders();
            for (Builder builder : builders) {
                plugins.addAll(getPluginsFromBuilder(builder));
            }

            for (BuildWrapper buildWrapper : matrixProject.getBuildWrappersList()) {
                plugins.add(getPluginFromClass(buildWrapper.getDescriptor().clazz));
            }
        }

        return plugins;
    }
}
