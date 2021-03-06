package br.com.anteros.remote.synch.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = {ElementType.TYPE, ElementType.FIELD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface RemoteSynchDataIntegration {

	String name();
	
	String description();
	
	DataSynchDirection[] direction() default {DataSynchDirection.SEND};

}
