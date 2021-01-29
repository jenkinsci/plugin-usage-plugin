package org.jenkinsci.plugins.pluginusage.api;

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public class PluginProjects {
    Plugin plugin;
    List<Project> projects;

    public PluginProjects() {
    }

    public PluginProjects(Plugin plugin, List<Project> projects) {
        this.plugin = plugin;
        this.projects = projects;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PluginProjects that = (PluginProjects) o;
        return Objects.equals(plugin, that.plugin) && Objects.equals(projects, that.projects);
    }

    @Override
    public int hashCode() {
        return Objects.hash(plugin, projects);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", PluginProjects.class.getSimpleName() + "[", "]")
                .add("plugin=" + plugin)
                .add("projects=" + projects)
                .toString();
    }
}
