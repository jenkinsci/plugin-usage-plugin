package org.jenkinsci.plugins.pluginusage.api;

import java.util.Objects;
import java.util.StringJoiner;

public class Project {
    String fullName;

    public Project() {
    }

    public Project(String fullName) {
        this.fullName = fullName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return Objects.equals(fullName, project.fullName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fullName);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Project.class.getSimpleName() + "[", "]")
                .add("fullName='" + fullName + "'")
                .toString();
    }
}
