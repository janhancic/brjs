package org.bladerunnerjs.spec.plugin.bundler.appmeta;

import org.bladerunnerjs.api.App;
import org.bladerunnerjs.api.Aspect;
import org.bladerunnerjs.api.spec.engine.SpecTest;
import org.bladerunnerjs.model.SdkJsLib;
import org.junit.Before;
import org.junit.Test;

public class AppMetadataContentPluginTest extends SpecTest {
	
	private App app;
	private Aspect aspect;
	private StringBuffer requestResponse = new StringBuffer();
	private SdkJsLib bootstrapLib;
	
	@Before
	public void initTestObjects() throws Exception
	{
		given(brjs).automaticallyFindsBundlerPlugins()
			.and(brjs).automaticallyFindsMinifierPlugins()
			.and(brjs).hasBeenCreated();
			app = brjs.app("app1");
			aspect = app.aspect("default");
			bootstrapLib = brjs.sdkLib("br-bootstrap");
	}
	
	@Test
	public void appVersionContentIsIncluded() throws Exception {
		given(brjs).hasVersion("dev");
		when(aspect).requestReceivedInDev("app-meta/version.js", requestResponse);
		then(requestResponse).containsTextOnce( "module.exports.APP_VERSION = 'dev';" );
	}
	
	@Test
	public void bundlePathContentIsIncluded() throws Exception {
		given(brjs).hasVersion("dev");
		when(aspect).requestReceivedInDev("app-meta/version.js", requestResponse);
		then(requestResponse).containsTextOnce( "module.exports.VERSIONED_BUNDLE_PATH = 'v/dev';" );
	}
	
	@Test
	public void appVersionContentIsIncludedInTheCompositeBundle() throws Exception {
		given(aspect).indexPageRequires("appns/Class")
			.and(brjs).hasVersion("dev")
			.and(aspect).hasClass("appns/Class")
			.and(bootstrapLib).hasBeenCreated()
			.and(bootstrapLib).containsFileWithContents("thirdparty-lib.manifest", "js: bootstrap.js\n"+"exports: lib")
			.and(bootstrapLib).containsFileWithContents("bootstrap.js", "// this is bootstrap");
		when(aspect).requestReceivedInDev("js/dev/combined/bundle.js", requestResponse);
		then(requestResponse).containsOrderedTextFragments(
				"// br-bootstrap",
				"module.exports.APP_VERSION = 'dev';",
				"module.exports.VERSIONED_BUNDLE_PATH = 'v/dev';",
				"appns/Class" );
	}
}
