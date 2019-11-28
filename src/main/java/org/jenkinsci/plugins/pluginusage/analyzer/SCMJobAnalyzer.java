package org.jenkinsci.plugins.pluginusage.analyzer;

import hudson.DescriptorExtensionList;
import hudson.PluginWrapper;
import hudson.model.Descriptor;
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.scm.SCMDescriptor;
import hudson.scm.SCM;
import hudson.tasks.BuildWrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.jenkinsci.plugins.pluginusage.JobsPerPlugin;

public class SCMJobAnalyzer extends JobAnalyzer{
	
	public SCMJobAnalyzer() {
		DescriptorExtensionList<SCM, SCMDescriptor<?>> all = SCM.all();
		for(SCMDescriptor<?> b: all)
		{
			PluginWrapper usedPlugin = getUsedPlugin(b.clazz);
			plugins.add(usedPlugin);
		}
	}

	@Override
	protected void doJobAnalyze(Job item, Map<PluginWrapper, JobsPerPlugin> mapJobsPerPlugin)
	{		
		super.doJobAnalyze(null, mapJobsPerPlugin);
		if(item instanceof AbstractProject){
			PluginWrapper scmPlugin = getUsedPlugin(((AbstractProject)item).getScm().getDescriptor().clazz);
			if(scmPlugin!=null)
			{
				JobsPerPlugin jobsPerPlugin = mapJobsPerPlugin.get(scmPlugin);
				if(jobsPerPlugin!=null)
				{
					jobsPerPlugin.addProject(item);
				}
				else
				{
					JobsPerPlugin jobsPerPlugin2 = new JobsPerPlugin(scmPlugin);
					jobsPerPlugin2.addProject(item);
					mapJobsPerPlugin.put(scmPlugin, jobsPerPlugin2);
				}
			}
		}
	}

}
