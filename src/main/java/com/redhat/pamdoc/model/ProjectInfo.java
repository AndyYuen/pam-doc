package com.redhat.pamdoc.model;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.tuple.Pair;

public class ProjectInfo {
	
	private String name;
	private List<Pair<String, String>> workItemHandlers;
	private List<Pair<String, Integer>> assetCounts;

	

	public String getName() {
		return name;
	}



	public void setName(String name) {
		this.name = name;
	}



	public List<Pair<String, String>> getWorkItemHandlers() {
		return workItemHandlers;
	}



	public void setWorkItemHandlers(List<Pair<String, String>> workItemHandlers) {
		this.workItemHandlers = workItemHandlers;
	}



	public List<Pair<String, Integer>> getAssetCounts() {
		return assetCounts;
	}



	public void setAssetCounts(List<Pair<String, Integer>> assetCounts) {
		this.assetCounts = assetCounts;
	}



	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}

}
