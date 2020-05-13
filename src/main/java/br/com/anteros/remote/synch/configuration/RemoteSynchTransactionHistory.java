package br.com.anteros.remote.synch.configuration;

import java.io.Serializable;
import java.util.Date;

import br.com.anteros.bean.validation.constraints.Required;
import br.com.anteros.persistence.metadata.annotation.Column;
import br.com.anteros.persistence.metadata.annotation.CompanyId;
import br.com.anteros.persistence.metadata.annotation.CompositeId;
import br.com.anteros.persistence.metadata.annotation.Entity;
import br.com.anteros.persistence.metadata.annotation.Table;
import br.com.anteros.persistence.metadata.annotation.Temporal;
import br.com.anteros.persistence.metadata.annotation.TenantId;
import br.com.anteros.persistence.metadata.annotation.type.TemporalType;
import br.com.anteros.validation.api.constraints.Size;

@Entity
@Table(name = "TRANSACAO_HISTORICO")
public class RemoteSynchTransactionHistory implements Serializable {

	/*
	 * Identificação da transação
	 */
	@CompositeId
	@Column(name = "UUID_TRANSACAO", required = true, label = "ID transação")
	private String id;

	/*
	 * Nome da entidade
	 */
	@CompositeId
	@Column(name = "ENTIDADE", length = 40, required = true)
	private String entity;

	/*
	 * Data/hora da transação
	 */
	@CompositeId
	@Column(name = "DH_TRANSACAO", required = true)
	@Temporal(TemporalType.DATE_TIME)
	private Date dhTransaction;

	/*
	 * Número de registros enviados
	 */
	@Column(name = "NR_REGISTROS", precision = 4)
	private Long numberOfRecords;

	/*
	 * UUID do equipamento
	 */
	@Column(name = "UUID_EQUIPAMENTO", length = 40, required = true)
	private String equipament;

	/*
	 * UUID do Owner - proprietário do registro no banco de dados
	 */
	@Required
	@Size(max = 40)
	@TenantId
	@Column(name = "ID_OWNER", required = true, length = 40, label = "Proprietário do banco de dados")
	private String owner;

	/*
	 * Empresa
	 */
	@Required
	@CompanyId
	@Column(name = "ID_EMPRESA", required = true, precision = 8, label = "Empresa")
	private Long company;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getEntity() {
		return entity;
	}

	public void setEntity(String entity) {
		this.entity = entity;
	}

	public Date getDhTransaction() {
		return dhTransaction;
	}

	public void setDhTransaction(Date dhTransaction) {
		this.dhTransaction = dhTransaction;
	}

	public Long getNumberOfRecords() {
		return numberOfRecords;
	}

	public void setNumberOfRecords(Long numberOfRecords) {
		this.numberOfRecords = numberOfRecords;
	}

	public String getEquipament() {
		return equipament;
	}

	public void setEquipament(String equipament) {
		this.equipament = equipament;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public Long getCompany() {
		return company;
	}

	public void setCompany(Long company) {
		this.company = company;
	}
}
