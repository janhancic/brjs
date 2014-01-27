package org.bladerunnerjs.testing.specutility;

import java.io.IOException;
import java.io.StringWriter;

import org.apache.commons.io.FileUtils;
import org.bladerunnerjs.model.Aspect;
import org.bladerunnerjs.model.RequestMode;
import org.bladerunnerjs.model.exception.ConfigException;
import org.bladerunnerjs.model.exception.ModelOperationException;
import org.bladerunnerjs.model.exception.RequirePathException;
import org.bladerunnerjs.testing.specutility.engine.Command;
import org.bladerunnerjs.testing.specutility.engine.CommanderChainer;
import org.bladerunnerjs.testing.specutility.engine.NodeCommander;
import org.bladerunnerjs.testing.specutility.engine.SpecTest;
import org.bladerunnerjs.utility.NoTagHandlerFoundException;
import org.bladerunnerjs.utility.TagPluginUtility;
import org.dom4j.DocumentException;


public class AspectCommander extends NodeCommander<Aspect> {
	private final Aspect aspect;
	public AspectCommander(SpecTest modelTest, Aspect aspect)
	{
		super(modelTest, aspect);
		this.aspect = aspect;
	}
	
	public BundleInfoCommander getBundleInfo() throws Exception {
		return new BundleInfoCommander((aspect.getBundleSet()));
	}
	
	public CommanderChainer indexPageLoadedInDev(final StringBuffer pageResponse, final String locale) throws ConfigException, IOException, ModelOperationException, NoTagHandlerFoundException {
		call(new Command() {
			public void call() throws Exception {
				pageLoaded(pageResponse, locale, RequestMode.Dev);
			}
		});
		
		return commanderChainer;
	}

	public CommanderChainer indexPageLoadedInProd(final StringBuffer pageResponse, final String locale) throws ConfigException, IOException, ModelOperationException, NoTagHandlerFoundException {
		call(new Command() {
			public void call() throws Exception {
				pageLoaded(pageResponse, locale, RequestMode.Prod);
			}
		});
		
		return commanderChainer;
	}
	
	public CommanderChainer retrievesAlias(final String aliasName) throws Exception {
		call(new Command() {
			public void call() throws Exception {
				aspect.aliasesFile().getAlias(aliasName);
			}
		});
		
		return commanderChainer;
	}
	
	private void pageLoaded(StringBuffer pageResponse, String locale, RequestMode opMode) throws ConfigException, IOException, ModelOperationException, NoTagHandlerFoundException, DocumentException, RequirePathException {
		StringWriter writer = new StringWriter();	
		
		TagPluginUtility.filterContent(FileUtils.readFileToString(aspect.file("index.html")), aspect.getBundleSet(), writer, opMode, locale);
		
		pageResponse.append(writer.toString());
	}
}
