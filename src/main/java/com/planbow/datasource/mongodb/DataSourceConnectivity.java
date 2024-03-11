package com.planbow.datasource.mongodb;


import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;

import static java.util.Collections.singletonList;

@Configuration
@Log4j2
public class DataSourceConnectivity extends AbstractMongoClientConfiguration {


    @Autowired
    private DataSourceConfig dataSourceConfig;

    @Bean
    public MongoTemplate mongoTemplate() {
        log.info("Initializing MongoTemplate bean");
        return new MongoTemplate(mongoDbFactory());
    }

    @Override
    protected String getDatabaseName() {
        return dataSourceConfig.getDatabase();
    }

    @Override
    protected void configureClientSettings(MongoClientSettings.Builder builder) {
        log.info("Configuring MongoClientSetting Builder");

        if(dataSourceConfig.getHost().startsWith("mongodb+srv://")){
            builder
                    .applyConnectionString(new ConnectionString(dataSourceConfig.getHost()));
        }else{
            builder
                    .credential(MongoCredential.createCredential(dataSourceConfig.getUserName(), dataSourceConfig.getAuthenticatedDatabase(), dataSourceConfig.getPassword().toCharArray()))
                    .applyToClusterSettings(settings -> {
                        settings.hosts(singletonList(new ServerAddress(dataSourceConfig.getHost(), dataSourceConfig.getPort())));
                    });
        }
    }
}
