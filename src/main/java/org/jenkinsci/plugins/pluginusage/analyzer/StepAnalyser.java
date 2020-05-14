package org.jenkinsci.plugins.pluginusage.analyzer;

import hudson.PluginWrapper;
import hudson.model.Job;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTBranch;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTBuildCondition;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTPipelineDef;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTPostBuild;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTPostStage;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStage;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStages;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStep;
import org.jenkinsci.plugins.pipeline.modeldefinition.parser.Converter;
import org.jenkinsci.plugins.pluginusage.JobsPerPlugin;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.flow.FlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;

import java.util.HashMap;
import java.util.Map;

public class StepAnalyser extends JobAnalyzer {

    private Map<String, PluginWrapper> pluginPerFunction = new HashMap<>();
    private boolean hasPlugin;

    public StepAnalyser() {
        hasPlugin = Jenkins.get().getPlugin("maven-plugin") != null;
        if (hasPlugin){
            for (StepDescriptor b : StepDescriptor.all()) {
                PluginWrapper usedPlugin = getUsedPlugin(b.clazz);
                plugins.add(usedPlugin);
                pluginPerFunction.put(b.getFunctionName(), usedPlugin);
            }
        }
    }

    @Override
    protected void doJobAnalyze(Job item, Map<PluginWrapper, JobsPerPlugin> mapJobsPerPlugin) {
        super.doJobAnalyze(null, mapJobsPerPlugin);

        if (hasPlugin){
            if (item instanceof WorkflowJob) {
                WorkflowJob job = (WorkflowJob) item;
                FlowDefinition definition = job.getDefinition();
                if (definition instanceof CpsFlowDefinition) {
                    ModelASTPipelineDef model = Converter.scriptToPipelineDef(((CpsFlowDefinition) definition).getScript());
                    if (model != null) {

                        // stages
                        ModelASTStages stages = model.getStages();
                        for (ModelASTStage stage : stages.getStages()) {
                            for (ModelASTBranch branch : stage.getBranches()) {
                                for (ModelASTStep step : branch.getSteps()) {
                                    if (pluginPerFunction.containsKey(step.getName())) {
                                        addItem(item, mapJobsPerPlugin, pluginPerFunction.get(step.getName()));
                                    }
                                }
                            }

                            ModelASTPostStage postStage = stage.getPost();
                            if (postStage != null) {
                                for (ModelASTBuildCondition condition : postStage.getConditions()) {
                                    ModelASTBranch branch = condition.getBranch();
                                    for (ModelASTStep step : branch.getSteps()) {
                                        if (pluginPerFunction.containsKey(step.getName())) {
                                            addItem(item, mapJobsPerPlugin, pluginPerFunction.get(step.getName()));
                                        }
                                    }
                                }
                            }
                        }

                        // post
                        ModelASTPostBuild postBuild = model.getPostBuild();
                        if (postBuild != null) {
                            for (ModelASTBuildCondition condition : postBuild.getConditions()) {
                                ModelASTBranch branch = condition.getBranch();
                                for (ModelASTStep step : branch.getSteps()) {
                                    if (pluginPerFunction.containsKey(step.getName())) {
                                        addItem(item, mapJobsPerPlugin, pluginPerFunction.get(step.getName()));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}