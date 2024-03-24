package org.jenkinsci.plugins.pluginusage;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import hudson.BulkChange;
import hudson.PluginWrapper;
import hudson.XmlFile;
import hudson.model.Item;
import hudson.model.Saveable;
import hudson.model.TopLevelItem;
import hudson.model.listeners.SaveableListener;
import jenkins.model.Jenkins;

public class PersistedModel implements Saveable {

    private static final Logger LOGGER = Logger.getLogger(PersistedModel.class.getName());

    private String timestamp;
    private Map<String, Set<String>> jobsPerPluginMap;

    @Override
    public synchronized void save() {
        if (BulkChange.contains(this)) return;
        try {
            getConfigFile().write(this);
            SaveableListener.fireOnChange(this, getConfigFile());
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to save " + getConfigFile(), e);
        }
    }

    public synchronized void load() {
        XmlFile file = getConfigFile();
        if (!file.exists())
            return;

        try {
            file.unmarshal(this);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to load " + file, e);
        }
    }

    protected XmlFile getConfigFile() {
        return new XmlFile(new File(Jenkins.get().getRootDir(),"plugin-usage.xml"));
    }

    public Map<PluginWrapper, JobsPerPlugin> getJobsPerPlugin() {
        if (jobsPerPluginMap == null){
            return Map.of();
        }

        return jobsPerPluginMap.entrySet()
                .stream()
                .filter(e -> Jenkins.get().getPluginManager().getPlugin(e.getKey()) != null)
                .collect(Collectors.toMap(
                        e -> Jenkins.get().getPluginManager().getPlugin(e.getKey()),
                        e -> {
                            final var plugin = Jenkins.get().getPluginManager().getPlugin(e.getKey());
                            final var jobsPerPlugin = new JobsPerPlugin(plugin);
                            e.getValue()
                                    .stream()
                                    .map(Jenkins.get()::getItem)
                                    .filter(Objects::nonNull)
                                    .forEach(jobsPerPlugin::addProject);
                            return jobsPerPlugin;
                        }
                ));
    }

    public void setJobsPerPlugin(Map<PluginWrapper, JobsPerPlugin> jobsPerPlugin) {
        this.jobsPerPluginMap =
                jobsPerPlugin.entrySet().stream()
                        .collect(Collectors.toMap(
                                e -> e.getKey().getShortName(),
                                e -> e.getValue()
                                        .getProjects()
                                        .stream()
                                        .map(Item::getFullDisplayName)
                                        .collect(Collectors.toSet())
                        ));
    }

    public Instant getTimestamp() {
        if (timestamp != null){
            return Instant.parse(timestamp);
        }
        return null;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp.toString();
    }
}
