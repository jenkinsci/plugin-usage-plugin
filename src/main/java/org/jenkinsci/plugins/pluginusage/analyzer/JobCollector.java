package org.jenkinsci.plugins.pluginusage.analyzer;

import hudson.PluginWrapper;
import hudson.PluginWrapper.Dependency;

import hudson.model.AbstractProject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.logging.Logger;

import hudson.model.Job;
import jenkins.model.Jenkins;

import org.jenkinsci.plugins.pluginusage.JobsPerPlugin;

public class JobCollector {
	
	private static final Logger LOGGER = Logger.getLogger(JobCollector.class.getName());
	private ArrayList<JobAnalyzer> analysers = new ArrayList<>();
	private List<PluginWrapper> allPlugins;
	private List<String> isDependency;

	public JobCollector() {
		analysers.add(new BuilderJobAnalyzer());
		analysers.add(new BuildWrapperJobAnalyzer());
		analysers.add(new PropertiesJobAnalyzer());
		analysers.add(new PublisherJobAnalyzer());
		analysers.add(new SCMJobAnalyzer());
		analysers.add(new TriggerJobAnalyzer());
		analysers.add(new StepAnalyser());
		analysers.add(new MavenJobAnalyzer());

		// get a single list of all the plugins that are dependency of another plugin
		allPlugins = Jenkins.get().getPluginManager().getPlugins();
		isDependency = new ArrayList<>();
		for(PluginWrapper plugin: allPlugins)
		{
			List<Dependency> dependencies = plugin.getDependencies();
			for( Dependency dependency: dependencies )
			{
				isDependency.add(dependency.shortName);
			}
		}
	}

	public Map<PluginWrapper, JobsPerPlugin> getJobsPerPlugin()
	{
		Map<PluginWrapper, JobsPerPlugin> mapJobsPerPlugin = new HashMap<>();

		// bootstrap map with all job related plugins
		for(JobAnalyzer analyser: analysers)
		{
			analyser.doJobAnalyze(null, mapJobsPerPlugin);
		}

		List<Job> allItems = Jenkins.get().getAllItems(Job.class);
		for(Job item: allItems)
		{
			for(JobAnalyzer analyser: analysers)
			{
				try{
					analyser.doJobAnalyze(item, mapJobsPerPlugin);
				} catch(Exception e){
					LOGGER.warning("Exception catched: " + e );
				}
			}
		}

		return mapJobsPerPlugin;
	}
	
	public int getNumberOfJobs() {
		List<AbstractProject> allItems = Jenkins.get().getAllItems(AbstractProject.class);
		return allItems.size();	
	}

    public List<PluginWrapper> getOtherPlugins() {
		List<PluginWrapper> others = new ArrayList<>(allPlugins);

		for(JobAnalyzer analyser: analysers)
		{
			others.removeAll(analyser.getPlugins());
		}

		return others;
    }

	public void removeDependencies (Map<PluginWrapper, JobsPerPlugin> listOfPlugins){
		List<PluginWrapper> pluginsToRemove = new ArrayList<>();

		//now I want to remove plugins that are dependency of another plugin
		for(Map.Entry<PluginWrapper, JobsPerPlugin> plugin: listOfPlugins.entrySet())
		{
			String pluginName = plugin.getKey().getShortName();
			if(isDependency.contains(pluginName))
			{
				PluginWrapper toRemove = Jenkins.get().getPluginManager().getPlugin(pluginName);
				//LOGGER.info("about to remove:"+toRemove);
				pluginsToRemove.add(toRemove);
			}
		}

		//listOfPlugins.removeAll(pluginsToRemove);
		Iterator<Map.Entry<PluginWrapper, JobsPerPlugin>> itr = listOfPlugins.entrySet().iterator();
        while(itr.hasNext())
        {
             Map.Entry<PluginWrapper, JobsPerPlugin> entry = itr.next();
             if( pluginsToRemove.contains( entry.getKey() ) )
             {
				itr.remove();
             }
        }
	}

    //remove plugins that are dependency of another plugin
	public void removeDependencies (List<PluginWrapper> listOfPlugins){
		List<PluginWrapper> pluginsToRemove = new ArrayList<>();

		//now I want to remove plugins that are dependency of another plugin
		for(PluginWrapper plugin: listOfPlugins)
		{
			String pluginName = plugin.getShortName();
			if(isDependency.contains(pluginName))
			{
				PluginWrapper toRemove = Jenkins.get().getPluginManager().getPlugin(pluginName);
				//LOGGER.info("about to remove:"+toRemove);
				pluginsToRemove.add(toRemove);
			}
		}

		listOfPlugins.removeAll(pluginsToRemove);
	}
}
