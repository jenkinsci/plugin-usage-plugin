package org.jenkinsci.plugins.pluginusage.analyzer;

import hudson.PluginWrapper;
import hudson.model.Describable;
import hudson.model.Descriptor;
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
import org.jenkinsci.plugins.structs.SymbolLookup;
import org.jenkinsci.plugins.structs.describable.DescribableModel;
import org.jenkinsci.plugins.structs.describable.DescribableParameter;
import org.jenkinsci.plugins.structs.describable.HeterogeneousObjectType;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.flow.FlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class StepAnalyser extends JobAnalyzer {

    private final Map<String, PluginWrapper> pluginPerFunction = new HashMap<>();
    private final boolean hasPlugin;

    public StepAnalyser() {
        hasPlugin = Jenkins.get().getPlugin("pipeline-model-definition") != null;
        if (hasPlugin){
            for (StepDescriptor b : StepDescriptor.all()) {
                PluginWrapper usedPlugin = getUsedPlugin(b.clazz);
                plugins.add(usedPlugin);

                // adapted from org.jenkinsci.plugins.workflow.cps.Snippetizer.getQuasiDescriptors()
                if (!b.isAdvanced()) {
                    pluginPerFunction.put(b.getFunctionName(), usedPlugin);
                    if (b.isMetaStep()) {
                        DescribableModel<?> m = new DescribableModel<>(b.clazz);
                        Collection<DescribableParameter> parameters = m.getParameters();
                        if (parameters.size() == 1) {
                            DescribableParameter delegate = parameters.iterator().next();
                            if (delegate.isRequired()) {
                                if (delegate.getType() instanceof HeterogeneousObjectType) {
                                    for (DescribableModel<?> delegateOptionSchema : ((HeterogeneousObjectType) delegate.getType()).getTypes().values()) {
                                        Class<?> delegateOptionType = delegateOptionSchema.getType();
                                        Descriptor<?> delegateDescriptor = Jenkins.getActiveInstance().getDescriptorOrDie(delegateOptionType.asSubclass(Describable.class));
                                        PluginWrapper usedPlugin2 = getUsedPlugin(delegateDescriptor.clazz);
                                        if (usedPlugin2 != null){
                                            Set<String> symbols = SymbolLookup.getSymbolValue(delegateDescriptor);
                                            if (!symbols.isEmpty()) {
                                                for (String symbol : symbols) {
                                                    pluginPerFunction.put(symbol, usedPlugin2);
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