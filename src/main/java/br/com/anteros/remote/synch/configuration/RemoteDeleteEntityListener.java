package br.com.anteros.remote.synch.configuration;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import br.com.anteros.persistence.metadata.EntityCache;
import br.com.anteros.persistence.metadata.annotation.PreRemove;
import br.com.anteros.persistence.metadata.descriptor.DescriptionField;
import br.com.anteros.persistence.metadata.identifier.Identifier;
import br.com.anteros.persistence.serialization.jackson.AnterosObjectMapper;
import br.com.anteros.persistence.session.SQLSessionFactory;
import br.com.anteros.remote.synch.annotation.RemoteSynchMobile;

public class RemoteDeleteEntityListener {

	@Autowired
	private SQLSessionFactory sessionFactorySQL;

	public static DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	public static DateFormat dft = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

	@PreRemove
	public void preRemove(Object oldObject) throws Exception {
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

		Identifier<Object> identifier = sessionFactorySQL.getCurrentSession().getIdentifier(oldObject);
		Map<DescriptionField, Object> fieldsValues = identifier.getFieldsValues();
		AnterosObjectMapper mapper = new AnterosObjectMapper(sessionFactorySQL);
		ObjectNode nodeValue = convertToNodeValue(mapper, fieldsValues);
		
		Long company = null;
		if (sessionFactorySQL.getCurrentSession().getCompanyId() != null) {
			company = Long.valueOf(sessionFactorySQL.getCurrentSession().getCompanyId().toString());
		}

		RemoteSynchDeletedEntity entity = new RemoteSynchDeletedEntity();
		entity.setDhEntidadeRemovida(new Date());
		entity.setEmpresa(company);
		entity.setOwner(sessionFactorySQL.getCurrentSession().getTenantId().toString());
		entity.setEntityID(nodeValue.toString());
		EntityCache entityCache = sessionFactorySQL.getEntityCacheManager().getEntityCache(oldObject.getClass());
		RemoteSynchMobile annRemoteSynch = entityCache.getEntityClass().getAnnotation(RemoteSynchMobile.class);
		entity.setEntityName(annRemoteSynch.name());
		sessionFactorySQL.getCurrentSession().save(entity);

	}

	private ObjectNode convertToNodeValue(ObjectMapper mapper, Map<DescriptionField, Object> fieldsValues) {
		ObjectNode result = mapper.createObjectNode();
		for (DescriptionField descriptionField : fieldsValues.keySet()) {
			Object value = fieldsValues.get(descriptionField);
			if (value instanceof Map) {
				result.put(descriptionField.getField().getName(),
						convertToNodeValue(mapper, (Map<DescriptionField, Object>) value));
			} else {
				putValue(result, descriptionField, value);
			}
		}
		return result;
	}

	protected void putValue(ObjectNode node, DescriptionField descriptionField, Object value) {
		if (value == null) {
			node.putNull(descriptionField.getField().getName());
			return;
		}
		if (value instanceof String) {
			node.put(descriptionField.getField().getName(), (String) (value));
		} else if (value instanceof Integer) {
			node.put(descriptionField.getField().getName(), (Integer) (value));
		} else if (value instanceof Double) {
			node.put(descriptionField.getField().getName(), (Double) (value));
		} else if (value instanceof BigDecimal) {
			node.put(descriptionField.getField().getName(), (BigDecimal) (value));
		} else if (value instanceof Boolean) {
			node.put(descriptionField.getField().getName(), (Boolean) (value));
		} else if (value instanceof Long) {
			node.put(descriptionField.getField().getName(), (Long) (value));
		} else if (value instanceof BigInteger) {
			node.put(descriptionField.getField().getName(), ((BigInteger) (value)).intValue());
		} else if (value instanceof Date) {
			if (descriptionField.isTemporalDate()) {
				node.put(descriptionField.getField().getName(), df.format(((Date) (value))));
			} else {
				node.put(descriptionField.getField().getName(), dft.format(((Date) (value))));
			}
		} else if (value instanceof byte[]) {
			node.put(descriptionField.getField().getName(), (byte[]) value);
		}
	}
}
