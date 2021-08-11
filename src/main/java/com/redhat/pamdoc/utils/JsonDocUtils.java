package com.redhat.pamdoc.utils;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.pamdoc.model.DataModelInfo;
import com.redhat.pamdoc.model.ProcessInfo;
import com.redhat.pamdoc.model.ProjectInfo;
import com.redhat.pamdoc.model.RuleInfo;

public class JsonDocUtils {
	
	private static final Logger log = LoggerFactory.getLogger(JsonDocUtils.class);

	private static final String INDEX = "{\"index\":{\"_id\":\"%s\"}}\n";
	
	static public String getCurrentDateString() {
		DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;

		return formatter.format(ZonedDateTime.now());
	}
	
	static private JSONArray processRules(List<Triple<String, Set<String>, String>> list) {
		if (list == null) return null;

		JSONArray ja = new JSONArray();
		for (Triple<String, Set<String>, String> triple : list) {
			
			if (triple.getMiddle() != null) {
				JSONArray ruleflowJa = new JSONArray();

				for (String ruleflowName : triple.getMiddle()) {
					JSONObject jo = new JSONObject();
					jo.put("name", ruleflowName);
					ruleflowJa.put(jo);
				}
				JSONObject jo = new JSONObject();
				jo.put("ruleflowGroups", ruleflowJa);
				jo.put("name", triple.getLeft());
				ja.put(jo);
			} else {
				JSONObject jo = new JSONObject();
				jo.put("name", triple.getLeft());
				ja.put(jo);
			}
		}

		return ja;
	}

	
	static public String createBusinessProcessJson(List<ProcessInfo> list, String projectPath) {
		if (list == null) return null;
		// create project json
		StringBuffer json = new StringBuffer();

		for (ProcessInfo info : list) {
			JSONObject rootJo = new JSONObject();
			//rootJo.put("_id", projectPath + "-" + info.getProcessId());
			rootJo.put("@timestamp", getCurrentDateString());
			rootJo.put("processName", info.getProcessId());
			rootJo.put("projectName", projectPath);
			
			// extract variables
			if (info.getVariables() != null) {
				JSONArray ja = new JSONArray();
				for (Pair<String, String> pair : info.getVariables()) {
					JSONObject jo = new JSONObject();
					jo.put("name", pair.getLeft());
					jo.put("class", pair.getRight());
					ja.put(jo);
				}
				rootJo.put("variables", ja);
			}
			
			// ruleflows called
			if (info.getRuleflows() != null) {
				JSONArray ja = new JSONArray();
				for (String name : info.getRuleflows()) {
					JSONObject jo = new JSONObject();
					jo.put("name", name);
					ja.put(jo);
				}
				rootJo.put("ruleflowGroups", ja);
			}
			
			// dmns called
			if (info.getDmns() != null) {
				JSONArray ja = new JSONArray();
				for (String name : info.getDmns()) {
					JSONObject jo = new JSONObject();
					jo.put("name", name);
					ja.put(jo);
				}
				rootJo.put("dmns", ja);
			}
			
			
			// subprocesses called
			if (info.getSubprocesses() != null) {
				JSONArray ja = new JSONArray();
				for (String name : info.getSubprocesses()) {				
					JSONObject jo = new JSONObject();
					jo.put("name", name);
					ja.put(jo);
				}
				rootJo.put("subprocesses", ja);
			}
			
			// soap services called
			if (info.getSoapServices() != null) {
				JSONArray ja = new JSONArray();
				for (Pair<String, String> pair : info.getSoapServices()) {
					JSONObject jo = new JSONObject();
					jo.put("operation", pair.getLeft());
					jo.put("url", pair.getRight());
					ja.put(jo);
				}
				rootJo.put("soapServices", ja);
			}

			// rest services called
			if (info.getRestServices() != null) {
				JSONArray ja = new JSONArray();
				for (Pair<String, String> pair : info.getRestServices()) {
					JSONObject jo = new JSONObject();
					jo.put("method", pair.getLeft());
					jo.put("url", pair.getRight());
					ja.put(jo);
				}
				rootJo.put("restServices", ja);
			}
			json.append(String.format(INDEX, projectPath + "-" + info.getProcessId()));
			json.append(rootJo.toString());
			json.append('\n');

		}
		
		log.info("************ Business Process json:\n" + json.toString());
		return json.toString();
	}

	static public String createProjectSummaryJson(ProjectInfo projInfo, 
			List<ProcessInfo> procList, RuleInfo ruleInfo,
			List<DataModelInfo> dmList) {
		
		// create project summary json
		// First: project info
		JSONObject rootJo = new JSONObject();
		//rootJo.put("_id", projInfo.getName());
		rootJo.put("@timestamp", getCurrentDateString());
		rootJo.put("projectName", projInfo.getName());		
		if (projInfo.getAssetCounts() != null) {
			JSONArray ja = new JSONArray();
			for (Pair<String, Integer> pair : projInfo.getAssetCounts()) {
				JSONObject jo = new JSONObject();
				jo.put("name", pair.getLeft());
				jo.put("count", pair.getRight());
				ja.put(jo);
			}
			rootJo.put("assetCounts", ja);
		}
		
		if (projInfo.getWorkItemHandlers() != null) {
			JSONArray ja = new JSONArray();
			for (Pair<String, String> pair : projInfo.getWorkItemHandlers()) {
				JSONObject jo = new JSONObject();
				jo.put("name", pair.getLeft());
				jo.put("constructor", pair.getRight());
				ja.put(jo);
			}
			rootJo.put("workItemHandlers", ja);
		}
		
		// Data model info
		if (dmList != null) {
			JSONArray ja = new JSONArray();
			for (DataModelInfo info : dmList) {
				JSONObject jo = new JSONObject();
				jo.put("name", info.getClassName());
				jo.put("package", info.getPackageName());
				ja.put(jo);
			}
			rootJo.put("dataModels", ja);
		}
		
		// business process info
		if (procList != null) {
			JSONArray ja = new JSONArray();
			for (ProcessInfo info : procList) {
				JSONObject jo = new JSONObject();
				jo.put("name", info.getProcessId());
				ja.put(jo);
			}
			rootJo.put("processes", ja);
		}	
		// rules info
		if (ruleInfo != null) {
			JSONArray dmn = processRules(ruleInfo.getDmns());
			if (dmn != null) rootJo.put("dmns", dmn);
			JSONArray drools = processRules(ruleInfo.getDrools());
			if (drools != null) rootJo.put("drls", drools);
			JSONArray guidedRule = processRules(ruleInfo.getGuidedRules());
			if (guidedRule != null) rootJo.put("guidedRules", guidedRule);
		}
		
		String result = String.format(INDEX, projInfo.getName()) + rootJo.toString();
		
		log.info("************ Project Summary json:\n" + result);
		return result;
	}

}
