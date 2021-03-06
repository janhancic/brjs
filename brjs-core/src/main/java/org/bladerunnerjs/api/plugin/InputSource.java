package org.bladerunnerjs.api.plugin;

import java.io.Reader;

import org.bladerunnerjs.api.BundleSet;
import org.bladerunnerjs.api.model.exception.request.ContentProcessingException;
import org.bladerunnerjs.api.model.exception.request.MalformedRequestException;
import org.bladerunnerjs.model.UrlContentAccessor;

public class InputSource {
	
//	private static final Pattern SOURCE_MAPPING_URL_PATTERN = Pattern.compile("\n//# sourceMappingURL=(.*)$");
	
	private String requestPath;
	private final ContentPlugin contentPlugin;
	private BundleSet bundleSet;
	private UrlContentAccessor contentPluginUtility;
	private String version;
	
	public InputSource(String requestPath, ContentPlugin contentPlugin, BundleSet bundleSet, UrlContentAccessor contentPluginUtility, String version) {
		this.requestPath = requestPath;
		this.contentPlugin = contentPlugin;
		this.bundleSet = bundleSet;
		this.contentPluginUtility = contentPluginUtility;
		this.version = version;
	}
	
	public Reader getContentPluginReader() throws ContentProcessingException {
		try {
			ResponseContent pluginContent = contentPlugin.handleRequest(requestPath, bundleSet, contentPluginUtility, version);
			if (pluginContent instanceof CharResponseContent) {
				return ((CharResponseContent) pluginContent).getReader();
			}
			throw new RuntimeException("Minifies only support content plugins that return an instance of '"+CharResponseContent.class.getSimpleName()+"'.");
		}
		catch(MalformedRequestException e) {
			throw new ContentProcessingException(e);
		}
	}
	
	
//TODO: re-enable source map support
//	public String getSourceMap(String version) throws MalformedRequestException, ContentProcessingException {
//		String sourceMap = null;
//		
//		try {
//			if(source != filteredSource) {
//				ByteArrayOutputStream bos = new ByteArrayOutputStream();
//				ParsedContentPath contentPath = contentPlugin.getContentPathParser().parse(sourceMappingUrl);
//				ContentPluginUtility contentOutputStream = new StaticContentPluginUtility(bundleSet.getBundlableNode().app(), bos);
//				
//				contentPlugin.writeContent(contentPath, bundleSet, contentOutputStream, version);
//				
//				sourceMap = bos.toString(bundleSet.getBundlableNode().root().bladerunnerConf().getBrowserCharacterEncoding());
//			}
//		}
//		catch(ConfigException | IOException e) {
//			throw new ContentProcessingException(e);
//		}
//		
//		return sourceMap;
//	}
//	
//	private String getSourceMappingUrl(String source) {
//		Matcher matcher = SOURCE_MAPPING_URL_PATTERN.matcher(source);
//		String sourceMappingUrl = null;
//		
//		if(matcher.find()) {
//			sourceMappingUrl = matcher.group(1);
//		}
//		
//		return sourceMappingUrl;
//	}
	
}
