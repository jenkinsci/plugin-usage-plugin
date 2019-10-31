package org.jenkinsci.plugins.pluginusage.analyzer;

import hudson.DescriptorExtensionList;
import hudson.PluginWrapper;
import hudson.model.Descriptor;
import hudson.model.AbstractProject;
import hudson.model.Project;
import hudson.tasks.Builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jenkins.model.Jenkins;

import org.jenkinsci.plugins.pluginusage.JobsPerPlugin;

public class BuilderJobAnalyzer extends JobAnalyzer{
	
	public BuilderJobAnalyzer() {
		DescriptorExtensionList<Builder,Descriptor<Builder>> all = Builder.all();
		for(Descriptor<Builder> b: all)
		{
			PluginWrapper usedPlugin = getUsedPlugin(b.clazz);
			plugins.add(usedPlugin);
		}
	}

	@Override
	protected void doJobAnalyze(AbstractProject item, HashMap<PluginWrapper, JobsPerPlugin> mapJobsPerPlugin)
	{	
		super.doJobAnalyze(null, mapJobsPerPlugin);
                if (item instanceof Project)
                {
                    List<Builder> builders = ((Project)item).getBuilders();
                    for(Builder builder: builders)
                    {
                        PluginWrapper usedPlugin = getUsedPlugin(builder.getDescriptor().clazz);
                        if(usedPlugin!=null)
                        {
                            JobsPerPlugin jobsPerPlugin = mapJobsPerPlugin.get(usedPlugin);
                            if(jobsPerPlugin!=null)
                            {
                                    jobsPerPlugin.addProject(item);
                            }
                            else
                            {
                                    JobsPerPlugin jobsPerPlugin2 = new JobsPerPlugin(usedPlugin);
                                    jobsPerPlugin2.addProject(item);
                                    mapJobsPerPlugin.put(usedPlugin, jobsPerPlugin2);
                            }
                        }
                    }
                }
	}

}
