package br.com.anteros.remote.synch.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import br.com.anteros.remote.synch.configuration.RemoteSynchConfiguration;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(RemoteSynchConfiguration.class)
@Documented
public @interface EnableRemoteSynch {
	String filterAndProcessorDataScanPackage();
}
