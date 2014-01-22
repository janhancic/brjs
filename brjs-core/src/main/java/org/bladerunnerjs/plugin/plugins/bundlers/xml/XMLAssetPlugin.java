package org.bladerunnerjs.plugin.plugins.bundlers.xml;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.bladerunnerjs.model.Asset;
import org.bladerunnerjs.model.AssetContainer;
import org.bladerunnerjs.model.AssetFileInstantationException;
import org.bladerunnerjs.model.AssetLocation;
import org.bladerunnerjs.model.BRJS;
import org.bladerunnerjs.model.FullyQualifiedLinkedAsset;
import org.bladerunnerjs.model.JsLib;
import org.bladerunnerjs.model.LinkedAsset;
import org.bladerunnerjs.model.SourceModule;
import org.bladerunnerjs.plugin.base.AbstractAssetPlugin;

public class XMLAssetPlugin extends AbstractAssetPlugin {
	
	private IOFileFilter notAliasFileFilter = FileFilterUtils.notFileFilter( new NameFileFilter("aliases.xml" ) );
	private IOFileFilter xmlFilesFilter = FileFilterUtils.and( notAliasFileFilter, new SuffixFileFilter(".xml") );
	
	@Override
	public void setBRJS(BRJS brjs) {
	}
	
	@Override
	public List<SourceModule> getSourceModules(AssetLocation assetLocation) {
		return new ArrayList<>();
	}
	
	@Override
	public List<LinkedAsset> getLinkedAssets(AssetLocation assetLocation) {
		List<LinkedAsset> assets;
		
		try {
			AssetContainer assetContainer = assetLocation.getAssetContainer();
			
			// TODO: it doesn't make sense that the XML bundler plug-in can never be used for any future library type -- we will need to re-factor
			// once we have concrete use cases for supporting this
			if(assetContainer instanceof JsLib) {
				assets = new ArrayList<>();
			}
			else {
				assets = assetContainer.root().createAssetFiles(FullyQualifiedLinkedAsset.class, assetLocation,  xmlFilesFilter);
			}
		}
		catch (AssetFileInstantationException e) {
			throw new RuntimeException(e);
		}
		
		return assets;
	}
	
	@Override
	public List<Asset> getAssets(AssetLocation assetLocation) {
		return new ArrayList<>();
	}
}
