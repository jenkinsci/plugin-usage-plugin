package org.jenkinsci.plugins.pluginusage.analyzer;

import hudson.DescriptorExtensionList;
import hudson.PluginWrapper;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.model.ParameterDefinition;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.Project;
import hudson.tasks.BuildStep;
import hudson.tasks.BuildStepCompatibilityLayer;
import hudson.tasks.Builder;

import java.util.List;
import java.util.Map;

import org.jenkinsci.plugins.conditionalbuildstep.ConditionalBuilder;
import org.jenkinsci.plugins.conditionalbuildstep.singlestep.SingleConditionalBuilder;
import org.jenkinsci.plugins.pluginusage.JobsPerPlugin;

public class BuilderJobAnalyzer extends JobAnalyzer {

    public BuilderJobAnalyzer() {
        DescriptorExtensionList<Builder, Descriptor<Builder>> all = Builder.all();
        for (Descriptor<Builder> b : all) {
            PluginWrapper usedPlugin = getUsedPlugin(b.clazz);
            plugins.add(usedPlugin);
        }
    }

    @Override
    protected void doJobAnalyze(AbstractProject item, Map<PluginWrapper, JobsPerPlugin> mapJobsPerPlugin) {
        super.doJobAnalyze(null, mapJobsPerPlugin);
        if (item instanceof Project) {
            Project project = (Project) item;
            List<Builder> builders = project.getBuilders();
            for (Builder builder : builders) {
                PluginWrapper usedPlugin = getUsedPlugin(builder.getDescriptor().clazz);
                addItem(item, mapJobsPerPlugin, usedPlugin);
                processConditionalBuilder(item, mapJobsPerPlugin, builder);
            }
            processParameters(item, mapJobsPerPlugin, project);
        }
    }

    private void processParameters(AbstractProject item, Map<PluginWrapper, JobsPerPlugin> mapJobsPerPlugin, Project project) {
        ParametersDefinitionProperty parameters = project.getAction(ParametersDefinitionProperty.class);
        if (parameters!=null){
            List<ParameterDefinition> parameterDefinitions = parameters.getParameterDefinitions();
            for (ParameterDefinition parameterDefinition: parameterDefinitions) {
                PluginWrapper usedPlugin = getUsedPlugin(parameterDefinition.getDescriptor().clazz);
                addItem(item, mapJobsPerPlugin, usedPlugin);
            }
        }
    }

    private void processConditionalBuilder(AbstractProject item, Map<PluginWrapper, JobsPerPlugin> mapJobsPerPlugin, Builder builder) {
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

    private void addItem(AbstractProject item, Map<PluginWrapper, JobsPerPlugin> mapJobsPerPlugin, PluginWrapper usedPlugin) {
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
    }

}
