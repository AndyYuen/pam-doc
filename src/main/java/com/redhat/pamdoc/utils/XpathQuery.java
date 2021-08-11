package com.redhat.pamdoc.utils;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


import java.io.IOException;
import java.io.InputStream;
import java.io.File;

public class XpathQuery {

    private DocumentBuilder builder;
    private XPath xPath;
    private Document document;
    
    public XpathQuery() throws ParserConfigurationException {
        // parti initialise XML XPATH stuff
		builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        xPath = XPathFactory.newInstance().newXPath();
        document = null;
    }

    public void parseDocument(String xmlFileName) throws SAXException, IOException {
        document = builder.parse(new File(xmlFileName));    
    }
    
    public void parseDocument(InputStream stream) throws SAXException, IOException {
        document = builder.parse(stream);    
    }

    public Integer getEntityCount(String xpathExpr) throws SAXException, XPathExpressionException, IOException {
        if (document == null) throw new IOException("No document defined.");
		return ((Double) xPath.compile(xpathExpr).evaluate(document, XPathConstants.NUMBER)).intValue();
	}
    
    public NodeList getNodeList(String xpathExpr) throws SAXException, XPathExpressionException, IOException {
        if (document == null) throw new IOException("No document defined.");
		return (NodeList) xPath.compile(xpathExpr).evaluate(document, XPathConstants.NODESET);
	}
    
    public String getStringValue(String xpathExpr) throws SAXException, XPathExpressionException, IOException {
        if (document == null) throw new IOException("No document defined.");
		return (String) xPath.compile(xpathExpr).evaluate(document, XPathConstants.STRING);
	}
}