package br.com.anteros.remote.synch.configuration;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;

import br.com.anteros.core.scanner.ClassFilter;
import br.com.anteros.core.scanner.ClassPathScanner;
import br.com.anteros.core.utils.ExpireConcurrentHashMap;
import br.com.anteros.core.utils.ReflectionUtils;
import br.com.anteros.core.utils.StringUtils;
import br.com.anteros.persistence.metadata.EntityCache;
import br.com.anteros.persistence.metadata.EntityListener;
import br.com.anteros.persistence.metadata.annotation.Code;
import br.com.anteros.persistence.metadata.annotation.EventType;
import br.com.anteros.persistence.metadata.annotation.Temporal;
import br.com.anteros.persistence.metadata.annotation.Transient;
import br.com.anteros.persistence.metadata.annotation.type.BooleanType;
import br.com.anteros.persistence.metadata.annotation.type.TemporalType;
import br.com.anteros.persistence.metadata.descriptor.DescriptionColumn;
import br.com.anteros.persistence.metadata.descriptor.DescriptionField;
import br.com.anteros.persistence.parameter.NamedParameter;
import br.com.anteros.persistence.parameter.NamedParameterList;
import br.com.anteros.persistence.session.FindParameters;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.SQLSessionFactory;
import br.com.anteros.persistence.session.query.SQLQuery;
import br.com.anteros.persistence.sql.command.Delete;
import br.com.anteros.persistence.sql.command.Insert;
import br.com.anteros.persistence.sql.command.Select;
import br.com.anteros.persistence.sql.command.Update;
import br.com.anteros.remote.synch.annotation.DataIntegrationFilterData;
import br.com.anteros.remote.synch.annotation.DataProcessorResult;
import br.com.anteros.remote.synch.annotation.MobileDataProcessor;
import br.com.anteros.remote.synch.annotation.MobileFilterData;
import br.com.anteros.remote.synch.annotation.RemoteSynchContext;
import br.com.anteros.remote.synch.annotation.RemoteSynchDataIntegration;
import br.com.anteros.remote.synch.annotation.RemoteSynchDataIntegrationFilterData;
import br.com.anteros.remote.synch.annotation.RemoteSynchIntegrationIgnore;
import br.com.anteros.remote.synch.annotation.RemoteSynchMobile;
import br.com.anteros.remote.synch.annotation.RemoteSynchMobileDataProcessor;
import br.com.anteros.remote.synch.annotation.RemoteSynchMobileFilterData;
import br.com.anteros.remote.synch.resource.RemoteSynchException;
import br.com.anteros.remote.synch.resource.TransactionHistoryData;
import br.com.anteros.remote.synch.serialization.RealmRemoteSynchSerialize;
import br.com.anteros.remote.synch.serialization.RemoteSynchSerializer;

public class RemoteSynchManager {

	private SQLSessionFactory sessionFactorySQL;
	private Map<String, RemoteMobileEntity> mobileEntities = new HashMap<>();
	private Map<String, RemoteDataIntegrationEntity> dataIntegrationEntities = new HashMap<>();
	private Map<String, RemoteFilterDataIntegrationEntity> filterDataIntegrationEntities = new HashMap<>();
	private String filterAndProcessorDataScanPackage;
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat sdft = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
	private SimpleDateFormat sdt = new SimpleDateFormat("HH:mm:ss.SSS");
	private Map<String, Object> idsByCode = new HashMap<>();
	private ExpireConcurrentHashMap<String, LinkedHashMap<String, JsonNode>> queue = new ExpireConcurrentHashMap<>(
			120000);

	public RemoteSynchManager(SQLSessionFactory sessionFactorySQL, String filterAndProcessorDataScanPackage) {
		this.sessionFactorySQL = sessionFactorySQL;
		this.filterAndProcessorDataScanPackage = filterAndProcessorDataScanPackage;
		init();
	}

	private void init() {
		List<Class<?>> scanClasses = new ArrayList<>();
		if (filterAndProcessorDataScanPackage != null) {
			String[] packages = StringUtils.tokenizeToStringArray(filterAndProcessorDataScanPackage, ", ;");
			scanClasses = ClassPathScanner.scanClasses(new ClassFilter().packages(packages)
					.annotation(RemoteSynchMobileFilterData.class).annotation(RemoteSynchMobileDataProcessor.class)
					.annotation(RemoteSynchDataIntegration.class).annotation(RemoteSynchDataIntegrationFilterData.class));
		}

		RemoteDeleteEntityListener deleteListener = new RemoteDeleteEntityListener();

		for (EntityCache entityCache : sessionFactorySQL.getEntityCacheManager().getEntities().values()) {
			if (entityCache.getEntityClass().isAnnotationPresent(RemoteSynchMobile.class)) {
				Method method = ReflectionUtils.getMethodByName(deleteListener.getClass(), "preRemove");
				entityCache.getEntityListeners().add(EntityListener.of(deleteListener, method, EventType.PreRemove));

				MobileFilterData filterData = null;
				MobileDataProcessor dataProcessor = null;
				RemoteSynchMobile annRemoteSynch = entityCache.getEntityClass().getAnnotation(RemoteSynchMobile.class);

				for (Class<?> cls : scanClasses) {
					RemoteSynchMobileFilterData annotation1 = cls.getAnnotation(RemoteSynchMobileFilterData.class);
					RemoteSynchMobileDataProcessor annotation2 = cls
							.getAnnotation(RemoteSynchMobileDataProcessor.class);
					if (annotation1 != null && annotation1.name().equals(annRemoteSynch.name())) {
						if (ReflectionUtils.isImplementsInterface(cls, MobileFilterData.class)) {
							try {
								filterData = (MobileFilterData) cls.newInstance();
							} catch (InstantiationException e) {
								e.printStackTrace();
							} catch (IllegalAccessException e) {
								e.printStackTrace();
							}
						}
					}

					if (annotation2 != null && annotation2.name().equals(annRemoteSynch.name())) {
						if (ReflectionUtils.isImplementsInterface(cls, MobileDataProcessor.class)) {
							try {
								dataProcessor = (MobileDataProcessor) cls.newInstance();
							} catch (InstantiationException e) {
								e.printStackTrace();
							} catch (IllegalAccessException e) {
								e.printStackTrace();
							}
						}
					}
				}
				mobileEntities.put(annRemoteSynch.name(),
						new RemoteMobileEntity(annRemoteSynch.name(), entityCache, filterData, dataProcessor));
			}

			if (entityCache.getEntityClass().isAnnotationPresent(RemoteSynchDataIntegration.class)) {
				RemoteSynchDataIntegration annRemoteSynch = entityCache.getEntityClass()
						.getAnnotation(RemoteSynchDataIntegration.class);

				try {
					EntityCache[] entityCaches = sessionFactorySQL.getEntityCacheManager()
							.getEntitiesBySuperClassIncluding(entityCache);
					dataIntegrationEntities.put(annRemoteSynch.name(),
							new RemoteDataIntegrationEntity(annRemoteSynch.name(), entityCaches));

					for (EntityCache enCache : entityCaches) {
						List<DescriptionField> joinTables = enCache.getJoinTables();
						for (DescriptionField df : joinTables) {
							if (df.isJoinTable()) {
								RemoteSynchDataIntegration CC = df.getField()
										.getAnnotation(RemoteSynchDataIntegration.class);
								if (CC != null) {
									dataIntegrationEntities.put(CC.name(),
											new RemoteDataIntegrationEntity(CC.name(), df));
								}
							}
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
				}

			}
			
			
			try {
					
				for (Class<?> cls : scanClasses) {
					RemoteSynchDataIntegrationFilterData annotation1 = cls.getAnnotation(RemoteSynchDataIntegrationFilterData.class);
					if (annotation1 != null && annotation1.name().equals(annotation1.name())) {
						if (ReflectionUtils.isImplementsInterface(cls, DataIntegrationFilterData.class)) {
							try {
								DataIntegrationFilterData filterData = null;
								filterData = (DataIntegrationFilterData) cls.newInstance();
								filterDataIntegrationEntities.put(annotation1.name(),
										new RemoteFilterDataIntegrationEntity(annotation1.name(),filterData));
							} catch (InstantiationException e) {
								e.printStackTrace();
							} catch (IllegalAccessException e) {
								e.printStackTrace();
							}
						}
					}
				}
				
				
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	public MobileDataProcessor lookupDataProcessor(String name) {
		for (String nm : mobileEntities.keySet()) {
			if (nm.equals(name)) {
				return mobileEntities.get(nm).getDataProcessor();
			}
		}
		return null;

	}

	public MobileFilterData lookupMobileFilterData(String name) {
		for (String nm : mobileEntities.keySet()) {
			if (nm.equals(name)) {
				return mobileEntities.get(nm).getFilterData();
			}
		}
		return null;

	}
	
	public DataIntegrationFilterData lookupDataIntegrationFilterData(String name) {
		for (String nm : filterDataIntegrationEntities.keySet()) {
			if (nm.equals(name)) {
				return filterDataIntegrationEntities.get(nm).getFilterData();
			}
		}
		return null;

	}

	public RemoteDataIntegrationEntity lookupDataIntegration(String name) {
		for (String nm : dataIntegrationEntities.keySet()) {
			if (nm.equals(name)) {
				return dataIntegrationEntities.get(nm);
			}
		}
		return null;

	}

	public RemoteSynchSerializer defaultSerializer() {
		return new RealmRemoteSynchSerialize();
	}

	public void updateData(SQLSession session, String entityName, RemoteDataIntegrationEntity dataIntegration,
			Collection<? extends Map<String, Object>> payload) {
		idsByCode.clear();
		int recno = 0;
		String tableName = "";
		Collection<RemoteRecord> parsedPayload = new ArrayList<>();
		Set<Object> idsToRemove = new LinkedHashSet<>();
		if (dataIntegration.getDescriptionField() != null) {

			EntityCache entityCache = dataIntegration.getDescriptionField().getEntityCache();

			ParameterizedType listType = (ParameterizedType) dataIntegration.getDescriptionField().getField()
					.getGenericType();
			Class<?> clazz2 = (Class<?>) listType.getActualTypeArguments()[0];

			EntityCache entityCache2 = session.getEntityCacheManager().getEntityCache(clazz2);

			for (Map<String, Object> record : payload) {
				recno++;
				RemoteRecord parsedRecord = new RemoteRecord();
				parsedPayload.add(parsedRecord);
				if (record.size() != 2) {
					throw new RemoteSynchException("Registro " + recno + " da Entidade " + entityName
							+ " possui o número de campos incorretos.");
				}

				DescriptionField code1 = entityCache.getCodeField();
				DescriptionField code2 = entityCache2.getCodeField();

				tableName = dataIntegration.getDescriptionField().getTableName();

				if (!record.containsKey(code1.getField().getName())) {
					throw new RemoteSynchException("Registro " + recno + " da Entidade " + entityName
							+ " não possui o campo " + code1.getField().getName() + " informado.");
				}

				if (!record.containsKey(code2.getField().getName())) {
					throw new RemoteSynchException("Registro " + recno + " da Entidade " + entityName
							+ " não possui o campo " + code2.getField().getName() + " informado.");
				}

				Object idByCode1 = null;
				Object idByCode2 = null;
				try {
					idByCode1 = getIdByCode(session, code1, record.get(code1.getField().getName()));
					if (idByCode1 == null) {
						throw new RemoteSynchException("Registro " + recno + " da Entidade " + entityName
								+ " não foi possível encontrar o valor do campo " + code1.getField().getName() + "="
								+ record.get(code1.getField().getName()) + " na Entidade "
								+ entityCache.getEntityClass().getName());
					}
				} catch (Exception e) {
					throw new RemoteSynchException("Registro " + recno + " da Entidade " + entityName
							+ " não foi possível encontrar o valor do campo " + code1.getField().getName() + "="
							+ record.get(code1.getField().getName()) + " na Entidade "
							+ entityCache.getEntityClass().getName());
				}

				try {
					idByCode2 = getIdByCode(session, code2, record.get(code2.getField().getName()));
					if (idByCode2 == null) {
						throw new RemoteSynchException("Registro " + recno + " da Entidade " + entityName
								+ " não foi possível encontrar o valor do campo " + code2.getField().getName() + "="
								+ record.get(code2.getField().getName()) + " na Entidade relacionada "
								+ entityCache2.getEntityClass().getName());
					}
				} catch (Exception e) {
					throw new RemoteSynchException("Registro " + recno + " da Entidade " + entityName
							+ " não foi possível encontrar o valor do campo " + code2.getField().getName() + "="
							+ record.get(code2.getField().getName()) + " na Entidade relacionada "
							+ entityCache2.getEntityClass().getName());
				}

				List<DescriptionColumn> columns = dataIntegration.getDescriptionField().getDescriptionColumns();

				for (DescriptionColumn column : columns) {
					if (column.isJoinColumn()) {
						parsedRecord.addField(column.getColumnName(), idByCode1);
					}
					if (column.isInversedJoinColumn()) {
						parsedRecord.addField(column.getColumnName(), idByCode2);
					}
				}

				idsToRemove.add(idByCode1);
			}

			for (Object objToRemove : idsToRemove) {
				try {
					Delete delete = new Delete();
					delete.addPrimaryKeyColumn(entityCache.getPrimaryKeyColumns().iterator().next().getColumnName(),
							objToRemove.toString());
					delete.setTableName(tableName);
					session.update(delete.toStatementString());
				} catch (Exception e) {
					throw new RemoteSynchException(e);
				}
			}

			for (RemoteRecord record : parsedPayload) {
				recno++;
				try {
					Insert insert = new Insert(session.getDialect());
					insert.setTableName(tableName);
					NamedParameterList namedParameterList = NamedParameter.list();
					for (String fieldName : record.record.keySet()) {
						Object value = record.record.get(fieldName);
						insert.addColumn(fieldName, ":" + fieldName);
						namedParameterList.add(new NamedParameter(fieldName, value));
					}
					session.update(insert.toStatementString(), namedParameterList.toArray());
				} catch (Exception e) {
					throw new RemoteSynchException(e);
				}
			}

		} else {
			EntityCache[] entityCaches = dataIntegration.getEntityCache();
			Set<String> valuesType = getValuesType(entityCaches);
			for (Map<String, Object> record : payload) {
				recno++;
				RemoteRecord parsedRecord = new RemoteRecord();
				parsedPayload.add(parsedRecord);
				if (valuesType.size() > 0) {
					if (!record.containsKey("type") || StringUtils.isBlank(record.get("type") + "")) {
						throw new RemoteSynchException("Registro " + recno + " da Entidade " + entityName
								+ " não possui valor para o campo type.");
					} else {
						String tp = (String) record.get("type");
						if (!valuesType.contains(tp)) {
							throw new RemoteSynchException("Registro " + recno + " da Entidade " + entityName
									+ " informado valor incorreto para o campo type: " + tp);
						}

						for (EntityCache entityCache : entityCaches) {
							if (entityCache.hasDiscriminatorColumn()) {
								parsedRecord.addField(entityCache.getDiscriminatorColumn().getColumnName(), tp);
							}
						}
					}
				}

				for (EntityCache entityCache : entityCaches) {
					if (entityCache.getTableName() != null) {
						tableName = entityCache.getTableName();
					}

					DescriptionField _codeField = entityCache.getCodeField();
					DescriptionField _primaryKey = entityCache.getPrimaryKeyFields()[0];

					if (record.containsKey(_primaryKey.getField().getName())) {
						throw new RemoteSynchException(
								"Registro " + recno + " da Entidade " + entityName + " não deve ser informado campo ID "
										+ _primaryKey.getField().getName() + " pois será gerado pelo sistema.");
					}

					if (_codeField != null && record.size() == 1) {
						if (record.containsKey(_codeField.getField().getName())) {
							buildDeleteRecord(session, record, parsedRecord, _codeField, _primaryKey);
							continue;
						}
					}

					for (DescriptionField descriptionField : entityCache.getDescriptionFields()) {
						Field field = descriptionField.getField();
						if (field.isAnnotationPresent(Transient.class) || field.isAnnotationPresent(JsonIgnore.class)
								|| field.isAnnotationPresent(RemoteSynchIntegrationIgnore.class)) {
							continue;
						}

						if (descriptionField.isPrimaryKey()) {
							try {
								DescriptionField codeField = entityCache.getCodeField();
								parsedRecord.setCode(record.get(codeField.getField().getName()) + "");
								if (codeField != null) {
									Object idByCode = getIdByCode(session, codeField,
											record.get(codeField.getField().getName()));
									if (idByCode != null) {
										parsedRecord.addPrimaryKeyField(
												descriptionField.getSimpleColumn().getColumnName(), idByCode);
										parsedRecord.setOperation("update");
									}
								}
								continue;
							} catch (Exception e) {

							}

						}

						validateImportantFields(entityName, recno, record, parsedRecord, descriptionField, field);

						if (record.containsKey(field.getName())) {
							if (descriptionField.isSimple()) {
								parseSimpleField(entityName, recno, record, parsedRecord, descriptionField, field);
							} else if (descriptionField.isEnumerated()) {
								descriptionField.convertObjectToEnum(record.get(field.getName()).toString());
								parsedRecord.addField(descriptionField.getSimpleColumn().getColumnName(),
										record.get(field.getName()).toString());
							} else if ((field.getType() == byte[].class) || (field.getType() == Byte[].class)) {
								parsedRecord.addField(descriptionField.getSimpleColumn().getColumnName(),
										record.get(field.getName()).toString().getBytes());
							} else if (descriptionField.isRelationShip()) {
								parseRelationShipField(session, entityName, recno, record, parsedRecord,
										descriptionField, field);
							}
						}
					}
				}
			}
			for (RemoteRecord record : parsedPayload) {
				recno++;
				try {
					if (record.operation.equals("insert")) {
						Insert insert = new Insert(session.getDialect());
						insert.setTableName(tableName);
						NamedParameterList namedParameterList = NamedParameter.list();
						for (String fieldName : record.record.keySet()) {
							Object value = record.record.get(fieldName);
							insert.addColumn(fieldName, ":" + fieldName);
							namedParameterList.add(new NamedParameter(fieldName, value));
						}
						session.update(insert.toStatementString(), namedParameterList.toArray());
					} else if (record.operation.equals("update")) {
						Update update = new Update(session.getDialect());
						update.setTableName(tableName);
						NamedParameterList namedParameterList = NamedParameter.list();
						for (String fieldName : record.record.keySet()) {
							Object value = record.record.get(fieldName);
							update.addColumn(fieldName, ":" + fieldName);
							namedParameterList.add(new NamedParameter(fieldName, value));
						}
						update.addPrimaryKeyColumn(record.pkField, ":" + record.pkField);
						namedParameterList.add(new NamedParameter(record.pkField, record.id));
						session.update(update.toStatementString(), namedParameterList.toArray());
					} else if (record.operation.equals("delete")) {
						session.remove(record.getObjectDelete());
					}
				} catch (Exception e) {
					throw new RemoteSynchException(e);
				}
			}
		}
	}

	protected void parseRelationShipField(SQLSession session, String entityName, int recno, Map<String, Object> record,
			RemoteRecord parsedRecord, DescriptionField descriptionField, Field field) {
		Object idByRelationShip = null;
		try {
			idByRelationShip = getIdByCode(session, descriptionField.getTargetEntity().getCodeField(),
					record.get(field.getName()));

		} catch (Exception e) {
			EntityCache targetEntity = descriptionField.getTargetEntity();
			RemoteSynchDataIntegration annotation = targetEntity.getEntityClass()
					.getAnnotation(RemoteSynchDataIntegration.class);
			String relationShipName = targetEntity.getEntityClass().getSimpleName();
			if (annotation != null) {
				relationShipName = annotation.name();
			}

			throw new RemoteSynchException("Registro " + recno + " da Entidade " + entityName
					+ " não foi possível encontrar o valor do campo " + field.getName() + "="
					+ record.get(field.getName()) + " na Entidade relacionada " + relationShipName);
		}

		if (idByRelationShip == null) {
			EntityCache targetEntity = descriptionField.getTargetEntity();
			RemoteSynchDataIntegration annotation = targetEntity.getEntityClass()
					.getAnnotation(RemoteSynchDataIntegration.class);
			String relationShipName = targetEntity.getEntityClass().getSimpleName();
			if (annotation != null) {
				relationShipName = annotation.name();
			}

			throw new RemoteSynchException("Registro " + recno + " da Entidade " + entityName
					+ " não foi possível encontrar o valor do campo " + field.getName() + "="
					+ record.get(field.getName()) + " na Entidade relacionada " + relationShipName);
		}

		parsedRecord.addField(descriptionField.getSimpleColumn().getColumnName(), idByRelationShip);
	}

	protected void parseSimpleField(String entityName, int recno, Map<String, Object> record, RemoteRecord parsedRecord,
			DescriptionField descriptionField, Field field) {
		if (ReflectionUtils.isExtendsClass(BigInteger.class, field.getType())) {
			try {
				if (!StringUtils.isNumber(record.get(field.getName()).toString())) {
					throw new RemoteSynchException("Registro " + recno + " da Entidade " + entityName
							+ " não foi possível converter valor do campo " + field.getName() + " para Númerico.");
				}
				parsedRecord.addField(descriptionField.getSimpleColumn().getColumnName(),
						new BigInteger(record.get(field.getName()).toString()));
			} catch (NumberFormatException e) {
				throw new RemoteSynchException("Registro " + recno + " da Entidade " + entityName
						+ " não foi possível converter valor do campo " + field.getName() + " para Numérico(Inteiro).");
			}
		} else if (ReflectionUtils.isExtendsClass(BigDecimal.class, field.getType())) {
			try {
				if (!StringUtils.isNumber(record.get(field.getName()).toString())) {
					throw new RemoteSynchException("Registro " + recno + " da Entidade " + entityName
							+ " não foi possível converter valor do campo " + field.getName() + " para Númerico.");
				}
				parsedRecord.addField(descriptionField.getSimpleColumn().getColumnName(),
						new BigDecimal(record.get(field.getName()).toString()));
			} catch (NumberFormatException e) {
				throw new RemoteSynchException("Registro " + recno + " da Entidade " + entityName
						+ " não foi possível converter valor do campo " + field.getName() + " para Númerico(Decimal).");
			}
		} else if (ReflectionUtils.isExtendsClass(Number.class, field.getType())) {
			try {
				if (!StringUtils.isNumber(record.get(field.getName()).toString())) {
					throw new RemoteSynchException("Registro " + recno + " da Entidade " + entityName
							+ " não foi possível converter valor do campo " + field.getName() + " para Númerico.");
				}
				Double value = Double.valueOf(record.get(field.getName()).toString());
				parsedRecord.addField(descriptionField.getSimpleColumn().getColumnName(), value);
			} catch (Exception e) {
				throw new RemoteSynchException("Registro " + recno + " da Entidade " + entityName
						+ " não foi possível converter valor do campo " + field.getName() + " para Númerico.");
			}
		} else if (descriptionField.isEnumerated()) {
			descriptionField.convertObjectToEnum(record.get(field.getName()).toString());
			parsedRecord.addField(descriptionField.getSimpleColumn().getColumnName(),
					record.get(field.getName()).toString());
		} else if ((field.getType() == byte[].class) || (field.getType() == Byte[].class)) {
			parsedRecord.addField(descriptionField.getSimpleColumn().getColumnName(),
					record.get(field.getName()).toString().getBytes());
		} else if (ReflectionUtils.isExtendsClass(String.class, field.getType())) {
			parsedRecord.addField(descriptionField.getSimpleColumn().getColumnName(),
					record.get(field.getName()).toString());
		} else if (descriptionField.isBoolean()) {
			Object value = record.get(field.getName());
			if (descriptionField.getSimpleColumn().getBooleanType() == BooleanType.INTEGER) {
				if (value.toString().equals("true")) {
					parsedRecord.addField(descriptionField.getSimpleColumn().getColumnName(), 1);
				} else {
					parsedRecord.addField(descriptionField.getSimpleColumn().getColumnName(), 0);
				}
			} else if (descriptionField.getSimpleColumn().getBooleanType() == BooleanType.STRING) {
				if (value.toString().equals("true")) {
					parsedRecord.addField(descriptionField.getSimpleColumn().getColumnName(), "S");
				} else {
					parsedRecord.addField(descriptionField.getSimpleColumn().getColumnName(), "N");
				}
			} else {
				parsedRecord.addField(descriptionField.getSimpleColumn().getColumnName(), value);
			}
		} else if (ReflectionUtils.isExtendsClass(Date.class, field.getType())
				|| (ReflectionUtils.isExtendsClass(java.sql.Date.class, field.getType()))) {
			if (field.isAnnotationPresent(Temporal.class)) {
				Temporal temp = field.getAnnotation(Temporal.class);
				if (temp.value() == TemporalType.DATE) {
					try {
						parsedRecord.addField(descriptionField.getSimpleColumn().getColumnName(),
								sdf.parse(record.get(field.getName()).toString()));
					} catch (ParseException e) {
						throw new RemoteSynchException("Registro " + recno + " da Entidade " + entityName
								+ " não foi possível converter valor do campo " + field.getName() + " para Data.");
					}
				}
				if (temp.value() == TemporalType.DATE_TIME) {
					try {
						parsedRecord.addField(descriptionField.getSimpleColumn().getColumnName(),
								sdft.parse(record.get(field.getName()).toString()));
					} catch (ParseException e) {
						throw new RemoteSynchException("Registro " + recno + " da Entidade " + entityName
								+ " não foi possível converter valor do campo " + field.getName() + " para Data/hora.");
					}
				}
				if (temp.value() == TemporalType.TIME) {
					try {
						parsedRecord.addField(descriptionField.getSimpleColumn().getColumnName(),
								sdt.parse(record.get(field.getName()).toString()));
					} catch (ParseException e) {
						throw new RemoteSynchException("Registro " + recno + " da Entidade " + entityName
								+ " não foi possível converter valor do campo " + field.getName() + " para Hora.");
					}
				}
			}
		}
	}

	protected void validateImportantFields(String entityName, int recno, Map<String, Object> record,
			RemoteRecord parsedRecord, DescriptionField descriptionField, Field field) {
		if (descriptionField.isVersioned()) {
			if (record.containsKey(descriptionField.getField().getName())) {
				throw new RemoteSynchException(
						"Registro " + recno + " da Entidade " + entityName + " não deve ser informado campo VERSÃO "
								+ descriptionField.getField().getName() + " pois será gerado pelo sistema.");
			}
			parsedRecord.addField(descriptionField.getSimpleColumn().getColumnName(), new Date());
		}

		if (descriptionField.isRequired() && !descriptionField.isCompositeId() && !descriptionField.isPrimaryKey()
				&& !descriptionField.isVersioned() && !descriptionField.isJoinTable()) {
			if (!record.containsKey(descriptionField.getField().getName())) {
				throw new RemoteSynchException("Registro " + recno + " da Entidade " + entityName
						+ " não possui valor para o campo " + field.getName());
			}

		}

		if (field.isAnnotationPresent(Code.class)) {
			if (!record.containsKey(field.getName())) {
				throw new RemoteSynchException(
						"Registro " + recno + " da Entidade " + entityName + " não possui valor para o campo "
								+ field.getName() + ". Este campo é a chave do registro e é obrigatório.");
			}
		}
	}

	protected void buildDeleteRecord(SQLSession session, Map<String, Object> record, RemoteRecord parsedRecord,
			DescriptionField _codeField, DescriptionField _primaryKey) {
		try {
			Object idByCode = getIdByCode(session, _codeField, record.get(_codeField.getField().getName()));
			if (idByCode != null) {
				parsedRecord.addPrimaryKeyField(_primaryKey.getSimpleColumn().getColumnName(), idByCode);
				Class entityClass = _primaryKey.getEntityCache().getEntityClass();
				FindParameters<Object> parameters = new FindParameters<>();
				parameters.entityClass(entityClass).id(idByCode);
				Object objectDelete = session.find(parameters);
				parsedRecord.setOperation("delete");
				parsedRecord.setObjectDelete(objectDelete);
			}
		} catch (Exception e) {

		}
	}

	private Object getIdByCode(SQLSession session, DescriptionField codeField, Object value) throws Exception {
		String key = codeField.getTableName() + "_" + value.toString();
		if (idsByCode.containsKey(key)) {
			return idsByCode.get(key);
		}

		Select select = new Select(session.getDialect());
		select.addTableName(codeField.getEntityCache().getTableName());
		boolean appendAnd = false;
		if (codeField.getEntityCache().hasTenantId()) {
			select.addCondition(codeField.getEntityCache().getTenantId().getSimpleColumn().getColumnName(), "=",
					"'" + session.getTenantId().toString() + "'");
			appendAnd = true;
		}

		if (codeField.getEntityCache().hasCompanyId()) {
			String companyId = this
					.getCompanyIdByEntityCache(session, codeField.getEntityCache().getCompanyId().getTargetEntity())
					.toString();
			if (appendAnd)
				select.and();
			appendAnd = true;
			select.addCondition(codeField.getEntityCache().getCompanyId().getSimpleColumn().getColumnName(), "=",
					companyId);
		}
		if (appendAnd)
			select.and();
		select.addCondition(codeField.getSimpleColumn().getColumnName(), "=", '"' + value.toString() + '"');

		String sql = select.toStatementString();
		SQLQuery query = session.createQuery(sql);
		ResultSet resultSet = query.executeQuery();
		Object idValue = null;
		if (resultSet.next()) {
			String columnName = codeField.getEntityCache().getPrimaryKeyColumns().get(0).getColumnName();
			int index = resultSet.findColumn(columnName);
			idValue = resultSet.getObject(index);
			resultSet.close();
			idsByCode.put(key, idValue);
		} else {
			resultSet.close();
		}
		return idValue;
	}

	private Object getCompanyIdByEntityCache(SQLSession session, EntityCache targetEntity) throws Exception {
		if (targetEntity.getCodeField() != null) {

			DescriptionField codeField = targetEntity.getCodeField();

			String key = codeField.getTableName() + "_" + session.getTenantId().toString();
			if (idsByCode.containsKey(key)) {
				return idsByCode.get(key);
			}
			SQLQuery query = session.createQuery("select * from " + targetEntity.getTableName() + " tb where" + " tb."
					+ codeField.getSimpleColumn().getColumnName() + " = '" + session.getCompanyId().toString()
					+ "' and tb." + targetEntity.getTenantId().getSimpleColumn().getColumnName() + " = '"
					+ session.getTenantId().toString() + "'");
			ResultSet resultSet = query.executeQuery();
			Object idValue = null;
			if (resultSet.next()) {
				String columnName = codeField.getEntityCache().getPrimaryKeyColumns().get(0).getColumnName();
				int index = resultSet.findColumn(columnName);
				idValue = resultSet.getObject(index);
				resultSet.close();
				idsByCode.put(key, idValue);
			} else {
				resultSet.close();
			}
			return idValue;
		}
		throw new RemoteSynchException("Entidade " + targetEntity.getEntityClass().getName()
				+ " não possuí campo Code definido e foi usada como relacionamento. Não é possível realizar a integração.");
	}

	private Set<String> getValuesType(EntityCache[] entityCaches) {
		Set<String> result = new HashSet<>();
		for (EntityCache entityCache : entityCaches) {
			if (entityCache.hasDiscriminatorValue()) {
				result.add(entityCache.getDiscriminatorValue());
			}
		}
		return result;
	}

	class RemoteRecord {

		private Map<String, Object> record = new HashMap<>();
		private String operation = "insert";
		private String pkField;
		private String code;
		private Object id;
		private Object objectDelete;

		public RemoteRecord addField(String field, Object value) {
			record.put(field, value);
			return this;
		}

		public void setObjectDelete(Object objectDelete) {
			this.objectDelete = objectDelete;
		}

		public void addPrimaryKeyField(String columnName, Object id) {
			this.pkField = columnName;
			this.id = id;

		}

		public RemoteRecord setOperation(String op) {
			this.operation = op;
			return this;
		}

		public Object getObjectDelete() {
			return objectDelete;
		}

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}
	}

	public void enqueue(String clientId, String tnsID, String name, JsonNode object) {
		if (!queue.containsKey(tnsID)) {
			queue.put(tnsID, new LinkedHashMap<>());
		}
		queue.get(tnsID).put(name, object);
	}

	public void startTransaction(String clientId, String tnsID) {
		if (queue.containsKey(tnsID)) {
			queue.get(tnsID).clear();
		} else {
			queue.put(tnsID, new LinkedHashMap<>());
		}

	}

	public List<TransactionHistoryData> finishTransaction(SQLSession session, String clientId, String tnsID) throws Exception {
		if (!queue.containsKey(tnsID)) {
			throw new RemoteSynchException("A transação " + tnsID + " não foi iniciada no servidor.");
		}
		LinkedHashMap<String, JsonNode> data = queue.get(tnsID);
		Map<String, Object> objectCache = new HashMap<>();
		List<TransactionHistoryData> result = new ArrayList<>();
		for (String name : data.keySet()) {
			MobileDataProcessor dataProcessor = lookupDataProcessor(name);
			if (dataProcessor == null) {
				throw new RemoteSynchException(
						"Não foi possível encontrar um processador para os dados da entidade " + name);
			}

			RemoteSynchContext context = new RemoteSynchContext(session);
			context.addParameter("clientId", clientId);
			context.addParameter("tnsID", tnsID);
			context.addParameter("data", data.get(name));
			context.addParameter("cache", objectCache);

			DataProcessorResult dataResult = dataProcessor.process(context);

			RemoteSynchTransactionHistory history = new RemoteSynchTransactionHistory();
			history.setCompany(Long.valueOf(session.getCompanyId().toString()));
			history.setDhTransaction(new Date());
			history.setEntity(name);
			history.setEquipament(clientId);
			history.setId(tnsID);
			history.setNumberOfRecords(new Long(data.size()));
			history.setOwner(session.getTenantId().toString());
			session.save(history);
			
			result.add(new TransactionHistoryData(name, data.size(), Integer.valueOf(session.getCompanyId().toString()), dataResult.getCompanyCode()));
		}
		return result;
	}

	public Boolean checkTransaction(SQLSession session, String clientId, String tnsID) {
		Boolean result = false;
		try {
			SQLQuery query = session
					.createQuery("select count(*) as quant from TRANSACAO_HISTORICO tns where tns.UUID_TRANSACAO = "
							+ "'" + tnsID + "'");
			ResultSet resultSet = query.executeQuery();
			if (resultSet.next()) {
				result = (resultSet.getLong(1) > 0);
			}
			resultSet.close();
		} catch (Exception e) {
			throw new RemoteSynchException(e);
		}
		return result;
	}

}
