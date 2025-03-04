package org.jenkinsci.plugins.pluginusage;

import hudson.Extension;
import hudson.model.Api;
import hudson.model.AsyncPeriodicWork;
import hudson.model.RootAction;
import hudson.security.Permission;
import hudson.security.PermissionGroup;
import hudson.security.PermissionScope;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.StaplerResponse2;

@Extension
public class PluginUsageView implements RootAction{
    /**
     * Permission group for PluginUsageView related permissions.
     */
    public static final PermissionGroup PERMISSIONS =
        new PermissionGroup(PluginUsageView.class, Messages._PluginUsageView_PermissionGroup());
	/**
     * Permission to get the plugin view usage.
     */
    public static final Permission PLUGIN_VIEW = new Permission(PERMISSIONS,
        "PluginView", Messages._PluginUsageView_PluginViewPermission_Description(), Jenkins.ADMINISTER, PermissionScope.JENKINS);

	@Override
	public String getDisplayName() {
		if (Jenkins.get().hasPermission(PluginUsageView.PLUGIN_VIEW)){
			return "Plugin Usage";
		}
		return null;
	}

	@Override
	public String getIconFileName() {
		if (Jenkins.get().hasPermission(PluginUsageView.PLUGIN_VIEW)){
			return "plugin.svg";
		}
		return null;
	}

	@Override
	public String getUrlName() {
		if (Jenkins.get().hasPermission(PluginUsageView.PLUGIN_VIEW)){
			return "pluginusage";
		}
		return null;
	}

	public void doUpdate(StaplerRequest2 req, StaplerResponse2 res) throws Exception {
		if (Jenkins.get().hasPermission(PluginUsageView.PLUGIN_VIEW)){
			AsyncPeriodicWork.all().getInstance(PluginUsageAsyncPeriodicWork.class).doRun();
		}
		res.forwardToPreviousPage(req);
	}

	public PluginUsageModel getData() {
		Jenkins.get().checkPermission(PluginUsageView.PLUGIN_VIEW);
		PluginUsageModel pluginUsageModel = new PluginUsageModel();
		return pluginUsageModel;
	}

	public Api getApi() {
		Jenkins.get().checkPermission(PluginUsageView.PLUGIN_VIEW);
		return new Api(getData());
	}
}
