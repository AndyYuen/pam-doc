package com.redhat.pamdoc.model;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.tuple.Pair;

public class ProcessInfo {
	private String metadataHtml;
	private String imageName;
	private String processId;
	private List<Pair<String, String>> variables;
	private Set<String> ruleflows;
	private Set<String> dmns;
	private Set<String> subprocesses;
	private Set<Pair<String, String>> soapServices;
	private Set<Pair<String, String>> restServices;


	
	public String getMetadataHtml() {
		return metadataHtml;
	}



	public void setMetadataHtml(String metadataHtml) {
		this.metadataHtml = metadataHtml;
	}



	public String getImageName() {
		return imageName;
	}



	public void setImageName(String imageName) {
		this.imageName = imageName;
	}



	public String getProcessId() {
		return processId;
	}



	public void setProcessId(String processId) {
		this.processId = processId;
	}



	public List<Pair<String, String>> getVariables() {
		return variables;
	}



	public void setVariables(List<Pair<String, String>> variables) {
		this.variables = variables;
	}



	public Set<String> getRuleflows() {
		return ruleflows;
	}



	public void setRuleflows(Set<String> ruleflows) {
		this.ruleflows = ruleflows;
	}



	public Set<String> getDmns() {
		return dmns;
	}



	public void setDmns(Set<String> dmns) {
		this.dmns = dmns;
	}



	public Set<String> getSubprocesses() {
		return subprocesses;
	}



	public void setSubprocesses(Set<String> subprocesses) {
		this.subprocesses = subprocesses;
	}



	public Set<Pair<String, String>> getSoapServices() {
		return soapServices;
	}



	public void setSoapServices(Set<Pair<String, String>> soapServices) {
		this.soapServices = soapServices;
	}



	public Set<Pair<String, String>> getRestServices() {
		return restServices;
	}



	public void setRestServices(Set<Pair<String, String>> restServices) {
		this.restServices = restServices;
	}



	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}
	
}
