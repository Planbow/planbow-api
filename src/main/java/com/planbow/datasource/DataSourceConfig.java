package com.planbow.datasource;

import com.planbow.util.data.support.configurations.mongodb.DataSourceConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "data-sources.mongodb")
public class DataSourceConfig extends DataSourceConfiguration {
}
