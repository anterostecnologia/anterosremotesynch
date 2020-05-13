package br.com.anteros.remote.synch.resource;

public class TransactionHistoryData {

	private String entity;
	private int numberOfRecords;
	private int companyId;
	private String companyCode;	
	
	public TransactionHistoryData(String entity, int numberOfRecords, int companyId, String companyCode) {
		super();
		this.entity = entity;
		this.numberOfRecords = numberOfRecords;
		this.companyId = companyId;
		this.companyCode = companyCode;
	}
	
	public String getEntity() {
		return entity;
	}
	
	public void setEntity(String entity) {
		this.entity = entity;
	}
	
	public int getNumberOfRecords() {
		return numberOfRecords;
	}
	
	public void setNumberOfRecords(int numberOfRecords) {
		this.numberOfRecords = numberOfRecords;
	}

	public int getCompanyId() {
		return companyId;
	}

	public void setCompanyId(int companyId) {
		this.companyId = companyId;
	}

	public String getCompanyCode() {
		return companyCode;
	}

	public void setCompanyCode(String companyCode) {
		this.companyCode = companyCode;
	}	

}
