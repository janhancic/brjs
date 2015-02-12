package org.bladerunnerjs.spec.aliasing;

import org.bladerunnerjs.api.App;
import org.bladerunnerjs.api.AppConf;
import org.bladerunnerjs.api.Aspect;
import org.bladerunnerjs.api.Blade;
import org.bladerunnerjs.api.Bladeset;
import org.bladerunnerjs.api.JsLib;
import org.bladerunnerjs.api.model.exception.UnresolvableRequirePathException;
import org.bladerunnerjs.api.spec.engine.SpecTest;
import org.bladerunnerjs.model.BladeWorkbench;
import org.bladerunnerjs.plugin.bundlers.aliasing.AliasDefinitionsFile;
import org.bladerunnerjs.plugin.bundlers.aliasing.AliasNameIsTheSameAsTheClassException;
import org.bladerunnerjs.plugin.bundlers.aliasing.AliasesFile;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;


public class AliasBundlingTest extends SpecTest {
	private App app;
	private AppConf appConf;
	private Aspect aspect;
	private Bladeset bladeset;
	private Blade blade;
	private JsLib brLib, otherBrLib;
	private AliasesFile aspectAliasesFile;
	private AliasDefinitionsFile brLibAliasDefinitionsFile, bladeAliasDefinitionsFile;
	private StringBuffer response = new StringBuffer();
	private BladeWorkbench workbench;
	private AliasesFile worbenchAliasesFile;
	private Bladeset defaultBladeset;
	private Blade bladeInDefaultBladeset;
	
	@Before
	public void initTestObjects() throws Exception
	{
		given(brjs).automaticallyFindsBundlerPlugins()
			.and(brjs).automaticallyFindsMinifierPlugins()
			.and(brjs).hasBeenCreated();
			app = brjs.app("app1");
			appConf = app.appConf();
			aspect = app.aspect("default");
			aspectAliasesFile = aspect.aliasesFile();
			bladeset = app.bladeset("bs");
			blade = bladeset.blade("b1");
			bladeAliasDefinitionsFile = blade.aliasDefinitionsFile("src");
			workbench = blade.workbench();
			worbenchAliasesFile = workbench.aliasesFile();
			brLib = app.jsLib("br");
			brLibAliasDefinitionsFile = brLib.aliasDefinitionsFile("resources");
			otherBrLib = brjs.sdkLib("otherBrLib");
			defaultBladeset = app.defaultBladeset();
			bladeInDefaultBladeset = defaultBladeset.blade("b1");
	}
	
	// SDK AliasDefinitions
	@Test
	public void sdkLibAliasDefinitionsShouldNotGetScannedForDependenciesIfTheClassesAreNotReferencedViaIndexPage() throws Exception {
		given(brLib).hasClass("br/Class1")
			.and(brLibAliasDefinitionsFile).hasAlias("br.test-class", "otherBrLib.TestClass")
			.and(otherBrLib).classFileHasContent("otherBrLib.TestClass", "I should not be bundled")
			.and(aspect).indexPageRefersTo("br.Class1");
		when(aspect).requestReceivedInDev("js/dev/combined/bundle.js", response);
		then(response).doesNotContainText("otherBrLib.TestClass")
			.and(response).doesNotContainText("I should not be bundled");
	}
	
	@Test
	public void sdkLibAliasDefinitionsShouldNotGetScannedForDependenciesIfTheClassesAreNotReferencedViaAspectClass() throws Exception {
		given(brLib).hasClass("br/Class1")
			.and(brLibAliasDefinitionsFile).hasAlias("br.alias-class", "otherBrLib.TestClass")
			.and(otherBrLib).classFileHasContent("otherBrLib.TestClass", "I should not be bundled")
			.and(aspect).hasCommonJsPackageStyle()
			.and(aspect).classFileHasContent("appns.Class1", "'br.alias-class'")
			.and(aspect).indexPageRefersTo("br.Class1", "appns.Class1");
		when(aspect).requestReceivedInDev("js/dev/combined/bundle.js", response);
		then(response).doesNotContainText("otherBrLib.TestClass")
			.and(response).doesNotContainText("I should not be bundled");
	}
	
	@Test
	public void sdkLibAliasDefinitionsReferencesAreBundledIfTheyAreReferencedViaIndexPage() throws Exception {
		given(brLib).hasClasses("br/Class1", "br/Class2")
			.and(brLibAliasDefinitionsFile).hasAlias("br.alias", "br.Class2")
			.and(aspect).indexPageHasAliasReferences("br.alias");
		when(aspect).requestReceivedInDev("js/dev/combined/bundle.js", response);
		then(response).containsText("br/Class2");
	}
	
	@Test
	public void aliasClassesReferencedByANamespacedJsSourceModuleAreIncludedInTheBundle() throws Exception {
		given(aspect).hasNamespacedJsPackageStyle()
			.and(aspect).hasClasses("appns.Class", "appns.Class1", "appns.Class2", "appns.Class3")
			.and(aspectAliasesFile).hasAlias("br.pre-export-define-time-alias", "appns.Class1")
			.and(aspectAliasesFile).hasAlias("br.post-export-define-time-alias", "appns.Class2")
			.and(aspectAliasesFile).hasAlias("br.use-time-alias", "appns.Class3")
			.and(aspect).classFileHasContent("appns.Class", "'br.pre-export-define-time-alias' function() {'br.use-time-alias'} module.exports = X; 'br.post-export-define-time-alias'")
			.and(aspect).indexPageRefersTo("appns.Class");
		when(aspect).requestReceivedInDev("js/dev/combined/bundle.js", response);
		then(response).containsNamespacedJsClasses("appns.Class1", "appns.Class2", "appns.Class3");
	}
	
	@Test
	public void aliasClassesReferencedByACommonJsSourceModuleAreIncludedInTheBundle() throws Exception {
		given(brLib).hasClasses("br/Class1", "br/Class2")
			.and(brLibAliasDefinitionsFile).hasAlias("br.alias", "br.Class2")
			.and(aspect).hasCommonJsPackageStyle()
			.and(aspect).classFileHasContent("Class1", "require('alias!br.alias')")
			.and(aspect).indexPageRefersTo("appns.Class1");
		when(aspect).requestReceivedInDev("js/dev/combined/bundle.js", response);
		then(response).containsText("br/Class2");
	}
	
	@Test
	public void serviceClassesReferencedByACommonJsSourceModuleAreIncludedInTheBundle() throws Exception {
		given(brLib).hasClasses("br/ServiceRegistry", "br/Class2")
			.and(brLibAliasDefinitionsFile).hasAlias("br.service", "br.Class2")
			.and(aspect).hasCommonJsPackageStyle()
			.and(aspect).classRequires("Class1", "service!br.service")
			.and(aspect).indexPageRefersTo("appns.Class1");
		when(aspect).requestReceivedInDev("js/dev/combined/bundle.js", response);
		then(response).containsText("br/Class2");
	}
	
	@Test
	public void incompleteServiceClassesReferencedByACommonJsSourceModuleCauseTheAliasToBeIncludedInTheBundle() throws Exception {
		given(brLib).hasClasses("br/ServiceRegistry", "br/Core", "br/UnknownClass", "br/AliasInterfaceError", "br/Class2", "br/Interface")
			.and(brLibAliasDefinitionsFile).hasIncompleteAlias("br.service", "br/Interface")
			.and(aspect).hasCommonJsPackageStyle()
			.and(aspect).classRequires("Class1", "service!br.service")
			.and(aspect).indexPageRefersTo("appns.Class1");
		when(aspect).requestReceivedInDev("js/dev/combined/bundle.js", response);
		then(response).containsText("define('alias!br.service',")
			.and(response).doesNotContainText("br/Class2");
	}
	
	@Test // test exception isnt thrown for services - services can be defined and configure at run time, which differs from aliases
	public void anExceptionIsntThrownIfAServiceClassesReferencedByACommonJsSourceModuleDoesntExist() throws Exception {
		given(brLib).hasClasses("br/Class1", "br/Class2")
    		.and(aspect).hasCommonJsPackageStyle()
    		.and(aspect).classRequires("Class1", "service!br.service")
    		.and(aspect).indexPageRefersTo("appns.Class1");
		when(aspect).requestReceivedInDev("js/dev/combined/bundle.js", response);
		then(exceptions).verifyNoOutstandingExceptions();
	}
	// -----------------------------------
	
	@Test
	public void weBundleAClassWhoseAliasIsReferredToInTheIndexPage() throws Exception {
		given(aspect).hasClass("appns/Class1")
			.and(aspectAliasesFile).hasAlias("the-alias", "appns.Class1")
			.and(aspect).indexPageHasAliasReferences("the-alias");
		when(aspect).requestReceivedInDev("js/dev/combined/bundle.js", response);
		then(response).containsCommonJsClasses("appns.Class1");
	}
	
	@Test
	public void weBundleAClassIfItTheAliasReferenceIsInDoubleQuotes() throws Exception {
		given(aspect).hasClass("appns/Class1")
			.and(aspectAliasesFile).hasAlias("the-alias", "appns.Class1")
			.and(aspect).indexPageHasContent("\"the-alias\"");
		when(aspect).requestReceivedInDev("js/dev/combined/bundle.js", response);
		then(response).containsCommonJsClasses("appns.Class1");
	}
	
	@Test
	public void weBundleAClassIfItTheAliasReferenceIsInXmlTag() throws Exception {
		given(aspect).hasClass("appns/Class1")
			.and(aspectAliasesFile).hasAlias("the-alias", "appns.Class1")
			.and(aspect).indexPageHasContent("<the-alias attr='val'/>");
		when(aspect).requestReceivedInDev("js/dev/combined/bundle.js", response);
		then(response).containsCommonJsClasses("appns.Class1");
	}
	
	@Test
	public void weBundleAClassIfItTheAliasReferenceIsInASelfClosingXmlTag() throws Exception {
		given(aspect).hasClass("appns/Class1")
			.and(aspectAliasesFile).hasAlias("the-alias", "appns.Class1")
			.and(aspect).indexPageHasContent("<the-alias/>");
		when(aspect).requestReceivedInDev("js/dev/combined/bundle.js", response);
		then(response).containsCommonJsClasses("appns.Class1");
	}
	
	// Blade/Aspect AliasDefinitions
	@Test
	public void weAlsoBundleAClassIfTheAliasIsDefinedInABladeAliasDefinitionsXml() throws Exception {
		given(appConf).hasRequirePrefix("appns")
			.and(aspect).hasClass("appns/Class1")
			.and(bladeAliasDefinitionsFile).hasAlias("appns.bs.b1.the-alias", "appns.Class1")
			.and(aspect).indexPageHasAliasReferences("appns.bs.b1.the-alias");	
		when(aspect).requestReceivedInDev("js/dev/combined/bundle.js", response);
		then(response).containsCommonJsClasses("appns.Class1");
	}
	
	public void weBundleAClassWhoseAliasIsReferredToFromAnotherCommonJsClass() throws Exception {
		given(aspect).hasClasses("appns.Class1", "appns.Class2")
			.and(aspectAliasesFile).hasAlias("the-alias", "appns.Class2")
			.and(aspect).indexPageRefersTo("appns.Class1")
			.and(aspect).classFileHasContent("appns.Class1", "aliasRegistry.getAlias('the-alias')");
		when(aspect).requestReceivedInDev("js/dev/combined/bundle.js", response);
		then(response).containsDefinedClasses("appns/Class1", "appns/Class2");
	}
	
	@Test
	public void weBundleAClassWhoseAliasIsReferredToFromAnotherNamespacedClass() throws Exception {
		given(aspect).hasNamespacedJsPackageStyle()
			.and(aspect).hasClasses("appns.Class1", "appns.Class2")
			.and(aspectAliasesFile).hasAlias("the-alias", "appns.Class2")
			.and(aspect).indexPageRefersTo("appns.Class1")
			.and(aspect).classDependsOnAlias("appns.Class1", "the-alias");
		when(aspect).requestReceivedInDev("js/dev/combined/bundle.js", response);
		then(response).containsNamespacedJsClasses("appns.Class1", "appns.Class2");
	}
	
	@Test
	public void bundlingWorksForAliasesDefinedAtTheBladeLevel() throws Exception {
		given(appConf).hasRequirePrefix("appns")
			.and(aspect).hasClass("appns/Class1")
			.and(bladeAliasDefinitionsFile).hasAlias("appns.bs.b1.the-alias", "appns.Class1")
			.and(aspect).indexPageHasAliasReferences("appns.bs.b1.the-alias");
		when(aspect).requestReceivedInDev("js/dev/combined/bundle.js", response);
		then(response).containsCommonJsClasses("appns.Class1");
	}
	
	@Test
	public void weBundleTheCorrespondingInterfaceForAliasesThatSpecifyAnInterface() throws Exception {
		given(aspect).hasClasses("appns/TheClass", "appns/TheInterface")
			.and(bladeAliasDefinitionsFile).hasAlias("appns.bs.b1.the-alias", "appns.TheClass", "appns.TheInterface")
			.and(aspect).indexPageHasAliasReferences("appns.bs.b1.the-alias");
		when(aspect).requestReceivedInDev("js/dev/combined/bundle.js", response);
		then(response).containsCommonJsClasses("appns/TheInterface", "appns/TheClass");
	}
	
	@Test
	public void weBundleTheDependenciesOfClassesIncludedViaAlias() throws Exception {
		given(aspect).hasClasses("appns/Class1", "appns/Class2")
			.and(aspect).classRequires("appns/Class1", "appns/Class2")
			.and(aspectAliasesFile).hasAlias("the-alias", "appns.Class1")
			.and(aspect).indexPageHasAliasReferences("the-alias");
		when(aspect).requestReceivedInDev("js/dev/combined/bundle.js", response);
		then(response).containsCommonJsClasses("appns.Class1", "appns.Class2");
	}
	
	@Test
	public void weDoNotBundleAClassIfADefinedAliasIsNotReferenced() throws Exception {
		given(appConf).hasRequirePrefix("appns")
			.and(aspect).hasClass("appns/Class1")
			.and(bladeAliasDefinitionsFile).hasAlias("appns.bs.b1.the-alias", "appns.Class1");
		when(aspect).requestReceivedInDev("js/dev/combined/bundle.js", response);
		then(response).doesNotContainClasses("appns.Class1");
	}
	
	@Test
	public void aliasesAreNotSetIfTheAliasRegistryClassDoesntExist() throws Exception {
		given(aspect).hasClass("appns/Class1")
			.and(aspectAliasesFile).hasAlias("the-alias", "appns.Class1")
			.and(aspect).indexPageRefersTo("appns.Class1");
		when(aspect).requestReceivedInDev("js/dev/combined/bundle.js", response);
		then(response).doesNotContainText("setAliasData(");
	}
	
	@Test
	public void aliasesAreNotSetIfTheAliasRegistryClassIsNotUsed() throws Exception {
		given(aspect).hasClass("appns/Class1")
			.and(brLib).hasClass("br/AliasRegistry")
			.and(aspect).indexPageRefersTo("appns.Class1");
		when(aspect).requestReceivedInDev("js/dev/combined/bundle.js", response);
		then(response).doesNotContainText("setAliasData(");
	}
	
	@Test
	public void theAliasBlobIsEmptyIfNoAliasesAreUsed() throws Exception {
		given(aspect).classRequires("appns/Class1", "alias!$data")
			.and(aspectAliasesFile).hasAlias("the-alias", "appns.Class1")
			.and(aspect).indexPageRefersTo("appns.Class1");
		when(aspect).requestReceivedInDev("js/dev/combined/bundle.js", response);
		then(response).containsText("module.exports = {};");
	}
	
	@Test
	public void theAliasBlobContainsAClassReferencedByAlias() throws Exception {
		given(aspect).classRequires("appns/Class1", "alias!$data")
			.and(aspectAliasesFile).hasAlias("the-alias", "appns.Class1")
			.and(aspect).indexPageHasAliasReferences("the-alias");
		when(aspect).requestReceivedInDev("js/dev/combined/bundle.js", response);
		then(response).containsText("module.exports = {'the-alias':{'class':'appns/Class1','className':'appns.Class1'}};");
	}
	
	@Test
	public void anExceptionIsThrownIfTheClassReferredToByAnAliasDoesntExist() throws Exception {
		given(aspectAliasesFile).hasAlias("the-alias", "NonExistentClass")
			.and(aspect).indexPageHasAliasReferences("the-alias");
		when(aspect).requestReceivedInDev("js/dev/combined/bundle.js", response);
		then(exceptions).verifyException(UnresolvableRequirePathException.class, "NonExistentClass");
	}
	
	@Test
	public void anExceptionIsThrownIfTheInterfaceReferredToByAnAliasDoesntExist() throws Exception {
		given(aspect).hasClass("appns/TheClass")
			.and(bladeAliasDefinitionsFile).hasAlias("appns.bs.b1.the-alias", "appns.TheClass", "NonExistentInterface")
			.and(aspect).indexPageHasAliasReferences("appns.bs.b1.the-alias");
		when(aspect).requestReceivedInDev("js/dev/combined/bundle.js", response);
		then(exceptions).verifyException(UnresolvableRequirePathException.class, "NonExistentInterface");
	}
	
	@Test
	public void aliasesDefinedInMultipleLocationsDontCauseATrieException() throws Exception {
		given(brLib).hasClasses("br/Class1")
			.and(brLibAliasDefinitionsFile).hasAlias("br.alias", "br.Class1")
			.and(aspect).hasClass("appns/Class1")
			.and(aspectAliasesFile).hasAlias("br.alias", "appns.Class1")
			.and(aspect).indexPageRefersTo("aliasRegistry.getAlias('br.alias')")
			.and(blade).hasClass("appns/bs/b1/Class1")
			.and(worbenchAliasesFile).hasAlias("br.alias", "appns/bs/b1/Class1");
		when(aspect).requestReceivedInDev("js/dev/combined/bundle.js", response);
		then(exceptions).verifyNoOutstandingExceptions();
	}
	
	
	@Test
	public void multipleAliasDefinitionsCanBeInAnAssetContainer() throws Exception {
		given(appConf).hasRequirePrefix("appns")
			.and(aspect).hasClasses("appns/pkg1/Class1", "appns/pkg1/pkg2/Class2", "appns/pkg1/pkg2/pkg3/Class3")
			.and(aspect.assetLocation("src/appns/pkg1").aliasDefinitionsFile()).hasAlias("appns.alias1", "appns.pkg1.Class1")
			.and(aspect.assetLocation("src/appns/pkg1/pkg2").aliasDefinitionsFile()).hasAlias("appns.alias2", "appns.pkg1.pkg2.Class2")
			.and(aspect.assetLocation("src/appns/pkg1/pkg2/pkg3").aliasDefinitionsFile()).hasAlias("appns.alias3", "appns.pkg1.pkg2.pkg3.Class3")
			.and(aspect).indexPageHasAliasReferences("\"appns.alias1\" 'appns.alias2' \"appns.alias3\"");	
		when(aspect).requestReceivedInDev("js/dev/combined/bundle.js", response);
		then(response).containsCommonJsClasses("appns.pkg1.Class1", "appns.pkg1.pkg2.Class2", "appns.pkg1.pkg2.pkg3.Class3");
	}
	
	@Ignore // TODO: figure out why the bundle is completely empty
	@Test
	public void unknownClassRepresentingAbstractAliasesIsSetAsANullClass() throws Exception {
		given(aspect).indexPageRequires("appns/SomeClass")
			.and(aspect).classRequires("appns/SomeClass", "alias!appns/bs/b1/the-alias")
			.and(bladeAliasDefinitionsFile).hasAlias("appns.bs.b1.the-alias", null, "appns/TheInterface")
			.and(aspect).hasClass("appns/TheInterface")
			.and(brLib).hasClass("br/UnknownClass");
		when(aspect).requestReceivedInDev("js/dev/combined/bundle.js", response);			
		then(response).containsText("module.exports = {'appns.bs.b1.the-alias':{'interface':'appns/TheInterface','interfaceName':'appns/TheInterface'}};");
	}
	
	@Test
	public void multipleAliasDefinitionsCanBeInsideResourcesFolderAndSubfoldersAndUsedInANamespacedClass() throws Exception {
		given(aspect).hasNamespacedJsPackageStyle()
			.and(aspect).hasClasses("appns.App", "appns.Class1", "appns.Class2", "appns.Class3")
			.and(aspect.aliasDefinitionsFile("resources")).hasAlias("appns.alias1", "appns.Class1")
			.and(aspect).containsFileWithContents("resources/subfolder/aliasDefinitions.xml",
					"<?xml version=\"1.0\" encoding=\"UTF-8\"?><aliasDefinitions xmlns=\"http://schema.caplin.com/CaplinTrader/aliasDefinitions\">\n" +
						"<alias defaultClass=\"appns.Class2\" name=\"appns.alias2\"/>\n" +
					"</aliasDefinitions>")
			.and(aspect).containsFileWithContents("resources/subfolder/subfolder/aliasDefinitions.xml",
					"<?xml version=\"1.0\" encoding=\"UTF-8\"?><aliasDefinitions xmlns=\"http://schema.caplin.com/CaplinTrader/aliasDefinitions\">\n" +
							"<alias defaultClass=\"appns.Class3\" name=\"appns.alias3\"/>\n" +
						"</aliasDefinitions>")
			.and(aspect).indexPageRefersTo("appns.App")
			.and(aspect).classFileHasContent("appns.App", "'appns.alias1' 'appns.alias2' 'appns.alias3'");
		when(aspect).requestReceivedInDev("js/dev/combined/bundle.js", response);
		then(response).containsNamespacedJsClasses("appns.Class1", "appns.Class2", "appns.Class3");
	}
	
	@Test
	public void multipleAliasDefinitionsCanBeInsideResourcesFolderAndSubfoldersAndUsedInACommonJsClass() throws Exception {
		given(brLib).hasClass("br/ServiceRegistry")
			.and(aspect).hasClasses("appns/App", "appns/Class1", "appns/Class2", "appns/Class3")
			.and(aspect.aliasDefinitionsFile("resources")).hasAlias("appns.alias1", "appns.Class1")
			.and(aspect).containsFileWithContents("resources/subfolder/aliasDefinitions.xml",
					"<?xml version=\"1.0\" encoding=\"UTF-8\"?><aliasDefinitions xmlns=\"http://schema.caplin.com/CaplinTrader/aliasDefinitions\">\n" +
						"<alias defaultClass=\"appns.Class2\" name=\"appns.alias2\"/>\n" +
					"</aliasDefinitions>")
			.and(aspect).containsFileWithContents("resources/subfolder/subfolder/aliasDefinitions.xml",
					"<?xml version=\"1.0\" encoding=\"UTF-8\"?><aliasDefinitions xmlns=\"http://schema.caplin.com/CaplinTrader/aliasDefinitions\">\n" +
							"<alias defaultClass=\"appns.Class3\" name=\"appns.alias3\"/>\n" +
						"</aliasDefinitions>")
			.and(aspect).indexPageRequires("appns/App")	
			.and(aspect).classFileHasContent("appns.App", "require('service!appns.alias1'); require('service!appns.alias2'); require('service!appns.alias3');");
		when(aspect).requestReceivedInDev("js/dev/combined/bundle.js", response);
		then(response).containsCommonJsClasses("appns.Class1", "appns.Class2", "appns.Class3");
	}
	
	@Test
	public void anExceptionIsThrownInTheAliasNameIsTheSameAsTheDefaultClass() throws Exception {
		given(aspect).hasClasses("appns/Class1")
			.and(aspect.aliasDefinitionsFile("src")).hasAlias("appns.Class1", "appns.Class1")
			.and(aspect).indexPageHasAliasReferences("appns.Class1");	
		when(aspect).requestReceivedInDev("js/dev/combined/bundle.js", response);
		then(exceptions).verifyException(AliasNameIsTheSameAsTheClassException.class, "appns.Class1");
	}
	
	@Test
	public void anExceptionIsThrownInTheAliasNameIsTheSameAsTheAssignedClass() throws Exception {
		given(aspect).hasClasses("appns/Class1", "appns/AliasClass")
			.and(aspect.aliasDefinitionsFile("src")).hasAlias("appns.AliasClass", "appns.Class1")
			.and(aspectAliasesFile).hasAlias("appns.AliasClass", "appns.AliasClass")
			.and(aspect).indexPageHasAliasReferences("appns.AliasClass");	
		when(aspect).requestReceivedInDev("js/dev/combined/bundle.js", response);
		then(exceptions).verifyException(AliasNameIsTheSameAsTheClassException.class, "appns.AliasClass");
	}
	
	@Test
	public void theAliasBlobContainsAliasDefinitionsOnlyOnceEvenIfTheyAreReferencedMultipleTimes() throws Exception {
		given(aspect).hasClasses("appns/Class1", "appns/Class2", "appns/Class3")
			.and(aspect).classRequires("appns/Class1", "alias!the-alias")
			.and(aspect).classRequires("appns/Class2", "alias!the-alias")
			.and(brLib).hasClass("br/AliasRegistry")
			.and(aspectAliasesFile).hasAlias("the-alias", "appns.Class3")
			.and(aspect).indexPageRequires("appns/Class1", "appns/Class2", "br/AliasRegistry"); 
		when(aspect).requestReceivedInDev("js/dev/combined/bundle.js", response);
		then(response).containsText("module.exports = require('appns/Class3');");
	}
	
	@Test
	public void aliasesInDefaultBladesetCanBeBundled() throws Exception {
		given(bladeInDefaultBladeset).hasClasses("appns/b1/BladeClass", "appns/b1/Class1")
			.and(bladeInDefaultBladeset).classRequires("appns/b1/BladeClass", "alias!the-alias")
    		.and(aspectAliasesFile).hasAlias("the-alias", "appns.b1.Class1")
    		.and(aspect).indexPageRequires("appns/b1/BladeClass");
		when(aspect).requestReceivedInDev("js/dev/combined/bundle.js", response);
    	then(response).containsText("module.exports = require('appns/b1/Class1');");
	}
	
	@Test
	public void weBundleABladeClassWhoseAliasIsReferredToFromAnotherCommonJsBladeClass() throws Exception {
		given(blade).hasClasses("appns/bs/b1/Class1", "appns/bs/b1/Class2")
			.and(bladeAliasDefinitionsFile).hasAlias("appns.bs.b1.the-alias", "appns.bs.b1.Class2")
			.and(aspect).indexPageRefersTo("appns.bs.b1.Class1")
			.and(blade).classFileHasContent("appns/bs/b1/Class1", "require('alias!appns.bs.b1.the-alias')");
		when(aspect).requestReceivedInDev("js/dev/combined/bundle.js", response);
		then(response).containsDefinedClasses("appns/bs/b1/Class1", "appns/bs/b1/Class2");
	}
	
	@Test // Note: this test was written in an attempt to exactly replicate a bug we were seeing in the product
	public void workbenchesThatRequestTheDevScenarioArentInsteadGivenANonDevNamedGroupInstead() throws Exception {
		given(brLib).hasClasses("br/AliasRegistry", "br/ServiceRegistry", "br/Core", "br/UnknownClass", "br/AliasInterfaceError")
			.and(brLib).hasClasses("br/Interface", "br/DevScenarioClass", "br/GroupProductionClass")
			.and(brLibAliasDefinitionsFile).hasIncompleteAlias("br.service", "br/Interface")
			.and(brLibAliasDefinitionsFile).hasScenarioAlias("dev", "br.service", "br/DevScenarioClass")
			.and(brLibAliasDefinitionsFile).hasGroupAlias("br.g1", "br.service", "br/GroupProductionClass")
			.and(workbench).indexPageRequires("appns/WorkbenchClass")
			.and(workbench).classFileHasContent("appns/WorkbenchClass", "require('service!br.service'); require('br/AliasRegistry');")
			.and(worbenchAliasesFile).usesScenario("dev");
		when(workbench).requestReceivedInDev("js/dev/combined/bundle.js", response);
		then(response).containsCommonJsClasses("br/DevScenarioClass")
			.and(response).doesNotContainClasses("br/GroupProductionClass")
			.and(response).containsOrderedTextFragments(
				"define('alias!br.service',",
				"module.exports = require('br/DevScenarioClass');",
				"define('service!br.service',",
				"module.exports = require('br/ServiceRegistry').getService('br.service');");
	}
	
	@Test
	public void aliasesRequestedForAWorkbenchArentCachedAndReusedForAnAspect() throws Exception {
		given(brLib).hasClasses("br/Class", "br/StubClass")
			.and(aspectAliasesFile).hasAlias("the-alias", "br/Class")
			.and(worbenchAliasesFile).hasAlias("the-alias", "br/StubClass")
			.and(aspect).indexPageRequires("alias!the-alias")
			.and(workbench).indexPageRequires("alias!the-alias")
			.and(workbench).hasReceivedRequest("js/dev/combined/bundle.js");
		when(aspect).requestReceivedInDev("js/dev/combined/bundle.js", response);
		then(response).containsText("module.exports = require('br/Class');");
	}
}