package org.bladerunnerjs.aliasing.aliases;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.FileUtils;
import org.bladerunnerjs.aliasing.AliasOverride;
import org.bladerunnerjs.testing.specutility.XmlBuilderSerializer;

import com.esotericsoftware.yamlbeans.parser.Parser.ParserException;
import com.google.common.base.Joiner;
import com.jamesmurty.utils.XMLBuilder;

public class AliasesWriter {
	private final AliasesData data;
	private final File file;
	private String defaultInputEncoding;
	
	public AliasesWriter(AliasesData data, File file, String defaultInputEncoding) {
		this.data = data;
		this.file = file;
		this.defaultInputEncoding = defaultInputEncoding;
	}
	
	public void write() throws IOException {
		try {
			XMLBuilder builder = XMLBuilder.create("aliases").ns("http://schema.caplin.com/CaplinTrader/aliases");
			
			if (data.scenario != null) {
				builder.a("useScenario", data.scenario);
			}
			
			if (!data.groupNames.isEmpty()) {
				builder.a("useGroups", Joiner.on(" ").join(data.groupNames));
			}
			
			for (AliasOverride aliasOverride : data.aliasOverrides) {
				builder.e("alias").a("name", aliasOverride.getName()).a("class", aliasOverride.getClassName());
			}
			
			FileUtils.write(file, XmlBuilderSerializer.serialize(builder), defaultInputEncoding);
		} catch (ParserException | TransformerException | ParserConfigurationException | FactoryConfigurationError e) {
			throw new IOException(e);
		}
	}
	
}
