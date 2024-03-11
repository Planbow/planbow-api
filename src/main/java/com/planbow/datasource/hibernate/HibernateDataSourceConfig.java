package com.planbow.datasource.hibernate;

import com.planbow.util.data.support.configurations.hibernate.DataSourceConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "data-sources.idp")
public class HibernateDataSourceConfig extends DataSourceConfiguration {
}
