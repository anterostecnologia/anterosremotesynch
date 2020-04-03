package br.com.anteros.remote.synch.resource;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.SQLSessionFactory;
import br.com.anteros.remote.synch.annotation.MobileFilterData;
import br.com.anteros.remote.synch.annotation.RemoteSynchContext;
import br.com.anteros.remote.synch.configuration.RemoteSynchManager;


/**
 * Recurso REST para realizar sincronização de dados com dispositivos móveis.
 * @author Edson Martins - Relevant Solutions
 *
 */
@RestController
@RequestMapping(value = "/mobileSynch")
public class RemoteSynchMobileResource {

	
	@Autowired
	@Qualifier("remoteSynchManager")
	private RemoteSynchManager remoteSynchManager;
	
	@Autowired
	private SQLSessionFactory sessionFactorySQL;

	
	/**
	 * Recebe dados do servidor para sincronização no dispositivo móvel
	 * @param name Nome da entidade
	 * @param dhSynch Data/hora do último sincronismo
	 * @param clientId ID do equipamento
	 * @return JSON no formato Realm
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/receiveMobileData/{name}", params = { "dhSynch","clientId" })
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	@Transactional(rollbackFor = Throwable.class, propagation = Propagation.REQUIRED, readOnly = true, transactionManager = "transactionManagerSQL")
	public ObjectNode receiveMobileData(@PathVariable(required = true) String name,
			@RequestParam(required = true) @DateTimeFormat(iso = ISO.DATE_TIME) Date dhSynch, @RequestParam(required = true) String clientId) {
		
		try {
			SQLSession session = sessionFactorySQL.getCurrentSession();
			RemoteSynchContext context = new RemoteSynchContext(session);
			context.addParameter("name",name);
			context.addParameter("dhSynch", dhSynch);
			context.addParameter("clientId", clientId);
			context.addParameter("tenantId", session.getTenantId());
			context.addParameter("companyId", session.getCompanyId());
			
			MobileFilterData filterData = remoteSynchManager.lookupFilterData(name);			
			ResultData resultData = filterData.execute(context);			
			ObjectNode result = remoteSynchManager.defaultSerializer().serialize(resultData, session, resultData.getResultClass());
			return result;
		} catch (Exception e) {
			throw new RemoteSynchException(e.getMessage());
		}
	}
	
	
	/**
	 * Inicia transação para envio de dados para o servidor para sincronização 
	 * @param tnsID Número da transação para controle
	 * @param clientId ID do equipamento
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/startTransactionMobileData", params = { "tnsID","clientId" })
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	@Transactional(rollbackFor = Throwable.class, propagation = Propagation.REQUIRED, readOnly = true, transactionManager = "transactionManagerSQL")
	public void startTransactionMobileData(@RequestParam(required = true) String tnsID, @RequestParam(required = true) String clientId) {
		remoteSynchManager.startTransaction(clientId,tnsID);
	}
	

	/**
	 * Envia dados para o servidor para sincronização 
	 * @param name Nome da entidade
	 * @param tnsID Número da transação para controle
	 * @param clientId ID do equipamento
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/sendMobileData/{name}", params = { "tnsID","clientId" })
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	@Transactional(rollbackFor = Throwable.class, propagation = Propagation.REQUIRED, readOnly = true, transactionManager = "transactionManagerSQL")
	public void sendMobileData(@PathVariable(required = true) String name,
			@RequestParam(required = true) String tnsID, @RequestParam(required = true) String clientId, @RequestBody JsonNode jsonNode) {
		remoteSynchManager.enqueue(clientId, tnsID, name, jsonNode);
	}
	
	/**
	 * Finaliza transação e processa dados enviados para o servidor para sincronização 
	 * @param tnsID Número da transação para controle
	 * @param clientId ID do equipamento
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/finishTransactionMobileData", params = { "tnsID","clientId" })
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	@Transactional(rollbackFor = Throwable.class, propagation = Propagation.REQUIRED, readOnly = true, transactionManager = "transactionManagerSQL")
	public void finishTransactionMobileData(@RequestParam(required = true) String tnsID, @RequestParam(required = true) String clientId) {
		try {
			SQLSession session = sessionFactorySQL.getCurrentSession();
			remoteSynchManager.finishTransaction(session,clientId,tnsID);
		} catch (Exception e) {
			throw new RemoteSynchException(e);
		}	
	}
	
	/**
	 * Verifica se a transação foi processada 
	 * @param tnsID Número da transação para controle
	 * @param clientId ID do equipamento
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/checkTransactionMobileData", params = { "tnsID","clientId" })
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	@Transactional(rollbackFor = Throwable.class, propagation = Propagation.REQUIRED, readOnly = true, transactionManager = "transactionManagerSQL")
	public Boolean checkTransactionMobileData(@RequestParam(required = true) String tnsID, @RequestParam(required = true) String clientId) {
		try {
			SQLSession session = sessionFactorySQL.getCurrentSession();
			return remoteSynchManager.checkTransaction(session,clientId,tnsID);
		} catch (Exception e) {
			throw new RemoteSynchException(e);
		}		
	}


}
