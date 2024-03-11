package com.planbow.datasource.hibernate;

import com.planbow.util.data.support.configurations.hibernate.DatasourcePoolConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "data-sources.platform.pool-configuration")
public class DataSourcePoolConfig extends DatasourcePoolConfiguration {

}
