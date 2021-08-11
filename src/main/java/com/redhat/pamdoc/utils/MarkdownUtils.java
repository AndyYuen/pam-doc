package com.redhat.pamdoc.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.FileUtils;
import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class MarkdownUtils {

	private static final Logger log = LoggerFactory.getLogger(MarkdownUtils.class);
	
    // get file content
    static public String getFileContent(String filePath)  {
        if (filePath == null) return null;
        String content = null;
		
		try {
			content = FileUtils.readFileToString(new File(filePath), "UTF-8");
		} catch (IOException e) {
			log.error(e.getMessage());
		}

        return content;
    }

    // return parsed metadata from PAM
    static public Map<String, String> parseMetadata(String content)  {
        Map<String, String> map = new HashMap<>();

        if (content != null) {

			try {
				XpathQuery query = new XpathQuery();
				query.parseDocument(new ByteArrayInputStream(content.getBytes()));
				NodeList nodeList = query.getNodeList("//string");

				for (int i = 0; i < nodeList.getLength(); i += 2) { 
					String key =  ((org.w3c.dom.Node) nodeList.item(i)).getTextContent().substring(6);
					key = key.substring(0, key.indexOf('['));
					if ((i + 1) >= nodeList.getLength()) break;
					String value = ((org.w3c.dom.Node) nodeList.item(i + 1)).getTextContent();
					map.put(key, value);
					log.info("key=" + key + ", value=" + value);
				}


			} catch (ParserConfigurationException e) {
				log.error(e.getMessage());
			} catch (IOException e) {
				log.error(e.getMessage());
			} catch (SAXException e) {
				log.error(e.getMessage());
			} catch (XPathExpressionException e) {
				log.error(e.getMessage());				
			}
        }

        return map;
	}
	
	// convert markdown to html
    static public String convertToHtml(String markdown) {
		List<Extension> extensions = Arrays.asList(TablesExtension.create());
		Parser parser = Parser.builder()
				.extensions(extensions)
				.build();
		org.commonmark.node.Node document = parser.parse(markdown);
		HtmlRenderer renderer = HtmlRenderer.builder()
				.extensions(extensions)
				.build();

		//HtmlRenderer renderer = HtmlRenderer.builder().escapeHtml(true).build();
		return renderer.render(document);
	}
	
    static public String processMetadata(String fileName) {
		String content = getFileContent(fileName);
		// log.info(content);
		Map<String, String> map = parseMetadata(content);
		
		String html = "<em>No Metadata for this business process.</em>";
		if (map.containsKey("description")) {
			String markdown = map.get("description");
			// log.info("Markdown: " + markdown);
			html = convertToHtml(markdown);
			//log.info("html: " + html);
		}
		return html;
	}
	
}
