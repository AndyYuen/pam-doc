package com.redhat.pamdoc.model;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.tuple.Triple;

public class RuleInfo {

	private List<Triple<String, Set<String>, String>> dmns;
	private List<Triple<String, Set<String>, String>> guidedRules;
	private List<Triple<String, Set<String>, String>> drools;

	
	
	public List<Triple<String, Set<String>, String>> getDmns() {
		return dmns;
	}



	public void setDmns(List<Triple<String, Set<String>, String>> dmns) {
		this.dmns = dmns;
	}



	public List<Triple<String, Set<String>, String>> getGuidedRules() {
		return guidedRules;
	}



	public void setGuidedRules(List<Triple<String, Set<String>, String>> guidedRules) {
		this.guidedRules = guidedRules;
	}



	public List<Triple<String, Set<String>, String>> getDrools() {
		return drools;
	}



	public void setDrools(List<Triple<String, Set<String>, String>> drools) {
		this.drools = drools;
	}



	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}
	
}
