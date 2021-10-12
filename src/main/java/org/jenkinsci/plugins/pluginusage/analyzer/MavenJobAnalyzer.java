package org.jenkinsci.plugins.pluginusage.analyzer;

import hudson.PluginWrapper;
import hudson.maven.MavenModuleSet;
import hudson.model.Job;
import hudson.model.ParameterDefinition;
import hudson.model.ParametersDefinitionProperty;
import hudson.tasks.BuildStep;
import hudson.tasks.Builder;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.conditionalbuildstep.ConditionalBuilder;
import org.jenkinsci.plugins.conditionalbuildstep.singlestep.SingleConditionalBuilder;
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
                    processConditionalBuilder(item, mapJobsPerPlugin, builder);
                }
                for (Builder builder : moduleSet.getPostbuilders()) {
                    addItem(item, mapJobsPerPlugin, getUsedPlugin(builder.getDescriptor().clazz));
                    processConditionalBuilder(item, mapJobsPerPlugin, builder);
                }
                processParameters(item, mapJobsPerPlugin);
            }
        }
    }

    private void processConditionalBuilder(Job item, Map<PluginWrapper, JobsPerPlugin> mapJobsPerPlugin, Builder builder) {
        if (Jenkins.get().getPlugin("conditional-buildstep") != null){
            if(builder instanceof ConditionalBuilder){
                ConditionalBuilder conditionalBuilder = (ConditionalBuilder) builder;
                List<Builder> conditionalBuilders = conditionalBuilder.getConditionalbuilders();
                for (Builder innerBuilder: conditionalBuilders) {
                    PluginWrapper usedPlugin = getUsedPlugin(innerBuilder.getDescriptor().clazz);
                    addItem(item, mapJobsPerPlugin, usedPlugin);
                }
            }
            if(builder instanceof SingleConditionalBuilder){
                SingleConditionalBuilder singleConditionalBuilder = (SingleConditionalBuilder) builder;
                BuildStep buildStep = singleConditionalBuilder.getBuildStep();
                if (buildStep instanceof Builder){
                    Builder innerBuilder = (Builder) buildStep;
                    PluginWrapper usedPlugin = getUsedPlugin(innerBuilder.getDescriptor().clazz);
                    addItem(item, mapJobsPerPlugin, usedPlugin);
                }
            }
        }
    }

    private void processParameters(Job project, Map<PluginWrapper, JobsPerPlugin> mapJobsPerPlugin) {
        ParametersDefinitionProperty parameters = project.getAction(ParametersDefinitionProperty.class);
        if (parameters!=null){
            List<ParameterDefinition> parameterDefinitions = parameters.getParameterDefinitions();
            for (ParameterDefinition parameterDefinition: parameterDefinitions) {
                PluginWrapper usedPlugin = getUsedPlugin(parameterDefinition.getDescriptor().clazz);
                addItem(project, mapJobsPerPlugin, usedPlugin);
            }
        }
    }
}
