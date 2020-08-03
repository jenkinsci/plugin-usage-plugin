package org.jenkinsci.plugins.pluginusage;

import hudson.Extension;
import hudson.Plugin;
import hudson.model.RootAction;
import hudson.security.Permission;
import hudson.security.PermissionGroup;
import hudson.security.PermissionScope;
import jenkins.model.Jenkins;
import org.jvnet.localizer.ResourceBundleHolder;

@Extension
public class PluginUsageView implements RootAction {


	private static final ResourceBundleHolder HOLDER = new ResourceBundleHolder(PluginUsageView.class);

	private static final org.jvnet.localizer.Localizable DESCRIPTION =
			new org.jvnet.localizer.Localizable(HOLDER,"PluginUsageView.ViewPermission");

	private static final PermissionGroup PERMISSIONS = new PermissionGroup(
			PluginUsageView.class, DESCRIPTION);

	private static final Permission VIEW = new Permission(PERMISSIONS, "PluginsUsageView.ViewPermission",
			DESCRIPTION, Jenkins.ADMINISTER, PermissionScope.JENKINS);

	public String getDisplayName() {
		return "Plugin Usage";
	}

	public String getIconFileName() {
		return (Jenkins.get().hasPermission(VIEW)) ? "plugin.png" : null;
	}

	public String getUrlName() {
		return (Jenkins.get().hasPermission(VIEW)) ? "pluginusage" : null;
	}
	
	public PluginUsageModel getData() {
		if (Jenkins.get().hasPermission(VIEW)) {
			PluginUsageModel pluginUsageModel = new PluginUsageModel();
			return pluginUsageModel;
		}
		return null;
	}

}
