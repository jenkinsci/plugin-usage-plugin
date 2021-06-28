package org.jenkinsci.plugins.pluginusage;

import hudson.Extension;
import hudson.model.Api;
import hudson.model.RootAction;
import hudson.security.Permission;
import hudson.security.PermissionGroup;
import hudson.security.PermissionScope;
import jenkins.model.Jenkins;

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

	public String getDisplayName() {
		return "Plugin Usage";
	}

	public String getIconFileName() {
        Jenkins.getInstance().checkPermission(PluginUsageView.PLUGIN_VIEW);
		return "plugin.png";
	}

	public String getUrlName() {
        Jenkins.getInstance().checkPermission(PluginUsageView.PLUGIN_VIEW);
		return "pluginusage";
	}

	public PluginUsageModel getData() {
        Jenkins.getInstance().checkPermission(PluginUsageView.PLUGIN_VIEW);
		PluginUsageModel pluginUsageModel = new PluginUsageModel();
		return pluginUsageModel;
	}

	public Api getApi() {
		Jenkins.getInstance().checkPermission(PluginUsageView.PLUGIN_VIEW);
		return new Api(getData());
	}
}
