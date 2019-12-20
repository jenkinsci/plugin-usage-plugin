package org.jenkinsci.plugins.pluginusage.analyzer;

import hudson.PluginWrapper;
import hudson.PluginWrapper.Dependency;

import hudson.model.AbstractProject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import hudson.model.Job;
import jenkins.model.Jenkins;

import org.jenkinsci.plugins.pluginusage.JobsPerPlugin;

public class JobCollector {
	
	private static final Logger LOGGER = Logger.getLogger(JobCollector.class.getName());
	private ArrayList<JobAnalyzer> analysers = new ArrayList<>();
	
	public JobCollector() {
		analysers.add(new BuilderJobAnalyzer());
		analysers.add(new BuildWrapperJobAnalyzer());
		analysers.add(new PropertiesJobAnalyzer());
		analysers.add(new PublisherJobAnalyzer());
		analysers.add(new SCMJobAnalyzer());
		analysers.add(new TriggerJobAnalyzer());
		analysers.add(new StepAnalyser());
		analysers.add(new MavenJobAnalyzer());
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

	public void removeDependencies (List<PluginWrapper> listOfPlugins){
		List<PluginWrapper> pluginsToRemove = new ArrayList<>();
		List<String> isDependency = new ArrayList<>();
		List<PluginWrapper> allPlugins = Jenkins.get().getPluginManager().getPlugins();

		// get a single list of all the plugins that are dependency of another plugin		
		for(PluginWrapper plugin: allPlugins)
		{
			List<Dependency> dependencies = plugin.getDependencies();
			for( Dependency dependency: dependencies )
			{
				isDependency.add(dependency.shortName);
			}
		}

		//LOGGER.info("this jobs are dependency of another: " + isDependency);

		//now I want to remove plugins that are dependency of another plugin
		for(PluginWrapper plugin: listOfPlugins)
		{
			String pluginName = plugin.getShortName();
			//LOGGER.info("checking for "+pluginName);	
			if(isDependency.contains(pluginName))
			{
				LOGGER.info("this plugin: " + pluginName +" is dependency of another");
				PluginWrapper toRemove = Jenkins.get().getPluginManager().getPlugin(pluginName);
				LOGGER.info("about to remove:"+toRemove);
				pluginsToRemove.add(toRemove);
			}
		}

		listOfPlugins.removeAll(pluginsToRemove);

	}


    public List<PluginWrapper> getOtherPlugins() {
		List<PluginWrapper> allPlugins = Jenkins.get().getPluginManager().getPlugins();
		List<PluginWrapper> others = new ArrayList<>(allPlugins);

		for(JobAnalyzer analyser: analysers)
		{
			others.removeAll(analyser.getPlugins());
		}

		removeDependencies(others);

		return others;
    }
}
