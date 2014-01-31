package org.bladerunnerjs.plugin.utility;

import org.bladerunnerjs.plugin.AssetLocationPlugin;
import org.bladerunnerjs.plugin.ContentPlugin;
import org.bladerunnerjs.plugin.Plugin;
import org.bladerunnerjs.plugin.TagHandlerPlugin;
import org.bladerunnerjs.plugin.plugins.brjsconformant.BRJSConformantAssetLocationPlugin;
import org.bladerunnerjs.plugin.plugins.bundlers.aliasing.AliasingContentPlugin;
import org.bladerunnerjs.plugin.plugins.bundlers.brjsthirdparty.BRJSThirdpartyContentPlugin;
import org.bladerunnerjs.plugin.plugins.bundlers.brjsthirdparty.BRJSThirdpartyTagHandlerPlugin;
import org.bladerunnerjs.plugin.plugins.bundlers.namespacedjs.NamespacedJsContentPlugin;
import org.bladerunnerjs.plugin.plugins.bundlers.namespacedjs.NamespacedJsTagHandlerPlugin;
import org.bladerunnerjs.plugin.proxy.VirtualProxyPlugin;

public class PluginPriorityCalculator
{

	static int priority(Plugin plugin)
	{
		plugin = (plugin instanceof VirtualProxyPlugin) ? ((VirtualProxyPlugin) plugin).getUnderlyingPlugin() : plugin;
		
		if (plugin instanceof AssetLocationPlugin)
		{
			return priority( (AssetLocationPlugin) plugin);
		}
		if (plugin instanceof ContentPlugin)
		{
			return priority( (ContentPlugin) plugin);
		}
		if (plugin instanceof TagHandlerPlugin)
		{
			return priority( (TagHandlerPlugin) plugin);
		}
		
		return 0;
	}
	
	
	
	private static int priority(AssetLocationPlugin plugin)
	{
		if (plugin instanceof BRJSConformantAssetLocationPlugin)
		{
			return -10;
		}
		return 0;
	}
	
	private static int priority(ContentPlugin plugin)
	{
		if (plugin instanceof AliasingContentPlugin)
		{
			return 100;
		}
		if (plugin instanceof BRJSThirdpartyContentPlugin)
		{
			return 50;
		}
		if (plugin instanceof NamespacedJsContentPlugin)
		{
			return -20;
		}
		return 0;
	}
	
	private static int priority(TagHandlerPlugin plugin)
	{
		if (plugin instanceof BRJSThirdpartyTagHandlerPlugin)
		{
			return 1000;
		}
		if (plugin instanceof NamespacedJsTagHandlerPlugin)
		{
			return 20;
		}
		return 0;
	}
	
}
