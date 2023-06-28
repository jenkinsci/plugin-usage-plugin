package org.jenkinsci.plugins.pluginusage.analyzer;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import hudson.PluginWrapper;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Item;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.structs.SymbolLookup;
import org.jenkinsci.plugins.structs.describable.UninstantiatedDescribable;
import org.jenkinsci.plugins.workflow.actions.ArgumentsAction;
import org.jenkinsci.plugins.workflow.cps.nodes.StepAtomNode;
import org.jenkinsci.plugins.workflow.cps.nodes.StepStartNode;
import org.jenkinsci.plugins.workflow.flow.FlowExecution;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.graphanalysis.DepthFirstScanner;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.steps.CoreStep;
import org.jenkinsci.plugins.workflow.steps.CoreWrapperStep;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;

class PipelineLastBuildAnalyzer extends AbstractProjectAnalyzer {

    private final boolean hasPlugin;

    public PipelineLastBuildAnalyzer() {
        this.hasPlugin = Jenkins.get().getPlugin("pipeline-model-definition") != null;
    }

    @Override
    protected Set<PluginWrapper> getPluginsFromBuilders(Item item) {
        final Set<PluginWrapper> plugins = new HashSet<>();

        if (!hasPlugin) {
            return plugins;
        }

        if (item instanceof WorkflowJob){
            final WorkflowJob workflowJob = (WorkflowJob) item;
            final var jobs = new HashSet<WorkflowRun>();
            jobs.add(workflowJob.getLastBuild());
            jobs.add(workflowJob.getLastSuccessfulBuild());
            jobs.add(workflowJob.getLastCompletedBuild());
            jobs.add(workflowJob.getLastStableBuild());
            jobs.forEach(build -> processRun(plugins, build));
        }
        return plugins;
    }

    private void processRun(Set<PluginWrapper> plugins, WorkflowRun lastBuild) {
        if (lastBuild == null) {
            return;
        }
        final FlowExecution execution = lastBuild.getExecution();
        if (execution == null) {
            return;
        }
        final List<FlowNode> currentHeads = execution.getCurrentHeads();

        final DepthFirstScanner depthFirstScanner = new DepthFirstScanner();
        depthFirstScanner.visitAll(currentHeads, f -> {
            if (f instanceof StepStartNode){
                final StepStartNode startNode = (StepStartNode) f;
                final StepDescriptor stepDescriptor = startNode.getDescriptor();
                plugins.add(getPluginFromClass(stepDescriptor.clazz));
            }
            if (f instanceof StepAtomNode){
                final StepAtomNode stepAtomNode = (StepAtomNode) f;
                final StepDescriptor stepDescriptor = stepAtomNode.getDescriptor();
                plugins.add(getPluginFromClass(stepDescriptor.clazz));

                if (stepDescriptor.isMetaStep()) {
                    if (stepDescriptor instanceof CoreStep.DescriptorImpl) {
                        coreStepProcess(plugins, f);
                    }
                    if (stepDescriptor instanceof CoreWrapperStep.DescriptorImpl) {
                        coreStepProcess(plugins, f);
                    }
                }
            }
            return true;
        });
    }

    private void coreStepProcess(Set<PluginWrapper> plugins, FlowNode f) {
        final Map<String, Object> arguments = ArgumentsAction.getFilteredArguments(f);
        if (arguments.get("delegate") instanceof UninstantiatedDescribable) {
            final UninstantiatedDescribable describable = (UninstantiatedDescribable) arguments.get("delegate");
            if (describable != null) {
                final Descriptor<?> descriptor = SymbolLookup.get()
                        .findDescriptor(Describable.class, describable.getSymbol());
                plugins.add(getPluginFromClass(descriptor.clazz));
            }
        }
    }
}
