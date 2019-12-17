package br.com.anteros.remote.synch.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.MultiValueMap;

import br.com.anteros.persistence.session.SQLSessionFactory;
import br.com.anteros.remote.synch.annotation.EnableRemoteSynch;

@Configuration
@ComponentScan(basePackages = {"br.com.anteros.remote.synch.service"})
public class RemoteSynchConfiguration implements ImportAware {

	@Autowired
	@Qualifier("sessionFactorySQL")
	SQLSessionFactory sessionFactorySQL;
	
	private String filterDataScanPackage;


	@Bean
	@Qualifier("remoteSynchManager")
	public RemoteSynchManager getRemoteSynchManager() {
		return new RemoteSynchManager(sessionFactorySQL, filterDataScanPackage);
	}


	@Override
	public void setImportMetadata(AnnotationMetadata importMetadata) {
		MultiValueMap<String,Object> attributes = importMetadata.getAllAnnotationAttributes(EnableRemoteSynch.class.getName());
		this.filterDataScanPackage = (String) attributes.getFirst("filterDataScanPackage");
	}

}
