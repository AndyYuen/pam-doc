package com.redhat.pamdoc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.pamdoc.model.ProcessInfo;
import com.redhat.pamdoc.model.ProjectInfo;
import com.redhat.pamdoc.model.ProjectParams;
import com.redhat.pamdoc.model.RuleInfo;
import com.redhat.pamdoc.model.DataModelInfo;
import com.redhat.pamdoc.service.DocoService;
import com.redhat.pamdoc.utils.JsonDocUtils;


@Controller
public class PamDocController {
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	private static final String MY_SESSION_NAME = "MY_SESSION_PARAMS";

	@Value("${app.git.url}")
	private String httpUri;

	@Value("${app.git.target.dir}")
	private String targetDir;

	@Value("${app.git.username}")
	private String username;

	@Value("${app.git.password}")
	private String password;
	
	@Autowired
	private DocoService service;
	
	@Autowired
	ServletContext servletContext;
	
	@GetMapping("/")
	// GET operation to display project page
	public String showForm(Model model, final HttpSession session) {
		ProjectParams params = (ProjectParams) session.getAttribute(MY_SESSION_NAME);
		if (params == null) {
			params = new ProjectParams();
			params.setOption("documentation");
		}
		model.addAttribute("params", params);
		log.info("GET params\n" + params);
		
		return "index";
	}

	@PostMapping("/")
	// GET operation to display PAM project documentation page
	public String showForm(@Valid @ModelAttribute("params") ProjectParams params, 
			BindingResult bindingResult, Model model, 
			final HttpServletRequest request) throws IllegalStateException, GitAPIException, IOException, URISyntaxException {

		log.info("params=\n" + params);
		
	    if (bindingResult.hasErrors()) {       
	        return "index";
	    }
	    
		String projectPath = params.getSpaceName() + "/" + params.getProjectName();
		log.info("project Path: " + projectPath);
		model.addAttribute("projectPath", projectPath);
		
		// clone PAM GIT repo
		String repoDir = targetDir + UUID.randomUUID();
		
//	   try {

		// the following does not work when using self-signed certs using https
/*		
		  Git.cloneRepository() 
		    .setURI(httpUri + projectPath)
		  	.setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password)) 
		  	.setDirectory(new File(repoDir)) .call(); 
*/		  
//	   } catch (Exception e) {
//		  log.error(e.getMessage()); 
//	   }
		
		// more complex way to clone PAM repo when running on Openshift due to self-signed certs 
		Git git = Git.init()
			.setDirectory(new File(repoDir))
			.call();
		StoredConfig config = git.getRepository().getConfig();
		config.setBoolean("http", null, "sslVerify", false);
		config.save();
		git.remoteAdd()
		    .setName("origin")
		    .setUri(new URIish(StringUtils.appendIfMissing(httpUri, "/", "/") + projectPath))
		    .call();
	    git.pull().setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password))
	    	.setRemote("origin")
	    	.setRemoteBranchName("master")
	    	.call();
		 

		log.info("repoDir=" + repoDir + ", repoUrl=" + httpUri + projectPath);
		
		// get partial project info
		List<Pair<String, Integer>> assetCounts = new ArrayList<>();
		
		ProjectInfo projInfo = service.extractProjectInfo(repoDir, projectPath);
		log.info("projInfo=" + projInfo.toString());		
		model.addAttribute("projectInfo", projInfo);
		
		// get process info
		List<ProcessInfo> procList = service.extractBusinessProcessesInfo(repoDir, servletContext.getRealPath("/static/img"));		
		//log.info("procList=" + procList.toString());		
		model.addAttribute("procList", procList);
		model.addAttribute("processJson", JsonDocUtils.createBusinessProcessJson(procList, projectPath));
		// extract process asset counts
		if (procList.size() > 0) {
			assetCounts.add(Pair.of("Business Process", procList.size()));
		}
		Set<String> soap = new TreeSet<>();
		Set<String> rest = new TreeSet<>();
		for (ProcessInfo info : procList) {
			if ((info.getSoapServices() != null) && (info.getSoapServices().size() > 0)) {
				for (Pair<String, String> service : info.getSoapServices()) {
						soap.add(service.getRight());
				}
			}
			if ((info.getRestServices() != null) && (info.getRestServices().size() > 0)) {
				for (Pair<String, String> service : info.getRestServices()) {
						rest.add(service.getRight());
				}
			}
		}
		if (soap.size() > 0) assetCounts.add(Pair.of("Soap Web Service", soap.size()));
		if (rest.size() > 0) assetCounts.add(Pair.of("REST Web Service", rest.size()));
		
		
		// get data model info
		List<DataModelInfo> dmList = service.extractDataModelInfo(repoDir);
		log.info("dmInfo=" + dmList.toString());		
		model.addAttribute("dataModelList", dmList);

		// extract data model counts
		if (dmList.size() > 0) {
			assetCounts.add(Pair.of("Data Model", dmList.size()));
		}
		
		// get rules info
		RuleInfo ruleInfo = service.extractRuleInfo(repoDir);
		log.info("RuleInfo=" + ruleInfo.toString());		
		model.addAttribute("ruleInfo", ruleInfo);

		// extract rules counts
		if ((ruleInfo.getDmns() != null) && (ruleInfo.getDmns().size() > 0)) {
			assetCounts.add(Pair.of("DMN", ruleInfo.getDmns().size()));
		}
		if ((ruleInfo.getGuidedRules() != null) && (ruleInfo.getGuidedRules().size() > 0)) {
			assetCounts.add(Pair.of("Guided Decision Table", ruleInfo.getGuidedRules().size()));
		}
		if ((ruleInfo.getDrools() != null) && (ruleInfo.getDrools().size()) > 0) {
			assetCounts.add(Pair.of("DRL", ruleInfo.getDrools().size()));
		}
		
		// augment project asset counts
		assetCounts.sort((Pair<String, Integer> p1, Pair<String, Integer> p2) -> p1.getLeft().compareTo(p2.getLeft()));
		projInfo.setAssetCounts(assetCounts);
		model.addAttribute("projectJson", JsonDocUtils.createProjectSummaryJson(projInfo, procList, ruleInfo, dmList));
		
		// remove cloned repo
		 try {
		 	FileUtils.deleteDirectory(new File(repoDir));
		 } catch (IOException e) {
		 	log.error(e.getMessage());
		 }
		 
		 request.getSession().setAttribute(MY_SESSION_NAME, params);
		 
		return ((params.getOption().equals("jsonDoc"))? "jsondoc": "doco");
	}

}
