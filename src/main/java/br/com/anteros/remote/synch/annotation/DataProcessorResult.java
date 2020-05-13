package br.com.anteros.remote.synch.annotation;

public class DataProcessorResult {
	
	private String companyCode;

	public String getCompanyCode() {
		return companyCode;
	}

	public DataProcessorResult(String companyCode) {
		super();
		this.companyCode = companyCode;
	}

	public void setCompanyCode(String companyCode) {
		this.companyCode = companyCode;
	}
	
}
