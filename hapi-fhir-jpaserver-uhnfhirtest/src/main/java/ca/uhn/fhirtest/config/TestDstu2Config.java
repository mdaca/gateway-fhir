package ca.uhn.fhirtest.config;

import ca.uhn.fhir.jpa.api.config.DaoConfig;
import ca.uhn.fhir.jpa.api.model.HistoryCountModeEnum;
import ca.uhn.fhir.jpa.config.BaseJavaConfigDstu2;
import ca.uhn.fhir.jpa.model.dialect.HapiFhirPostgres94Dialect;
import ca.uhn.fhir.jpa.model.entity.ModelConfig;
import ca.uhn.fhir.jpa.search.HapiLuceneAnalysisConfigurer;
import ca.uhn.fhir.jpa.util.CurrentThreadCaptureQueriesListener;
import ca.uhn.fhir.jpa.util.DerbyTenSevenHapiFhirDialect;
import ca.uhn.fhir.jpa.validation.ValidationSettings;
import ca.uhn.fhir.rest.server.interceptor.RequestValidatingInterceptor;
import ca.uhn.fhir.validation.IValidatorModule;
import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhirtest.interceptor.PublicSecurityInterceptor;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.dialect.PostgreSQL94Dialect;
import org.hibernate.search.backend.lucene.cfg.LuceneBackendSettings;
import org.hibernate.search.backend.lucene.cfg.LuceneIndexSettings;
import org.hibernate.search.engine.cfg.BackendSettings;
import org.hibernate.search.mapper.orm.cfg.HibernateOrmMapperSettings;
import org.hl7.fhir.dstu2.model.Subscription;
import org.hl7.fhir.r5.utils.IResourceValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@Configuration
@Import(CommonConfig.class)
@EnableTransactionManagement()
public class TestDstu2Config extends BaseJavaConfigDstu2 {

	public static final String FHIR_LUCENE_LOCATION_DSTU2 = "${fhir.lucene.location.dstu2}";

	@Value(TestDstu3Config.FHIR_DB_USERNAME)
	private String myDbUsername;

	@Value(TestDstu3Config.FHIR_DB_PASSWORD)
	private String myDbPassword;

	@Value(FHIR_LUCENE_LOCATION_DSTU2)
	private String myFhirLuceneLocation;

	@Bean
	public PublicSecurityInterceptor securityInterceptor() {
		return new PublicSecurityInterceptor();
	}

	@Bean
	public DaoConfig daoConfig() {
		DaoConfig retVal = new DaoConfig();
		retVal.addSupportedSubscriptionType(Subscription.SubscriptionChannelType.EMAIL);
		retVal.addSupportedSubscriptionType(Subscription.SubscriptionChannelType.RESTHOOK);
		retVal.addSupportedSubscriptionType(Subscription.SubscriptionChannelType.WEBSOCKET);
		retVal.setWebsocketContextPath("/websocketDstu2");
		retVal.setAllowContainsSearches(true);
		retVal.setAllowMultipleDelete(true);
		retVal.setAllowInlineMatchUrlReferences(true);
		retVal.setAllowExternalReferences(true);
		retVal.getTreatBaseUrlsAsLocal().add("http://hapi.fhir.org/baseDstu2");
		retVal.getTreatBaseUrlsAsLocal().add("https://hapi.fhir.org/baseDstu2");
		retVal.getTreatBaseUrlsAsLocal().add("http://fhirtest.uhn.ca/baseDstu2");
		retVal.getTreatBaseUrlsAsLocal().add("https://fhirtest.uhn.ca/baseDstu2");
		retVal.setCountSearchResultsUpTo(TestR4Config.COUNT_SEARCH_RESULTS_UP_TO);
		retVal.setIndexMissingFields(DaoConfig.IndexEnabledEnum.ENABLED);
		retVal.setFetchSizeDefaultMaximum(10000);
		retVal.setWebsocketContextPath("/");
		retVal.setFilterParameterEnabled(true);
		retVal.setDefaultSearchParamsCanBeOverridden(false);
		retVal.getModelConfig().setIndexOnContainedResources(true);
//		retVal.setHistoryCountMode(HistoryCountModeEnum.COUNT_ACCURATE);
		return retVal;
	}

	@Bean
	public ModelConfig modelConfig() {
		return daoConfig().getModelConfig();
	}

	@Override
	@Bean
	public ValidationSettings validationSettings() {
		ValidationSettings retVal = super.validationSettings();
		retVal.setLocalReferenceValidationDefaultPolicy(IResourceValidator.ReferenceValidationPolicy.CHECK_VALID);
		return retVal;
	}


	@Bean(name = "myPersistenceDataSourceDstu1", destroyMethod = "close")
	public DataSource dataSource() {
		BasicDataSource retVal = new BasicDataSource();
		if (CommonConfig.isLocalTestMode()) {
			retVal.setUrl("jdbc:derby:memory:fhirtest_dstu2;create=true");
		} else {
			retVal.setDriver(new org.postgresql.Driver());
			retVal.setUrl("jdbc:postgresql://localhost/fhirtest_dstu2");
		}
		retVal.setUsername(myDbUsername);
		retVal.setPassword(myDbPassword);
		retVal.setDefaultQueryTimeout(20);
		retVal.setTestOnBorrow(true);

		DataSource dataSource = ProxyDataSourceBuilder
			.create(retVal)
//			.logQueryBySlf4j(SLF4JLogLevel.INFO, "SQL")
			.logSlowQueryBySlf4j(10000, TimeUnit.MILLISECONDS)
			.afterQuery(new CurrentThreadCaptureQueriesListener())
			.countQuery()
			.build();

		return dataSource;
	}

	@Primary
	@Bean
	public JpaTransactionManager hapiTransactionManager(EntityManagerFactory entityManagerFactory) {
		JpaTransactionManager retVal = new JpaTransactionManager();
		retVal.setEntityManagerFactory(entityManagerFactory);
		return retVal;
	}

	@Override
	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory(ConfigurableListableBeanFactory theConfigurableListableBeanFactory) {
		LocalContainerEntityManagerFactoryBean retVal = super.entityManagerFactory(theConfigurableListableBeanFactory);
		retVal.setPersistenceUnitName("PU_HapiFhirJpaDstu2");
		retVal.setDataSource(dataSource());
		retVal.setJpaProperties(jpaProperties());
		return retVal;
	}

	private Properties jpaProperties() {
		Properties extraProperties = new Properties();
		if (CommonConfig.isLocalTestMode()) {
			extraProperties.put("hibernate.dialect", DerbyTenSevenHapiFhirDialect.class.getName());
		} else {
			extraProperties.put("hibernate.dialect", HapiFhirPostgres94Dialect.class.getName());
		}
		extraProperties.put("hibernate.format_sql", "false");
		extraProperties.put("hibernate.show_sql", "false");
		extraProperties.put("hibernate.hbm2ddl.auto", "update");
		extraProperties.put("hibernate.jdbc.batch_size", "20");
		extraProperties.put("hibernate.cache.use_query_cache", "false");
		extraProperties.put("hibernate.cache.use_second_level_cache", "false");
		extraProperties.put("hibernate.cache.use_structured_entries", "false");
		extraProperties.put("hibernate.cache.use_minimal_puts", "false");

		extraProperties.put(BackendSettings.backendKey(BackendSettings.TYPE), "lucene");
		extraProperties.put(BackendSettings.backendKey(LuceneBackendSettings.ANALYSIS_CONFIGURER), HapiLuceneAnalysisConfigurer.class.getName());
		extraProperties.put(BackendSettings.backendKey(LuceneIndexSettings.DIRECTORY_TYPE), "local-filesystem");
		extraProperties.put(BackendSettings.backendKey(LuceneIndexSettings.DIRECTORY_ROOT), myFhirLuceneLocation);
		extraProperties.put(BackendSettings.backendKey(LuceneBackendSettings.LUCENE_VERSION), "LUCENE_CURRENT");

		return extraProperties;
	}

	/**
	 * Bean which validates incoming requests
	 * @param theInstanceValidator
	 */
	@Bean
	@Lazy
	public RequestValidatingInterceptor requestValidatingInterceptor(IValidatorModule theInstanceValidator) {
		RequestValidatingInterceptor requestValidator = new RequestValidatingInterceptor();
		requestValidator.setFailOnSeverity(null);
		requestValidator.setAddResponseHeaderOnSeverity(null);
		requestValidator.setAddResponseOutcomeHeaderOnSeverity(ResultSeverityEnum.INFORMATION);
		requestValidator.addValidatorModule(theInstanceValidator);
		requestValidator.setIgnoreValidatorExceptions(true);

		return requestValidator;
	}

	/**
	 * This lets the "@Value" fields reference properties from the properties file
	 */
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

//	@Bean(autowire = Autowire.BY_TYPE)
//	public IServerInterceptor subscriptionSecurityInterceptor() {
//		return new SubscriptionsRequireManualActivationInterceptorDstu2();
//	}

}
