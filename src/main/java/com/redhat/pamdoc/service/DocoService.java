package com.redhat.pamdoc.service;


import java.util.List;
import java.util.Set;

import com.redhat.pamdoc.model.DataModelInfo;
import com.redhat.pamdoc.model.ProcessInfo;
import com.redhat.pamdoc.model.ProjectInfo;
import com.redhat.pamdoc.model.RuleInfo;


public interface DocoService {

	// Extract project documentation
	public ProjectInfo extractProjectInfo(String repoDir, String name);
	
	// Extract data model documentation
	public List<DataModelInfo> extractDataModelInfo(String repoDir);
	
	// Extract business rules documentation
	public RuleInfo extractRuleInfo(String repoDir);
	
	// Extract business process documentation
	public List<ProcessInfo> extractBusinessProcessesInfo(String repoDir, String realPath);
	

}