package org.jenkinsci.plugins.pluginusage.analyzer;

import java.util.HashSet;
import java.util.Set;

import hudson.PluginWrapper;
import hudson.maven.MavenModuleSet;
import hudson.model.Job;
import hudson.tasks.BuildWrapper;
import hudson.tasks.Builder;
import jenkins.model.Jenkins;

class MavenProjectAnalyzer extends AbstractProjectAnalyzer {

    @Override
    protected Set<PluginWrapper> getPluginsFromBuilders(Job<?,?> item) {
        Set<PluginWrapper> plugins = new HashSet<>();

        if (Jenkins.get().getPlugin("maven-plugin") == null){
            return plugins;
        }

        if (item instanceof MavenModuleSet) {
            plugins.add(getPluginFromClass(MavenModuleSet.DescriptorImpl.class));

            final MavenModuleSet moduleSet = (MavenModuleSet) item;
            for (Builder builder : moduleSet.getPrebuilders()) {
                plugins.addAll(getPluginsFromBuilder(builder));
            }
            for (Builder builder : moduleSet.getPostbuilders()) {
                plugins.addAll(getPluginsFromBuilder(builder));
            }
            for (BuildWrapper buildWrapper : moduleSet.getBuildWrappersList()) {
                plugins.add(getPluginFromClass(buildWrapper.getDescriptor().clazz));
            }
        }

        return plugins;
    }
}
