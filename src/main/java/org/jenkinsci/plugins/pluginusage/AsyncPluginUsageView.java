package org.jenkinsci.plugins.pluginusage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.PluginWrapper;
import hudson.model.Item;
import hudson.model.RootAction;
import jenkins.model.Jenkins;
import jenkins.util.ProgressiveRendering;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.pluginusage.analyzer.JobCollector;

@Extension
public class AsyncPluginUsageView implements RootAction {

    @Override
    public String getIconFileName() {
        if (Jenkins.get().hasPermission(PluginUsageView.PLUGIN_VIEW)){
            return "plugin.svg";
        }
        return null;
    }

    @Override
    public String getDisplayName() {
        if (Jenkins.get().hasPermission(PluginUsageView.PLUGIN_VIEW)){
            return "Plugin Usage (Async)";
        }
        return null;
    }

    @Override
    public String getUrlName() {
        if (Jenkins.get().hasPermission(PluginUsageView.PLUGIN_VIEW)){
            return "asyncpluginusage";
        }
        return null;
    }

    public ProgressiveRendering progressiveRendering(){
        return new ProgressiveRendering() {

            final Map<String, PluginWrapper> pluginsMap = new HashMap<>();
            final Map<String, Item> jobsMap = new HashMap<>();

            final List<Map.Entry<Item, Set<PluginWrapper>>> newJobs = new ArrayList<>();

            @Override
            protected void compute() {

                JobCollector collector = new JobCollector();
                collector.getJobsPerPlugin(itemSetEntry -> {
                    synchronized (this){
                        itemSetEntry.getValue().forEach(p -> pluginsMap.putIfAbsent(p.getShortName(), p));
                        jobsMap.putIfAbsent(itemSetEntry.getKey().getFullName(), itemSetEntry.getKey());
                        newJobs.add(itemSetEntry);
                    }
                }, this::progress);

            }

            @NonNull
            @Override
            protected synchronized JSON data() {

                final var jobs = newJobs.stream()
                        .map(e -> e.getKey().getFullName())
                        .map(this.jobsMap::get)
                        .collect(Collectors.toList());
                final var plugins = newJobs.stream()
                        .flatMap(e -> e.getValue().stream().map(PluginWrapper::getShortName))
                        .map(this.pluginsMap::get)
                        .collect(Collectors.toList());

                Map<PluginWrapper, Collection<Item>> correlations = new HashMap<>();

                for (Map.Entry<Item, Set<PluginWrapper>> itemEntry : newJobs) {
                    for (PluginWrapper pluginWrapper : itemEntry.getValue()) {
                        correlations.compute(pluginWrapper, (k,v) -> {
                            Collection<Item> items = Objects.requireNonNullElseGet(v, ArrayList::new);
                            items.add(itemEntry.getKey());
                            return items;
                        });
                    }
                }
                newJobs.clear();
                return encode(plugins, jobs, correlations);
            }

            private JSONObject encode(Collection<PluginWrapper> plugins, Collection<Item> items, Map<PluginWrapper, Collection<Item>> correlations){
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("plugins", encodePlugins(plugins));
                jsonObject.put("jobs", encodeItems(items));
                jsonObject.put("correlations", encodeCorrelations(correlations));
                return jsonObject;
            }

            private JSONArray encodeCorrelations(Map<PluginWrapper, Collection<Item>> correlations) {
                JSONArray jsonArray = new JSONArray();
                correlations.forEach((k, v) -> jsonArray.add(encodeCorrelation(k, v)));
                return jsonArray;
            }

            private JSONObject encodeCorrelation(PluginWrapper plugin, Collection<Item> items) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("plugin", plugin.getShortName());
                jsonObject.put("jobs", items.stream().map(Item::getFullName).collect(Collectors.toList()));
                return jsonObject;
            }

            private JSONObject encodePlugins(Collection<PluginWrapper> plugins){
                JSONObject jsonObject = new JSONObject();
                plugins.forEach(plugin -> jsonObject.put(plugin.getShortName(), encodePlugin(plugin)));
                return jsonObject;
            }

            private JSONObject encodePlugin(PluginWrapper pluginWrapper){
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("displayName", pluginWrapper.getDisplayName());
                jsonObject.put("url", pluginWrapper.getUrl());
                jsonObject.put("version", pluginWrapper.getVersion());
                jsonObject.put("hasDependants", pluginWrapper.hasDependents());
                return jsonObject;
            }
            private JSONObject encodeItems(Collection<Item> items){
                JSONObject jsonObject = new JSONObject();
                items.forEach(item -> jsonObject.put(item.getFullName(), encodeItem(item)));
                return jsonObject;
            }
            private JSONObject encodeItem(Item item){
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("fullDisplayName", item.getFullDisplayName());
                jsonObject.put("url", item.getUrl());
                return jsonObject;
            }
        };
    }
}
