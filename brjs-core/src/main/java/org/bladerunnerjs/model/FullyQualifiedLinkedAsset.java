package org.bladerunnerjs.model;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

import org.bladerunnerjs.model.exception.ConfigException;
import org.bladerunnerjs.model.exception.ModelOperationException;
import org.bladerunnerjs.utility.RelativePathUtility;
import org.bladerunnerjs.utility.UnicodeReader;
import org.bladerunnerjs.utility.reader.JsCommentStrippingReader;

/**
 * A linked asset file that refers to another AssetFile using a fully qualified name such as 'my.package.myClass'
 *
 */
public class FullyQualifiedLinkedAsset implements LinkedAsset {
	private App app;
	private File assetFile;
	private AssetLocation assetLocation;
	private String assetPath;
	private String defaultFileCharacterEncoding;
	private TrieBasedDependenciesCalculator dependencyCalculator;
	
	public void initialize(AssetLocation assetLocation, File dir, String assetName)
	{
		try {
			this.assetLocation = assetLocation;
			app = assetLocation.assetContainer().app();
			this.assetFile = new File(dir, assetName);
			assetPath = RelativePathUtility.get(app.dir(), assetFile);
			defaultFileCharacterEncoding = assetLocation.root().bladerunnerConf().getDefaultFileCharacterEncoding();
			dependencyCalculator = new TrieBasedDependenciesCalculator(this, assetFile);
		}
		catch(ConfigException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public Reader getReader() throws IOException {
		return new UnicodeReader(assetFile, defaultFileCharacterEncoding);
	}
	
	@Override
	public List<SourceModule> getDependentSourceModules(BundlableNode bundlableNode) throws ModelOperationException {
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
	public File dir()
	{
		return assetFile.getParentFile();
	}
	
	@Override
	public String getAssetName() {
		return assetFile.getName();
	}
	
	@Override
	public String getAssetPath() {
		return assetPath;
	}
	
	@Override
	public AssetLocation assetLocation()
	{
		return assetLocation;
	}
}
