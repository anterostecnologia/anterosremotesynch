package br.com.anteros.remote.synch.resource;

import java.util.List;

import br.com.anteros.collections.queue.file.AnterosFileQueueItem;

public class TransactionInfo {
	
	private String tenantId;
	
	private String companyId;
	
	private List<TransactionHistoryData> data;
	
	public TransactionInfo() {
		
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public List<TransactionHistoryData> getData() {
		return data;
	}

	public void setData(List<TransactionHistoryData> data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return tenantId;
	}

}
