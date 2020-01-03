package br.com.anteros.remote.synch.serialization;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import br.com.anteros.persistence.metadata.EntityCache;
import br.com.anteros.persistence.metadata.descriptor.DescriptionField;
import br.com.anteros.persistence.metadata.identifier.Identifier;
import br.com.anteros.persistence.proxy.AnterosProxyObject;
import br.com.anteros.persistence.proxy.collection.AnterosPersistentCollection;
import br.com.anteros.persistence.serialization.jackson.AnterosObjectMapper;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.remote.synch.service.ResultData;

public class RealmRemoteSynchSerialize implements RemoteSynchSerializer {

	public static DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	public static DateFormat dft = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

	@Override
	public <T> ObjectNode serialize(ResultData<T> data, SQLSession currentSession, Class<?> resultClass) {
		AnterosObjectMapper objectMapper = new AnterosObjectMapper(currentSession.getSQLSessionFactory());
		ObjectNode mainNode = objectMapper.createObjectNode();
		ArrayNode listNode = mainNode.putArray(data.getName());
		try {
			EntityCache entityCache = currentSession.getEntityCacheManager().getEntityCache(resultClass);

			for (T res : data.getContent()) {
				ObjectNode node = listNode.addObject();
				for (DescriptionField descriptionField : entityCache.getDescriptionFields()) {
					if (descriptionField.isSimple()) {
						Object value = descriptionField.getObjectValue(res);
						putValue(node, descriptionField, value);
					} else if (descriptionField.isRelationShip()) {
						Object value = descriptionField.getObjectValue(res);
						if (value == null) {
							node.putNull(descriptionField.getField().getName());
						} else {
							addEntity(objectMapper,currentSession, node, descriptionField, value);
						}
					} else if (descriptionField.isCollectionEntity()) {
						Object value = descriptionField.getObjectValue(res);
						if (value == null) {
							node.putNull(descriptionField.getField().getName());
						} else if (AnterosProxyObject.class.isAssignableFrom(value.getClass())) {
							Object proxiedValue;
							try {
								proxiedValue = findProxied(value);
							} catch (Exception e) {
								throw new RuntimeException(e);
							}
							value = proxiedValue;
							if (value == null) {
								node.putNull(descriptionField.getField().getName());
							} else {
								ArrayNode arrayNode = node.putArray(descriptionField.getField().getName());
								Iterator it = ((Collection) value).iterator();
								while (it.hasNext()) {
									Object v = it.next();
									Identifier<Object> identifier = currentSession.getIdentifier(v);
									Map<DescriptionField, Object> fieldsValues = identifier.getFieldsValues();
									
									ObjectNode vl = convertToNodeValue(objectMapper,fieldsValues);
									arrayNode.add(vl);
								}
							}
						} else if (value instanceof AnterosPersistentCollection) {
							AnterosPersistentCollection coll = (AnterosPersistentCollection) value;
							if (!coll.isInitialized()) {
								node.putNull(descriptionField.getField().getName());
							} else {
								value = coll;
								if (value == null) {
									node.putNull(descriptionField.getField().getName());
								} else {
									putValue(node, descriptionField, value);
								}
							}
						}

					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return mainNode;
	}

	protected void addValue(ArrayNode node, Object value, DescriptionField descriptionField) {
		if (value == null) {
			node.addNull();
			return;
		}
		if (value instanceof String) {
			node.add((String) (value));
		} else if (value instanceof Integer) {
			node.add((Integer) (value));
		} else if (value instanceof Double) {
			node.add((Double) (value));
		} else if (value instanceof BigDecimal) {
			node.add((BigDecimal) (value));
		} else if (value instanceof Boolean) {
			node.add((Boolean) (value));
		} else if (value instanceof Long) {
			node.add((Long) (value));
		} else if (value instanceof BigInteger) {
			node.add(((BigInteger) (value)).intValue());
		} else if (value instanceof Date) {
			if (descriptionField.isTemporalDate()) {
				node.add(df.format(((Date) (value))));
			} else {
				node.add(dft.format(((Date) (value))));
			}
		} else if (value instanceof byte[]) {
			node.add((byte[]) value);
		}
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

	protected Object findProxied(Object value) throws Exception {
		if (value instanceof AnterosProxyObject) {
//			if (!((AnterosProxyObject) (value)).isInitialized()) {
//				return null;
//			}
			return ((AnterosProxyObject) (value)).initializeAndReturnObject();
		}

		return null;
	}

	protected void addEntity(ObjectMapper mapper, SQLSession currentSession, ObjectNode node, DescriptionField descriptionField,
			Object value) throws Exception {
		Identifier<Object> identifier = currentSession.getIdentifier(value);
		Map<DescriptionField, Object> fieldsValues = identifier.getFieldsValues();
		
		node.put(descriptionField.getField().getName(), convertToNodeValue(mapper,fieldsValues));
	}

	private ObjectNode convertToNodeValue(ObjectMapper mapper, Map<DescriptionField, Object> fieldsValues) {
		ObjectNode result = mapper.createObjectNode();
		for (DescriptionField descriptionField : fieldsValues.keySet()) {
			Object value = fieldsValues.get(descriptionField);
			if (value instanceof Map) {
				result.put(descriptionField.getField().getName(), convertToNodeValue(mapper, (Map<DescriptionField, Object>)value));
			} else {
				putValue(result, descriptionField, value);
			}
		}
		return result;
	}

}
