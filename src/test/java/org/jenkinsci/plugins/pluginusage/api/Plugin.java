package org.jenkinsci.plugins.pluginusage.api;

import java.util.Objects;
import java.util.StringJoiner;

public class Plugin {
    String shortName;
    String version;

    public Plugin() {
    }

    public Plugin(String shortName, String version) {
        this.shortName = shortName;
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Plugin plugin = (Plugin) o;
        return Objects.equals(shortName, plugin.shortName) && Objects.equals(version, plugin.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shortName, version);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Plugin.class.getSimpleName() + "[", "]")
                .add("shortName='" + shortName + "'")
                .add("version='" + version + "'")
                .toString();
    }
}
