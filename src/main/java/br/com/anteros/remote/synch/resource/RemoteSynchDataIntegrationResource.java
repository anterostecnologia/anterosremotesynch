package br.com.anteros.remote.synch.resource;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

import com.fasterxml.jackson.databind.node.ObjectNode;

import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.SQLSessionFactory;
import br.com.anteros.remote.synch.annotation.DataIntegrationFilterData;
import br.com.anteros.remote.synch.annotation.RemoteSynchContext;
import br.com.anteros.remote.synch.configuration.RemoteDataIntegrationEntity;
import br.com.anteros.remote.synch.configuration.RemoteSynchManager;


/**
 * Recurso REST para realizar integração de dados com outros Sistemas.
 * 
 * Os dados devem ser enviados no formato JSON. As entidades que precisam ser sincronizadas estão 
 * no tópico 7. Os dados vão estar no formato plano sem objetos aninhados, seguindo mesma idéia
 * de uma tabela de banco de dados. Onde estiver um campo Entidade Pessoa por exemplo vai estar apenas o CÓDIGO desta pessoa.
 * O campo ID não deverá ser preenchido pois será gerado pela plataforma. O campo CD_?????? [CODE] será 
 * o elo de ligação do ERP com a plataforma. Use neste campo um ID ou Código do ERP.
 * O dados serão enviados para a plataforma segundo sua data/hora de inclusão ou alteração que o ERP deve armazenar.
 * As exclusões deverão ser enviadas no JSON apenas o código da Entidade.
 * 
 *  "Quais entidades serão obrigatórias?"
 *  
 *  As entidades obrigatórias vão depender do nível de utilização da plataforma. Certas funcionalidades
 *  não contratadas ou não utilizadas pelo cliente da plataforma não precisarão ser integradas.
 * 
 * 
 * @author Edson Martins - Relevant Solutions
 *
 */
@RestController
@RequestMapping(value = "/dataIntegrationSynch")
public class RemoteSynchDataIntegrationResource {

	
	@Autowired
	@Qualifier("remoteSynchManager")
	private RemoteSynchManager remoteSynchManager;
	
	@Autowired
	private SQLSessionFactory sessionFactorySQL;

	
	/**
	 * Envia dados para o servidor para integração
	 * @param name Nome da entidade
	 * @param entidade JSON contendo dados da entidade 
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/sendDataIntegration/{name}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	@Transactional(rollbackFor = Throwable.class, propagation = Propagation.REQUIRED, readOnly = true, transactionManager = "transactionManagerSQL")
	public String receiveDataIntegration(@PathVariable(required = true) String name, @RequestBody Collection<? extends Map<String, Object>> payload) {
		RemoteDataIntegrationEntity dataIntegration = remoteSynchManager.lookupDataIntegration(name);
		if (dataIntegration==null) {
			 throw new RemoteSynchException("Entidade "+name+"  não encontrada na lista de entidades para integração.");
		}
		
		
		try {
			if (sessionFactorySQL.getCurrentSession().getTenantId()==null) {
				throw new RemoteSynchException("Informe o id do proprietário no cabeçalho da requisição. Ex: X-Tenant-ID : 20f148d9-8cd1-4042-891b-5f9d2f52e8ac");
			}
			
			remoteSynchManager.updateData(sessionFactorySQL.getCurrentSession(), name,dataIntegration,payload);
		} catch (Exception e) {
			throw new RemoteSynchException(e);
		}
	    return "OK";	
	}
	
	/**
	 * Busca os dados no servidor de integração
	 * @param name Nome da entidade
	 * @param entidade JSON contendo dados da entidade 
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/getDataIntegration/{name}", params = { "dhSynch" })
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	@Transactional(rollbackFor = Throwable.class, propagation = Propagation.REQUIRED, readOnly = true, transactionManager = "transactionManagerSQL")
	public DataIntegrationResultData getDataIntegration(@PathVariable(required = true) String name,
			@RequestParam(required = true) @DateTimeFormat(iso = ISO.DATE_TIME) Date dhSynch) {
		try {
			SQLSession session = sessionFactorySQL.getCurrentSession();
			RemoteSynchContext context = new RemoteSynchContext(session);
			context.addParameter("name",name);
			context.addParameter("dhSynch", dhSynch);
			context.addParameter("tenantId", session.getTenantId());
			context.addParameter("companyId", session.getCompanyId());
			
			DataIntegrationFilterData filterData = remoteSynchManager.lookupDataIntegrationFilterData(name);			
			DataIntegrationResultData resultData = filterData.execute(context);
			return resultData;
		} catch (Exception e) {
			throw new RemoteSynchException(e.getMessage());
		}
	}
	
	/**
	 * Confirma integração dos dados
	 * @param name Nome da entidade
	 * @param entidade JSON contendo dados da entidade 
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/confirmDataIntegration", params = { "dhSynch" })
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	@Transactional(rollbackFor = Throwable.class, propagation = Propagation.REQUIRED, readOnly = true, transactionManager = "transactionManagerSQL")
	public String confirmDataIntegration(@RequestParam(required = true) @DateTimeFormat(iso = ISO.DATE_TIME) Date dhSynch) {
		try {
			SQLSession session = sessionFactorySQL.getCurrentSession();
			RemoteSynchContext context = new RemoteSynchContext(session);
			context.addParameter("dhSynch", dhSynch);
			context.addParameter("tenantId", session.getTenantId());
			context.addParameter("companyId", session.getCompanyId());
			
			remoteSynchManager.confirmDataIntegration(context);			
		} catch (Exception e) {
			throw new RemoteSynchException(e.getMessage());
		}
		return "OK";
	}
	
}
