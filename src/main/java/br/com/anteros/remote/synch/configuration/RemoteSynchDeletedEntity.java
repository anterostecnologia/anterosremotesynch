package br.com.anteros.remote.synch.configuration;

import java.io.Serializable;
import java.util.Date;

import br.com.anteros.bean.validation.constraints.Required;
import br.com.anteros.persistence.metadata.annotation.Column;
import br.com.anteros.persistence.metadata.annotation.CompanyId;
import br.com.anteros.persistence.metadata.annotation.Entity;
import br.com.anteros.persistence.metadata.annotation.ForeignKey;
import br.com.anteros.persistence.metadata.annotation.GeneratedValue;
import br.com.anteros.persistence.metadata.annotation.Id;
import br.com.anteros.persistence.metadata.annotation.SequenceGenerator;
import br.com.anteros.persistence.metadata.annotation.Table;
import br.com.anteros.persistence.metadata.annotation.Temporal;
import br.com.anteros.persistence.metadata.annotation.TenantId;
import br.com.anteros.persistence.metadata.annotation.type.GeneratedType;
import br.com.anteros.persistence.metadata.annotation.type.TemporalType;
import br.com.anteros.validation.api.constraints.Size;

@Entity
@Table(name="ENTIDADES_REMOVIDAS")
public class RemoteSynchDeletedEntity implements Serializable {
	
	/*
	 * Identificação do contrato
	 */
	@Id
	@Column(name = "ID_CONTRATO", required = true, label = "ID contrato")
	@GeneratedValue(strategy = GeneratedType.AUTO)
	@SequenceGenerator(sequenceName = "SEQCONTRATO", initialValue = 1)
	private Long id;

	/*
	 * UUID do Owner - proprietário do registro no banco de dados
	 */
	@Required
	@Size (max = 40)
	@TenantId
	@Column(name = "ID_OWNER", required = true, length = 40, label = "Proprietário do banco de dados")
	private String owner;

	/*
	 * Empresa 
	 */
	@Required
	@CompanyId
	@Column(name = "ID_EMPRESA", required = true, precision = 8, label = "Empresa")
	private Long empresa;
	
	@Column(name="NOME_ENTIDADE", length = 200, required = true)
	private String entityName;	
		
	@Column(name="ID_ENTIDADE", length = 300, required = true)
	private String entityID;
	
	@Temporal(TemporalType.DATE_TIME)
	@Column(name="DH_ENTIDADEREMOVIDA", required = true)
	private Date dhEntidadeRemovida;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public Long getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Long empresa) {
		this.empresa = empresa;
	}

	public String getEntityName() {
		return entityName;
	}

	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	public String getEntityID() {
		return entityID;
	}

	public void setEntityID(String entityID) {
		this.entityID = entityID;
	}

	public Date getDhEntidadeRemovida() {
		return dhEntidadeRemovida;
	}

	public void setDhEntidadeRemovida(Date dhEntidadeRemovida) {
		this.dhEntidadeRemovida = dhEntidadeRemovida;
	}

}
