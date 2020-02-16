package br.com.anteros.remote.synch.configuration;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.anteros.core.scanner.ClassFilter;
import br.com.anteros.core.scanner.ClassPathScanner;
import br.com.anteros.core.utils.ReflectionUtils;
import br.com.anteros.core.utils.StringUtils;
import br.com.anteros.persistence.metadata.EntityCache;
import br.com.anteros.persistence.metadata.EntityListener;
import br.com.anteros.persistence.metadata.annotation.EventType;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.SQLSessionFactory;
import br.com.anteros.remote.synch.annotation.FilterData;
import br.com.anteros.remote.synch.annotation.RemoteSynchMobile;
import br.com.anteros.remote.synch.annotation.RemoteSynchMobileFilterData;
import br.com.anteros.remote.synch.serialization.RealmRemoteSynchSerialize;
import br.com.anteros.remote.synch.serialization.RemoteSynchSerializer;

public class RemoteSynchManager {

	private SQLSessionFactory sessionFactorySQL;
	private Map<String, RemoteEntity> entities = new HashMap<>();
	private String filterDataScanPackage;

	public RemoteSynchManager(SQLSessionFactory sessionFactorySQL, String filterDataScanPackage) {
		this.sessionFactorySQL = sessionFactorySQL;
		this.filterDataScanPackage = filterDataScanPackage;
		init();
	}

	private void init() {
		List<Class<?>> scanClasses = new ArrayList<>();
		if (filterDataScanPackage != null) {
			String[] packages = StringUtils.tokenizeToStringArray(filterDataScanPackage, ", ;");
			scanClasses = ClassPathScanner
					.scanClasses(new ClassFilter().packages(packages).annotation(RemoteSynchMobileFilterData.class));
		}
		
		RemoteDeleteEntityListener deleteListener = new RemoteDeleteEntityListener();
		

		for (EntityCache entityCache : sessionFactorySQL.getEntityCacheManager().getEntities().values()) {
			if (entityCache.getEntityClass().isAnnotationPresent(RemoteSynchMobile.class)) {
				Method method = ReflectionUtils.getMethodByName(deleteListener.getClass(), "preRemove");
				entityCache.getEntityListeners().add(EntityListener.of(deleteListener, method, EventType.PreRemove));
				
				FilterData filterData = null;
				RemoteSynchMobile annRemoteSynch = entityCache.getEntityClass().getAnnotation(RemoteSynchMobile.class);

				for (Class<?> cls : scanClasses) {
					RemoteSynchMobileFilterData annotation = cls.getAnnotation(RemoteSynchMobileFilterData.class);
					if (annotation.name().equals(annRemoteSynch.name())) {
						if (ReflectionUtils.isImplementsInterface(cls, FilterData.class)) {
							try {
								filterData = (FilterData) cls.newInstance();
							} catch (InstantiationException e) {
								e.printStackTrace();
							} catch (IllegalAccessException e) {
								e.printStackTrace();
							}
						}
					}
				}
				entities.put(annRemoteSynch.name(), new RemoteEntity(annRemoteSynch.name(), entityCache, filterData));
			}
		}
	}

	public SQLSession getSession() throws Exception {
		return sessionFactorySQL.getCurrentSession();
	}

	public FilterData lookupFilterData(String name) {
		for (String nm : entities.keySet()) {
			if (nm.equals(name)) {
				return entities.get(nm).getFilterData();
			}
		}
		return null;

	}

	public RemoteSynchSerializer defaultSerializer() {
		return new RealmRemoteSynchSerialize();
	}

}
