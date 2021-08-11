package com.redhat.pamdoc.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.springframework.stereotype.Component;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.commons.io.FilenameUtils;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.xml.sax.SAXException;

import com.redhat.pamdoc.model.DataModelInfo;
import com.redhat.pamdoc.model.ProcessInfo;
import com.redhat.pamdoc.model.ProjectInfo;
import com.redhat.pamdoc.model.RuleInfo;
import com.redhat.pamdoc.utils.MarkdownUtils;
import com.redhat.pamdoc.utils.XpathQuery;



@Component
public class DocoServiceImpl implements DocoService {
    
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private List<Triple<String, Set<String>, String>> extractAssetAndMetadata(String repoDir, String assetExtension, RuleInfo info) {
		// get asset
		ArrayList<File> dirList = new ArrayList<>(FileUtils.listFiles(new File(repoDir), 
				new SuffixFileFilter("." + assetExtension),
				DirectoryFileFilter.INSTANCE));
		log.info(assetExtension + " file count: " + dirList.size());

		List<Triple<String, Set<String>, String>> list = new ArrayList<>();
		for (File file : dirList) {
			if (!file.getName().startsWith(".")) {
				String fileName = file.getPath();
				log.info(fileName);
				// only asset file with associated metadata file will be processed
				log.info("path=" + FilenameUtils.getFullPath(fileName));
				String metaFileName = FilenameUtils.getFullPath(fileName) + "." + FilenameUtils.getName(fileName);
				log.info("metaFileName=" + metaFileName + " Exists: " + Files.exists(Paths.get(metaFileName)));
				String metadata = "&nbsp;  &nbsp;  &nbsp;  &nbsp;  &nbsp;<em>No metadata</em>";
				if (Files.exists(Paths.get(metaFileName))) {
					// process metadata
					metadata = MarkdownUtils.processMetadata(metaFileName);
				}

				Set<String> ruleflow = null;
				if (assetExtension.equals("gdst")) {
					//log.info("gdst file content=\n" + MarkdownUtils.getFileContent(fileName));
					try {
						XpathQuery query = new XpathQuery();
						
						// locate work item handlers
						String deploymentDescriptorFileName = repoDir + "/src/main/resources/META-INF/kie-deployment-descriptor.xml";
						log.info("fileName=" + fileName);
						query.parseDocument(fileName);
						Integer index = query.getEntityCount("count(//attributeCols/preceding-sibling::*)") - 1;
						NodeList ruleflowGroups = query.getNodeList("//data/list/value[" + index + "]/valueString/text()");
				
						if (ruleflowGroups.getLength() > 0) {
							ruleflow = new TreeSet<>();
							for (int i = 0; i < ruleflowGroups.getLength(); i++) { 
								ruleflow.add((ruleflowGroups.item(i)).getTextContent());			
								log.info("ruleflowGroup=" + (ruleflowGroups.item(i)).getTextContent());
							}
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
				
				if (assetExtension.equals("drl")) {
					Pattern p = Pattern.compile("ruleflow-group\\s+\"([0-9a-zA-Z\\-]+)\"");
					Matcher m = p.matcher(MarkdownUtils.getFileContent(fileName));  
					while (m.find()) {
						if (ruleflow == null) ruleflow = new TreeSet<>();
						ruleflow.add(m.group(1));
					}
				}
				
				list.add(Triple.of(FilenameUtils.getBaseName(file.getName()), ruleflow, metadata));
			}
		}
		log.info(list.toString());
		return list;
	}


	/* ****************** Implementing interface ****************** */
	
	// Extract data model documentation
	public List<DataModelInfo> extractDataModelInfo(String repoDir) {
		
		String javaDir = repoDir + "/src/main/java";
		log.info("javaDir=" + javaDir);
		ArrayList<File> dirList = new ArrayList<>(FileUtils.listFiles(new File(javaDir), 
				new SuffixFileFilter(".java"),
				DirectoryFileFilter.INSTANCE));
		log.info("Data model files: " + dirList);
		
		List<DataModelInfo> list = new ArrayList<>();
		for (File file: dirList) {
			String path = FilenameUtils.getFullPath(file.getPath()).replace('/', '.');
			path = path.substring(0, path.length() - 1);
			list.add(new DataModelInfo(path.substring(path.indexOf(".java") + 6),
					FilenameUtils.getBaseName(file.getName())));
		}
		
		return list;
	}

	// Extract project documentation
	public ProjectInfo extractProjectInfo(String repoDir, String name) {
		ProjectInfo info = new ProjectInfo();
		info.setName(name);
		
		try {
			XpathQuery query = new XpathQuery();
			
			// locate work item handlers
			String deploymentDescriptorFileName = repoDir + "/src/main/resources/META-INF/kie-deployment-descriptor.xml";
			log.info("deploymentDescriptorFileName=" + deploymentDescriptorFileName);
			query.parseDocument(deploymentDescriptorFileName);
			NodeList nameNodeList = query.getNodeList("//work-item-handler/name");
			NodeList idNodeList = query.getNodeList("//work-item-handler/identifier");
	
			if (nameNodeList.getLength() > 0) {
				List<Pair<String, String>> wiList = new ArrayList<>();
				info.setWorkItemHandlers(wiList);
				for (int i = 0; i < nameNodeList.getLength(); i++) { 
					wiList.add(Pair.of(((org.w3c.dom.Node) nameNodeList.item(i)).getTextContent(),
					((org.w3c.dom.Node) idNodeList.item(i)).getTextContent()));
	
				}
			}
			
			// locate dependencies
		

		} catch (ParserConfigurationException e) {
			log.error(e.getMessage());
		} catch (IOException e) {
			log.error(e.getMessage());
		} catch (SAXException e) {
			log.error(e.getMessage());
		} catch (XPathExpressionException e) {
			log.error(e.getMessage());				
		}
		
		return info;
	}
	
	// Extract business process documentation
	public List<ProcessInfo> extractBusinessProcessesInfo(String repoDir, String realPath) {
		
		List<ProcessInfo> procList = new ArrayList<>();
		// process bpmn file
		ArrayList<File> dirList = new ArrayList<>(FileUtils.listFiles(new File(repoDir), 
			new SuffixFileFilter(".bpmn"),
			DirectoryFileFilter.INSTANCE));
		log.info("Business Process file count: " + dirList.size());


		for (File file : dirList) {
			String bpmnFileName = file.getPath();
			log.info(bpmnFileName);
			// only bpmn file with associated metadata file will be processed

				if (!file.getName().startsWith(".")) {
				
					ProcessInfo processInfo = new ProcessInfo();
					procList.add(processInfo);

					// only asset file with associated metadata file will be processed
					String metaFileName = FilenameUtils.getFullPath(bpmnFileName) + "." + FilenameUtils.getName(bpmnFileName);
					log.info("metaFileName=" + metaFileName + " Exists: " + Files.exists(Paths.get(metaFileName)));
					String metadata = "&nbsp;  &nbsp;  &nbsp;  &nbsp;  &nbsp;<em>No metadata</em>";
					if (Files.exists(Paths.get(metaFileName))) {
					
						// locate metadata file and convert to html
						metadata = MarkdownUtils.processMetadata(metaFileName);
					}
					processInfo.setMetadataHtml(metadata);
	
					try {
						// locate corresponding business process and extract relevant info
						// String bpmnFileName = fileName.substring(0, fileName.indexOf('.')) + fileName.substring(fileName.indexOf('.') + 1);
//						String bpmnFileName = fileName;
						log.info("bpmnFilename=" + bpmnFileName);
	
						
						XpathQuery query = new XpathQuery();
						query.parseDocument(bpmnFileName);
	//					Integer count = query.getEntityCount("count(//*[contains(local-name(),'Activity')])");
	//					log.info("subProcess count=" + count);
						
						
						// get process diagram name
						// package name //*[local-name() = 'process']/@*[local-name() = 'packageName']
						// id //*[local-name() = 'process']/@id
						String processId = query.getStringValue("//*[local-name() = 'process']/@id");
						processInfo.setProcessId(processId);
						
						String imgSrc = FilenameUtils.getFullPath(bpmnFileName) + processId + "-svg.svg";
						//String imgDes = realPath + "/" + FilenameUtils.getBaseName(imgSrc) + UUID.randomUUID().toString() + ".svg";
						String imgDes = realPath + "/" + FilenameUtils.getName(imgSrc);
						log.info("imgSrc=" + imgSrc + ", imgDes=" + imgDes);
						FileUtils.copyFile(new File(imgSrc), new File(imgDes));
						processInfo.setImageName("/static/img/" + FilenameUtils.getName(imgDes));
						
						
						// locate process variables
						NodeList varNodeList = query.getNodeList("//*[local-name() = 'property']");
						if (varNodeList.getLength() > 0) {
							List<Pair<String, String>> varList = new ArrayList<>();
							processInfo.setVariables(varList);
							for (int i = 0; i < varNodeList.getLength(); i++) { 
								NamedNodeMap nodeMap = ((org.w3c.dom.Node) varNodeList.item(i)).getAttributes();
								String ref = nodeMap.getNamedItem("itemSubjectRef").getTextContent();
								String expr = "//*[local-name() = 'itemDefinition' and @id = '" + 
										ref + "']/@structureRef";
								String def = query.getStringValue(expr);
								log.info("expr=" + expr);
								varList.add(Pair.of(nodeMap.getNamedItem("name").getTextContent(),
										def));
							}
						}
						
						// locate ruleflows //*[local-name() = 'businessRuleTask']/@*[local-name() = 'ruleFlowGroup']
						NodeList rfNodeList = query.getNodeList("//*[local-name() = 'businessRuleTask']/@*[local-name() = 'ruleFlowGroup']");
						if (rfNodeList.getLength() > 0) {
							Set<String> rfSet = new TreeSet<>();
							processInfo.setRuleflows(rfSet);
							for (int i = 0; i < rfNodeList.getLength(); i++) { 
								rfSet.add(((org.w3c.dom.Node) rfNodeList.item(i)).getTextContent());
							}
						}
						
						// locate DMN used
						// //*[local-name() = 'businessRuleTask']/@implementation
						// //*[local-name() = 'businessRuleTask']//*[local-name() = 'metaValue']/text()
						NodeList taskNodeList = query.getNodeList("//*[local-name() = 'businessRuleTask']/@implementation");
						if (taskNodeList.getLength() > 0) {
							Set<String> dmnSet = new TreeSet<>();
							for (int i = 0; i < taskNodeList.getLength(); i++) { 
								if (((org.w3c.dom.Node) taskNodeList.item(i)).getNodeValue().indexOf("dmn") > 0) {
									String dnmName = query.getStringValue("//*[local-name() = 'businessRuleTask']//*[local-name() = 'metaValue']/text()");
									dmnSet.add(dnmName);
								}
		
							}
							if (dmnSet.size() > 0) processInfo.setDmns(dmnSet);
						}
						
						// locate subprocesses //*[local-name() = 'callActivity']/@*[local-name() = 'calledElement']
						NodeList spNodeList = query.getNodeList("//*[local-name() = 'callActivity']/@*[local-name() = 'calledElement']");
						if (spNodeList.getLength() > 0) {
							Set<String> spSet = new TreeSet<>();
							processInfo.setSubprocesses(spSet);
							for (int i = 0; i < spNodeList.getLength(); i++) { 
								spSet.add(((org.w3c.dom.Node) spNodeList.item(i)).getTextContent());
		
							}
						}
						
						// locate soapServices
						// url     //*[local-name() = 'targetRef' and contains(text(), '_EndpointInputX')]/following-sibling::*/*[local-name() = 'from']
						// service //*[local-name() = 'targetRef' and contains(text(), '_InterfaceInputX')]/following-sibling::*/*[local-name() = 'from']
						NodeList soapSvcNodeList = query.getNodeList("//*[local-name() = 'targetRef' and contains(text(), '_InterfaceInputX')]/following-sibling::*/*[local-name() = 'from']");
						NodeList soapUrlNodeList = query.getNodeList("//*[local-name() = 'targetRef' and contains(text(), '_EndpointInputX')]/following-sibling::*/*[local-name() = 'from']");
	
						if (soapSvcNodeList.getLength() > 0) {
							Set<Pair<String, String>> soapSet = new TreeSet<>();
							processInfo.setSoapServices(soapSet);
							for (int i = 0; i < soapSvcNodeList.getLength(); i++) { 
								soapSet.add(Pair.of(((org.w3c.dom.Node) soapSvcNodeList.item(i)).getTextContent(),
								((org.w3c.dom.Node) soapUrlNodeList.item(i)).getTextContent()));
		
							}
						}
						
						// locate rest services
						// method //*[loTreeSetcal-name() = 'targetRef' and contains(text(), '_MethodInputX')]/following-sibling::*/*[local-name() = 'from']
						// service //*[local-name() = 'targetRef' and contains(text(), '_UrlInputX')]/following-sibling::*/*[local-name() = 'from']
						NodeList methodNodeList = query.getNodeList("//*[local-name() = 'targetRef' and contains(text(), '_MethodInputX')]/following-sibling::*/*[local-name() = 'from']");
						NodeList urlNodeList = query.getNodeList("//*[local-name() = 'targetRef' and contains(text(), '_UrlInputX')]/following-sibling::*/*[local-name() = 'from']");
	
						if (methodNodeList.getLength() > 0) {
							Set<Pair<String, String>> restSet = new TreeSet<>();
							processInfo.setRestServices(restSet);
							for (int i = 0; i < methodNodeList.getLength(); i++) { 
								restSet.add(Pair.of(((org.w3c.dom.Node) methodNodeList.item(i)).getTextContent(),
								((org.w3c.dom.Node) urlNodeList.item(i)).getTextContent()));
		
							}
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
				
			}
		Comparator<ProcessInfo> byName = (p1,p2) -> p1.getProcessId().substring(p1.getProcessId().indexOf('.') + 1).compareTo(p2.getProcessId().substring(p1.getProcessId().indexOf('.') + 1));
		procList.sort(byName);
		return procList;
	}
	
	// Extract business rules documentation
	public RuleInfo extractRuleInfo(String repoDir) {
		RuleInfo info = new RuleInfo();
		
		// get DMN
		List<Triple<String, Set<String>, String>> list = extractAssetAndMetadata(repoDir, "dmn", info);
		if (list.size() > 0) info.setDmns(list);
		
		// get guided rules
		list = extractAssetAndMetadata(repoDir, "gdst", info);
		if (list.size() > 0) info.setGuidedRules(list);
		
		// get drools
		list = extractAssetAndMetadata(repoDir, "drl", info);
		if (list.size() > 0) info.setDrools(list);
		
		return info;
	}

}