package com.redhat.pamdoc.model;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public class DataModelInfo {
	
	private String packageName;
	private String className;
	
	public DataModelInfo(String packageName, String className) {
		this.packageName = packageName;
		this.className = className;
	}
	
	public String getPackageName() {
		return packageName;
	}



	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}



	public String getClassName() {
		return className;
	}



	public void setClassName(String className) {
		this.className = className;
	}



	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}

}
