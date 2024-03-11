package com.planbow.datasource.mongodb;

import com.planbow.util.data.support.configurations.mongodb.DataSourceConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
@Component
@ConfigurationProperties(prefix = "data-sources.planbow")
public class DataSourceConfig extends DataSourceConfiguration {
}
