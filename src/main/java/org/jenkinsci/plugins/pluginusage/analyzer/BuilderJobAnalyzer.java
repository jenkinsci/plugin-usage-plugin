package org.jenkinsci.plugins.pluginusage.analyzer;

import hudson.DescriptorExtensionList;
import hudson.PluginWrapper;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.model.ParameterDefinition;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.Project;
import hudson.plugins.promoted_builds.PromotedProjectAction;
import hudson.plugins.promoted_builds.PromotionProcess;
import hudson.tasks.BuildStep;
import hudson.tasks.Builder;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.conditionalbuildstep.ConditionalBuilder;
import org.jenkinsci.plugins.conditionalbuildstep.singlestep.SingleConditionalBuilder;
import org.jenkinsci.plugins.pluginusage.JobsPerPlugin;

import java.util.List;
import java.util.Map;

public class BuilderJobAnalyzer extends JobAnalyzer {

    public BuilderJobAnalyzer() {
        DescriptorExtensionList<Builder, Descriptor<Builder>> all = Builder.all();
        for (Descriptor<Builder> b : all) {
            PluginWrapper usedPlugin = getUsedPlugin(b.clazz);
            plugins.add(usedPlugin);
        }
    }

    @Override
    protected void doJobAnalyze(Job item, Map<PluginWrapper, JobsPerPlugin> mapJobsPerPlugin) {
        super.doJobAnalyze(null, mapJobsPerPlugin);
        if (item instanceof Project) {
            Project<?,?> project = (Project) item;
            List<Builder> builders = project.getBuilders();
            for (Builder builder : builders) {
                PluginWrapper usedPlugin = getUsedPlugin(builder.getDescriptor().clazz);
                addItem(item, mapJobsPerPlugin, usedPlugin);
                processConditionalBuilder(item, mapJobsPerPlugin, builder);
            }
            processPromotedBuilds(item, mapJobsPerPlugin);
            processParameters(project, mapJobsPerPlugin);
        }
    }

    private void processPromotedBuilds(Job item, Map<PluginWrapper, JobsPerPlugin> mapJobsPerPlugin) {
        if (Jenkins.get().getPlugin("promoted-builds") != null){
            PromotedProjectAction action = item.getAction(PromotedProjectAction.class);
            if (action != null){
                List<PromotionProcess> processes = action.getProcesses();
                for (PromotionProcess process: processes) {
                    List<BuildStep> buildSteps = process.getBuildSteps();
                    for (BuildStep buildStep: buildSteps) {
                        if (buildStep instanceof Builder){
                            Builder innerBuilder = (Builder) buildStep;
                            PluginWrapper usedPlugin = getUsedPlugin(innerBuilder.getDescriptor().clazz);
                            addItem(item, mapJobsPerPlugin, usedPlugin);
                        }
                    }
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

    private void addItem(Job item, Map<PluginWrapper, JobsPerPlugin> mapJobsPerPlugin, PluginWrapper usedPlugin) {
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
