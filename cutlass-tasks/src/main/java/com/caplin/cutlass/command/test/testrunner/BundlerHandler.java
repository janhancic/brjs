package com.caplin.cutlass.command.test.testrunner;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bladerunnerjs.model.App;
import org.bladerunnerjs.model.BRJS;
import org.bladerunnerjs.model.BundlableNode;
import org.bladerunnerjs.model.ContentPluginOutput;
import org.bladerunnerjs.model.StaticContentPluginOutput;
import org.bladerunnerjs.model.exception.request.ContentProcessingException;
import org.bladerunnerjs.model.exception.request.MalformedRequestException;
import org.bladerunnerjs.model.exception.request.ResourceNotFoundException;


public class BundlerHandler
{	
	private App app;
	protected Map<String,String> legacyBundlerHandlerPaths = new HashMap<>();
	protected Map<String,String> logicalBundlerHandlerPaths = new HashMap<>();
	private BRJS brjs;
	
	public BundlerHandler(BRJS brjs, App app)
	{
		// legacy paths - these are matched against the last part of the bundle path - e.g. js/js.bundle would match js.bundle
		legacyBundlerHandlerPaths.put("js.bundle", "js/dev/combined/bundle.js");
		legacyBundlerHandlerPaths.put("css.bundle", "css/common/bundle.css");
		legacyBundlerHandlerPaths.put("i18n.bundle", "i18n/en_GB.js");
		legacyBundlerHandlerPaths.put("(.*)_i18n.bundle", "i18n/$1.js"); // .* is a bad regex for a locale but since this is simply for legacy support we can get away with it
		legacyBundlerHandlerPaths.put("xml.bundle", "xml/bundle.xml");
		legacyBundlerHandlerPaths.put("html.bundle", "html/bundle.html");
		
		// logical/utility paths
		logicalBundlerHandlerPaths.put("bundle.js", "js/dev/combined/bundle.js");
		logicalBundlerHandlerPaths.put("bundle.css", "css/common/bundle.css");
		logicalBundlerHandlerPaths.put("bundle.i18n", "i18n/en_GB.js");
		logicalBundlerHandlerPaths.put("bundle.xml", "bundle.xml");
		logicalBundlerHandlerPaths.put("bundle.html", "bundle.html");
		
		this.brjs = brjs;
		this.app = app;
	}

	
	public void createBundleFile(File bundleFile, String bundlePath, String version) throws IOException, MalformedRequestException, ResourceNotFoundException, ContentProcessingException
	{
		if (bundlePath.contains("\\"))
		{
			throw new IllegalArgumentException("Invalid bundlePath - it should not contain '\', only '/' as a seperator");
		}
		ContentPluginOutput output= createBundleOutputStream(bundleFile);
		String modelRequestPath = getModelRequestPath(bundlePath);
		handleBundleRequest(bundleFile, modelRequestPath, output, version);
		Reader reader = output.getReader();
		if(reader != null){
			IOUtils.copy(reader, output.getWriter());
			output.getWriter().flush();
		}
		
		
	}

	private String getModelRequestPath(String bundlerPath)
	{
		String brjsRequestPath = lookupRequestPathFromKnownBundlePaths(bundlerPath);
		
		if (brjsRequestPath == null)
		{
			brjsRequestPath = bundlerPath;
		}
		
		return brjsRequestPath;
	}
	
	private String lookupRequestPathFromKnownBundlePaths(String bundlerFilePath)
	{
		String legacyBundleKey = (bundlerFilePath.contains("/")) ? StringUtils.substringAfterLast(bundlerFilePath, "/") : bundlerFilePath;
		for (String keyRegex : legacyBundlerHandlerPaths.keySet())
		{
			if (legacyBundleKey.matches(keyRegex))
			{
				String bundlerConvertedPath = legacyBundlerHandlerPaths.get(keyRegex);
				String bundlerPath = legacyBundleKey.replaceAll(keyRegex, bundlerConvertedPath);
				return bundlerPath;
			}
		}
		
		if (logicalBundlerHandlerPaths.containsKey(bundlerFilePath))
		{
			return logicalBundlerHandlerPaths.get(bundlerFilePath);
		}
		
		return null;
	}

	private void handleBundleRequest(File bundleFile, String brjsRequestPath, ContentPluginOutput outputStream, String version) throws MalformedRequestException, ResourceNotFoundException, ContentProcessingException 
	{
		BundlableNode bundlableNode = brjs.locateAncestorNodeOfClass(bundleFile, BundlableNode.class);
		if (bundlableNode == null)
		{
			throw new ResourceNotFoundException("Unable to calculate bundlable node for the bundle file: " + bundleFile.getAbsolutePath());
		}
		
		bundlableNode.handleLogicalRequest(brjsRequestPath, outputStream, new NoTestModuleBundleSourceFilter(), version);
	}
	
	private ContentPluginOutput createBundleOutputStream(File bundlerFile) throws IOException
	{
		bundlerFile.getParentFile().mkdirs();
		
		return new StaticContentPluginOutput(app, bundlerFile);
	}
	
	
}
