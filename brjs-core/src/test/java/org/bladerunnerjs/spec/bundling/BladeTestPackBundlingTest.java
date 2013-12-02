package org.bladerunnerjs.spec.bundling;

import org.bladerunnerjs.model.App;
import org.bladerunnerjs.model.Blade;
import org.bladerunnerjs.model.Bladeset;
import org.bladerunnerjs.model.TestPack;
import org.bladerunnerjs.specutil.engine.SpecTest;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;


public class BladeTestPackBundlingTest extends SpecTest
{
	private App app;
	private Bladeset bladeset;
	private Blade blade;
	private TestPack bladeUTs, bladeATs;
	
	@Before
	public void initTestObjects() throws Exception
	{
		given(brjs).hasBeenCreated();
			app = brjs.app("app1");
			bladeset = app.bladeset("bs");
			blade = bladeset.blade("b1");
			bladeUTs = blade.testType("unit").testTech("TEST_TECH");
			bladeATs = blade.testType("acceptance").testTech("TEST_TECH");
	}
	
	// TODO remove the @Ignores
	@Ignore
	@Test
	public void weBundleBladeFilesInUTs() throws Exception {
		given(blade).hasPackageStyle("src/novox/bs/b1", "caplin-js")
			.and(blade).hasClasses("novox.bs.b1.Class1", "novox.bs.b1.Class2")
			.and(blade).classRefersTo("novox.bs.b1.Class1", "novox.bs.b1.Class2")
			.and(bladeUTs).testRefersTo("novox.bs.b1.Class1");
		then(bladeUTs).bundledFilesEquals(
				blade.src().file("src/novox/bs/b1/Class1.js"),
				blade.src().file("src/novox/bs/b1/Class2.js"));
	}
	
	@Ignore
	@Test
	public void weBundleBladeFilesInATs() throws Exception {
		given(blade).hasPackageStyle("src/novox/bs/b1", "caplin-js")
			.and(blade).hasClasses("novox.bs.b1.Class1", "novox.bs.b1.Class2")
			.and(blade).classRefersTo("novox.bs.b1.Class1", "novox.bs.b1.Class2")
			.and(bladeUTs).testRefersTo("novox.bs.b1.Class1");
		then(bladeATs).bundledFilesEquals(
				blade.src().file("src/novox/bs/b1/Class1.js"),
				blade.src().file("src/novox/bs/b1/Class2.js"));
	}
}