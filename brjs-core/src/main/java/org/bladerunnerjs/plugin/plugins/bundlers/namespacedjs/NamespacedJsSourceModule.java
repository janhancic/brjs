package org.bladerunnerjs.plugin.plugins.bundlers.namespacedjs;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import org.bladerunnerjs.memoization.MemoizedValue;
import org.bladerunnerjs.model.AssetFileInstantationException;
import org.bladerunnerjs.model.AssetLocationUtility;
import org.bladerunnerjs.model.BundlableNode;
import org.bladerunnerjs.model.FullyQualifiedLinkedAsset;
import org.bladerunnerjs.model.LinkedAsset;
import org.bladerunnerjs.model.AssetLocation;
import org.bladerunnerjs.model.SourceModule;
import org.bladerunnerjs.model.SourceModulePatch;
import org.bladerunnerjs.model.TrieBasedDependenciesCalculator;
import org.bladerunnerjs.model.exception.ModelOperationException;
import org.bladerunnerjs.model.exception.RequirePathException;
import org.bladerunnerjs.utility.RelativePathUtility;
import org.bladerunnerjs.utility.reader.JsCodeBlockStrippingReader;
import org.bladerunnerjs.utility.reader.JsCommentStrippingReader;
import org.bladerunnerjs.utility.reader.JsStringStrippingReader;

import com.Ostermiller.util.ConcatReader;

public class NamespacedJsSourceModule implements SourceModule {
	private static final String DEFINE_BLOCK = "\ndefine('%s', function(require, exports, module) { module.exports = %s; });";
	
	private LinkedAsset linkedAsset;
	private AssetLocation assetLocation;
	private String requirePath;
	private String className;
	private SourceModulePatch patch;
	private TrieBasedDependenciesCalculator dependencyCalculator;
	private TrieBasedDependenciesCalculator staticDependencyCalculator;
	
	private MemoizedValue<List<AssetLocation>> assetLocationsList;
	
	@Override
	public void initialize(AssetLocation assetLocation, File dir, String assetName) throws AssetFileInstantationException
	{
		try {
			File assetFile = new File(dir, assetName);
			
			this.assetLocation = assetLocation;
			requirePath = assetLocation.requirePrefix() + "/" + RelativePathUtility.get(assetLocation.dir(), assetFile).replaceAll("\\.js$", "");
			className = requirePath.replaceAll("/", ".");
			linkedAsset = new FullyQualifiedLinkedAsset();
			linkedAsset.initialize(assetLocation, dir, assetName);
			patch = SourceModulePatch.getPatchForRequirePath(assetLocation, getRequirePath());
			dependencyCalculator = new TrieBasedDependenciesCalculator(this, assetFile, patch.getPatchFile());
			staticDependencyCalculator = new TrieBasedDependenciesCalculator(this, assetFile, patch.getPatchFile());
			assetLocationsList = new MemoizedValue<>("NamespacedJsSourceModule.assetLocations", assetLocation.root(), assetLocation.assetContainer().dir());
		}
		catch(RequirePathException e) {
			throw new AssetFileInstantationException(e);
		}
	}
	
	@Override
 	public List<SourceModule> getDependentSourceModules(BundlableNode bundlableNode) throws ModelOperationException {
		// TODO: is this a bug since we are returning all dependencies, whether they are reachable via the bundlable node or not?
		try (Reader commentStrippingReader = new JsCommentStrippingReader(getReader(), false)) {
			return dependencyCalculator.getCalculatedDependentSourceModules(commentStrippingReader);			
		}
		catch (IOException ex)
		{
			throw new RuntimeException(ex);
		}
	}
	
	@Override
	public List<String> getAliasNames() throws ModelOperationException {
		try (Reader commentStrippingReader = new JsCommentStrippingReader(getReader(), false)) {			
			return dependencyCalculator.getCalculataedAliases(commentStrippingReader);
		}
		catch (IOException ex)
		{
			throw new RuntimeException(ex);
		}
	}
	
	@Override
	public Reader getReader() throws IOException {
		String formattedDefineBlock = String.format(DEFINE_BLOCK, requirePath, className);
		Reader[] readers = new Reader[] { linkedAsset.getReader(), patch.getReader(), new StringReader(formattedDefineBlock) };
		return new ConcatReader( readers );
	}
	
	@Override
	public String getRequirePath() {
		return requirePath;
	}
	
	@Override
	public String getClassname() {
		return className;
	}
	
	@Override
	public boolean isEncapsulatedModule() {
		return false;
	}
	
	@Override
	public List<SourceModule> getOrderDependentSourceModules(BundlableNode bundlableNode) throws ModelOperationException {				
		try (
			Reader commentStrippingReader = new JsCommentStrippingReader(getReader(), false);
			Reader commentStrippingAndStringStrippingReader = new JsStringStrippingReader(commentStrippingReader);
			Reader commentStrippingAndStringStrippingAndCodeBlockStrippingReader = new JsCodeBlockStrippingReader(commentStrippingAndStringStrippingReader);
		) {
			return staticDependencyCalculator.getCalculatedDependentSourceModules(commentStrippingAndStringStrippingAndCodeBlockStrippingReader);
		}
		catch (IOException ex)
		{
			throw new RuntimeException(ex);
		}
	}
	
	@Override
	public File dir()
	{
		return linkedAsset.dir();
	}
	
	@Override
	public String getAssetName() {
		return linkedAsset.getAssetName();
	}
	
	@Override
	public String getAssetPath() {
		return linkedAsset.getAssetPath();
	}
	
	@Override
	public AssetLocation assetLocation()
	{
		return assetLocation;
	}
	
	@Override
	public List<AssetLocation> assetLocations() {
		return assetLocationsList.value(() -> {
			return AssetLocationUtility.getAllDependentAssetLocations(assetLocation);
		});
	}
}
