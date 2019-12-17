package br.com.anteros.remote.synch.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.node.ObjectNode;

import br.com.anteros.remote.synch.annotation.FilterData;
import br.com.anteros.remote.synch.annotation.RemoteSynchContext;
import br.com.anteros.remote.synch.configuration.RemoteSynchManager;

@RestController
@RequestMapping(value = "/synch")
public class RemoteSynchService {

	
	@Autowired
	@Qualifier("remoteSynchManager")
	private RemoteSynchManager remoteSynchManager;

	@RequestMapping(method = RequestMethod.GET, value = "/import/{name}", params = { "dhSynch","clientId" })
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	@Transactional(rollbackFor = Throwable.class, propagation = Propagation.REQUIRED, readOnly = true, transactionManager = "transactionManagerSQL")
	public ObjectNode importData(@PathVariable(required = true) String name,
			@RequestParam(required = true) @DateTimeFormat(iso = ISO.DATE_TIME) Date dhSynch, @RequestParam(required = true) String clientId) {
		try {
			RemoteSynchContext context = new RemoteSynchContext(remoteSynchManager.getSession());
			context.addParameter("name",name);
			context.addParameter("dhSynch", dhSynch);
			context.addParameter("clientId", clientId);
			context.addParameter("tenantId", remoteSynchManager.getSession().getTenantId());
			context.addParameter("companyId", remoteSynchManager.getSession().getCompanyId());
			
			FilterData filterData = remoteSynchManager.lookupFilterData(name);			
			ResultData resultData = filterData.execute(context);			
			ObjectNode result = remoteSynchManager.defaultSerializer().serialize(resultData, remoteSynchManager.getSession(), resultData.getResultClass());
			return result;
		} catch (Exception e) {
			new RemoteSynchException(e.getMessage());
		}
		return null;		
	}


	
	public void exportData() {

	}

}
