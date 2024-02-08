package org.jenkinsci.plugins.pluginusage;

import java.time.Instant;
import java.util.Map;

import hudson.Extension;
import hudson.PluginWrapper;
import hudson.model.AsyncPeriodicWork;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.pluginusage.analyzer.JobCollector;

@Extension
public class PluginUsageAsyncPeriodicWork extends AsyncPeriodicWork {

    public PluginUsageAsyncPeriodicWork() {
        super("plugin-usage-pre-calculate-worker");
    }

    @Override
    public long getRecurrencePeriod() {
        return DAY;
    }

    @Override
    protected void execute(TaskListener listener) {
        final JobCollector jobCollector = new JobCollector();
        final Map<PluginWrapper, JobsPerPlugin> jobsPerPlugin = jobCollector.getJobsPerPlugin();

        PersistedModel persistedModel = new PersistedModel();
        persistedModel.setJobsPerPlugin(jobsPerPlugin);
        persistedModel.setTimestamp(Instant.now());
        persistedModel.save();
    }
}
