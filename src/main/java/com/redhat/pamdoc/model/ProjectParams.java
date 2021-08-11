package com.redhat.pamdoc.model;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import javax.validation.constraints.NotEmpty;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;

public class ProjectParams implements Serializable {
	
	@NotEmpty
	@NotBlank
	private String spaceName;
	
	@NotEmpty
	@NotBlank
	private String projectName;
	
	@NotEmpty
	@NotBlank
	private String option;
	

	public String getSpaceName() {
		return spaceName;
	}



	public void setSpaceName(String spaceName) {
		this.spaceName = spaceName;
	}



	public String getProjectName() {
		return projectName;
	}



	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}



	public String getOption() {
		return option;
	}



	public void setOption(String option) {
		this.option = option;
	}



	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}
}
