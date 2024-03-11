package com.planbow.datasource.hibernate;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
public class HibernateDataSourceConnectivity {

    private HibernateDataSourceConfig dataSourceConfig;

    private DataSourcePoolConfig dataSourcePoolConfig;
    private LocalContainerEntityManagerFactoryBean entityManagerFactory;

    @Autowired
    public void setDataSourceConfig(HibernateDataSourceConfig dataSourceConfig) {
        this.dataSourceConfig = dataSourceConfig;
    }

    @Autowired
    public void setDataSourcePoolConfig(DataSourcePoolConfig dataSourcePoolConfig) {
        this.dataSourcePoolConfig = dataSourcePoolConfig;
    }

    @Autowired
    public void setEntityManagerFactory(LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Bean
    DataSource dataSource() {
        DriverManagerDataSource driverManagerDataSource = new DriverManagerDataSource();
        driverManagerDataSource.setDriverClassName(dataSourceConfig.getDriver());
        driverManagerDataSource.setUrl(dataSourceConfig.getConnectionUrl()+"&allowPublicKeyRetrieval=true");
        driverManagerDataSource.setUsername(dataSourceConfig.getUserName());
        driverManagerDataSource.setPassword(dataSourceConfig.getPassword());
        //driverManagerDataSource.setConnectionProperties(initializePoolProperties());
        return driverManagerDataSource;
    }

    private Properties getHibernateProperties() {
        Properties properties = new Properties();
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        properties.put("hibernate.show_sql", false);
        properties.put("hibernate.id.new_generator_mappings", false);
        properties.put("hibernate.connection.zeroDateTimeBehavior", "convertToNull");
        return properties;
    }


    @Bean
    @DependsOn({"dataSource"})
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        localContainerEntityManagerFactoryBean.setDataSource(dataSource());
        localContainerEntityManagerFactoryBean.setPackagesToScan("com.planbow.entities");
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        localContainerEntityManagerFactoryBean.setJpaVendorAdapter(vendorAdapter);
        localContainerEntityManagerFactoryBean.setJpaProperties(getHibernateProperties());
        return localContainerEntityManagerFactoryBean;
    }

    @Bean
    public JpaTransactionManager transactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(
                entityManagerFactory.getObject());
        return transactionManager;
    }


    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
        return new PersistenceExceptionTranslationPostProcessor();
    }

    protected Properties initializePoolProperties() {
        Properties properties = new Properties();
        properties.setProperty("type", dataSourcePoolConfig.getType());
        properties.setProperty("factory", dataSourcePoolConfig.getFactory());
        properties.setProperty("initialSize", String.valueOf(dataSourcePoolConfig.getInitialSize()));
        properties.setProperty("maxWaitMillis", String.valueOf(dataSourcePoolConfig.getMaxWaitMillis()));
        properties.setProperty("maxTotal", String.valueOf(dataSourcePoolConfig.getMaxTotal()));
        properties.setProperty("maxIdle", String.valueOf(dataSourcePoolConfig.getMaxIdle()));
        properties.setProperty("minIdle", String.valueOf(dataSourcePoolConfig.getMinIdle()));
        properties.setProperty("removeAbandoned", String.valueOf(dataSourcePoolConfig.isRemoveAbandoned()));
        properties.setProperty("removeAbandonedTimeout", String.valueOf(dataSourcePoolConfig.getRemoveAbandonedTimeout()));
        properties.setProperty("validationQuery", dataSourcePoolConfig.getValidationQuery());
        properties.setProperty("validationInterval", String.valueOf(dataSourcePoolConfig.getValidationInterval()));
        properties.setProperty("testOnBorrow", String.valueOf(dataSourcePoolConfig.isTestOnBorrow()));
        properties.setProperty("timeBetweenEvictionRunsMillis", String.valueOf(dataSourcePoolConfig.getTimeBetweenEvictionRunsMillis()));
        properties.setProperty("minEvictableIdleTimeMillis", String.valueOf(dataSourcePoolConfig.getMinEvictableIdleTimeMillis()));
        properties.setProperty("useSSL", String.valueOf(dataSourcePoolConfig.isUseSSL()));
        return properties;
    }

}
