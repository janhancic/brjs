package org.bladerunnerjs.plugin.seedlocator;

import java.util.Collections;
import java.util.List;

import org.bladerunnerjs.api.Asset;
import org.bladerunnerjs.api.BRJS;
import org.bladerunnerjs.api.JsLib;
import org.bladerunnerjs.api.LinkedAsset;
import org.bladerunnerjs.api.memoization.MemoizedFile;
import org.bladerunnerjs.api.plugin.AssetDiscoveryInitiator;
import org.bladerunnerjs.api.plugin.base.AbstractAssetPlugin;
import org.bladerunnerjs.model.AssetContainer;


public class BrLocaleLibSeedLocator extends AbstractAssetPlugin
{

	@Override
	public List<Asset> discoverAssets(AssetContainer assetContainer, MemoizedFile dir, String requirePrefix, List<Asset> implicitDependencies, AssetDiscoveryInitiator assetDiscoveryInitiator)
	{
		if (assetContainer instanceof JsLib) {
			JsLib jsLib = (JsLib) assetContainer;
			if (jsLib.getName().equals("br-locale")) {
				Asset brLocaleSeedAsset = assetDiscoveryInitiator.getRegisteredAsset("br-locale/switcher");
				if (brLocaleSeedAsset instanceof LinkedAsset && !assetDiscoveryInitiator.hasSeedAsset(brLocaleSeedAsset.getPrimaryRequirePath())) {
					assetDiscoveryInitiator.promoteRegisteredAssetToSeed( (LinkedAsset)brLocaleSeedAsset );
				}
			}
		}
		return Collections.emptyList();
	}

	@Override
	public void setBRJS(BRJS brjs)
	{
		// TODO Auto-generated method stub
		
	}

}