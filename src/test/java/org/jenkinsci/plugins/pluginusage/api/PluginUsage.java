package org.jenkinsci.plugins.pluginusage.api;

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public class PluginUsage {
    private List<PluginProjects> jobsPerPlugin;
    private List<Plugin> otherPlugins;

    public PluginUsage() {
    }

    public PluginUsage(List<PluginProjects> jobsPerPlugin){
        this.jobsPerPlugin = jobsPerPlugin;
    }

    public PluginUsage(List<PluginProjects> jobsPerPlugin, List<Plugin> otherPlugins) {
        this.jobsPerPlugin = jobsPerPlugin;
        this.otherPlugins = otherPlugins;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PluginUsage that = (PluginUsage) o;
        return Objects.equals(jobsPerPlugin, that.jobsPerPlugin) &&
               Objects.equals(otherPlugins, that.otherPlugins);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobsPerPlugin, otherPlugins);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", PluginUsage.class.getSimpleName() + "[", "]")
                .add("jobsPerPlugin=" + jobsPerPlugin)
                .add("otherPlugins=" + otherPlugins)
                .toString();
    }

    public List<PluginProjects> getJobsPerPlugin() {
        return jobsPerPlugin;
    }

    public List<Plugin> getOtherPlugins() {
        return otherPlugins;
    }
}