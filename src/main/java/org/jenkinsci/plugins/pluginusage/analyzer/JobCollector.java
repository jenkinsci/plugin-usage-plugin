package org.jenkinsci.plugins.pluginusage.analyzer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import hudson.PluginWrapper;
import hudson.model.Item;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.pluginusage.JobsPerPlugin;

public class JobCollector {

    private static final Logger LOGGER = Logger.getLogger(org.jenkinsci.plugins.pluginusage.analyzer.JobCollector.class.getName());

    public Map<PluginWrapper, JobsPerPlugin> getJobsPerPlugin()
    {
        Map<PluginWrapper, JobsPerPlugin> mapJobsPerPlugin = new HashMap<>();

        List<AbstractProjectAnalyzer> analyzers =
                Arrays.asList(
                        new ProjectAnalyzer(),
                        new MavenProjectAnalyzer(),
                        new PipelineProjectAnalyzer(),
                        new MatrixProjectAnalyzer(),
                        new ComputedFolderAnalyzer(),
                        new PipelineLastBuildAnalyzer());

        // bootstrap map with all job related plugins
        for(AbstractProjectAnalyzer analyzer: analyzers)
        {
            try{
                for(PluginWrapper plugin: analyzer.getPlugins()){
                    if (plugin != null){
                        if (mapJobsPerPlugin.get(plugin) == null) {
                            mapJobsPerPlugin.put(plugin, new JobsPerPlugin(plugin));
                        }
                    }
                }
            } catch(Exception e){
                LOGGER.log(Level.WARNING, "Exception caught: " + e , e);
            }
        }

        final List<Item> items = Jenkins.get().getAllItems()
                .stream()
                .filter(job -> !analyzers
                        .stream()
                        .map(analyzer -> analyzer.ignoreJob(job))
                        .reduce(false, (a, b) -> a || b))
                .collect(Collectors.toList());
        for(Item item: items)
        {
            for(AbstractProjectAnalyzer analyzer: analyzers)
            {
                try{
                    for(PluginWrapper plugin: analyzer.getPluginsFromItem(item)){
                        addItem(mapJobsPerPlugin, item, plugin);
                    }
                } catch(Exception e){
                    LOGGER.log(Level.WARNING, "Exception caught in job " + item.getFullName() + ": " + e , e);
                } catch (Throwable e){
                    LOGGER.log(Level.SEVERE,"Exception caught in job " + item.getFullName() + ": " + e, e);
                }
            }
        }

        return mapJobsPerPlugin;
    }

    protected void addItem(Map<PluginWrapper, JobsPerPlugin> mapJobsPerPlugin, Item item, PluginWrapper usedPlugin) {
        if (usedPlugin != null) {
            JobsPerPlugin jobsPerPlugin = mapJobsPerPlugin.get(usedPlugin);
            if (jobsPerPlugin != null) {
                jobsPerPlugin.addProject(item);
            } else {
                JobsPerPlugin jobsPerPlugin2 = new JobsPerPlugin(usedPlugin);
                jobsPerPlugin2.addProject(item);
                mapJobsPerPlugin.put(usedPlugin, jobsPerPlugin2);
            }
        }
    }
}
