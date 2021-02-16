package org.jenkinsci.plugins.pluginusage.api;

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public class PluginUsage {
    private List<PluginProjects> jobsPerPlugin;

    public PluginUsage() {
    }

    public PluginUsage(List<PluginProjects> jobsPerPlugin) {
        this.jobsPerPlugin = jobsPerPlugin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PluginUsage that = (PluginUsage) o;
        return Objects.equals(jobsPerPlugin, that.jobsPerPlugin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobsPerPlugin);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", PluginUsage.class.getSimpleName() + "[", "]")
                .add("jobsPerPlugin=" + jobsPerPlugin)
                .toString();
    }
}
