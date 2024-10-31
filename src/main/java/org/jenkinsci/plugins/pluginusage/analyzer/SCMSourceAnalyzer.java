package org.jenkinsci.plugins.pluginusage.analyzer;

import java.util.HashSet;
import java.util.Set;

import hudson.PluginWrapper;
import hudson.model.Item;
import jenkins.model.Jenkins;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceOwner;

public class SCMSourceAnalyzer extends AbstractProjectAnalyzer {

    private final boolean hasPlugin;

    public SCMSourceAnalyzer() {
        hasPlugin = Jenkins.get().getPlugin("scm-api") != null;
    }

    @Override
    protected Set<PluginWrapper> getPluginsFromBuilders(Item item) {
        Set<PluginWrapper> plugins = new HashSet<>();

        if (!hasPlugin){
            return plugins;
        }

        if (item instanceof SCMSourceOwner ){
            final var scmSourceOwner = (SCMSourceOwner) item;
            final var scmSources = scmSourceOwner.getSCMSources();
            for (SCMSource scmSource : scmSources) {
                plugins.add(getPluginFromClass(scmSource.getClass()));
            }
        }

        return plugins;
    }
}
