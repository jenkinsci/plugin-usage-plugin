package org.jenkinsci.plugins.pluginusage.analyzer;

import hudson.PluginWrapper;
import hudson.maven.MavenModuleSet;
import hudson.model.Job;
import hudson.tasks.Builder;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.pluginusage.JobsPerPlugin;

import java.util.List;
import java.util.Map;

public class MavenJobAnalyzer  extends JobAnalyzer {

    @Override
    protected void doJobAnalyze(Job item, Map<PluginWrapper, JobsPerPlugin> mapJobsPerPlugin) {
        super.doJobAnalyze(null, mapJobsPerPlugin);
        if (Jenkins.get().getPlugin("maven-plugin") != null){
            if (item instanceof MavenModuleSet) {
                PluginWrapper usedPlugin = getUsedPlugin(MavenModuleSet.DescriptorImpl.class);
                if (usedPlugin != null) {
                    JobsPerPlugin jobsPerPlugin = mapJobsPerPlugin.get(usedPlugin);
                    if (jobsPerPlugin != null) {
                        jobsPerPlugin.addProject(item);
                    } else {
                        JobsPerPlugin jobsPerPlugin2 = new JobsPerPlugin(usedPlugin);
                        jobsPerPlugin2.addProject(item);
                        mapJobsPerPlugin.put(usedPlugin, jobsPerPlugin2);
                    }
                }

                final MavenModuleSet moduleSet = (MavenModuleSet) item;
                for (Builder builder : moduleSet.getPrebuilders()) {
                    addItem(item, mapJobsPerPlugin, getUsedPlugin(builder.getDescriptor().clazz));
                }
                for (Builder builder : moduleSet.getPostbuilders()) {
                    addItem(item, mapJobsPerPlugin, getUsedPlugin(builder.getDescriptor().clazz));
                }
            }
        }
    }
}
