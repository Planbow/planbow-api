package com.planbow.repository;

import com.planbow.documents.global.Organization;
import com.planbow.util.data.support.repository.MongoDbRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
@Log4j2
public class GlobalApiRepository extends MongoDbRepository {

    public boolean isOrganizationExists(String name){
        Query query= new Query();
        Criteria criteria=  Criteria.where("name").regex("^"+name+"$","i");
        query.addCriteria(criteria);
        return mongoTemplate.exists(query, Organization.class);
    }

    public List<Organization> getOrganizations(String userId){
        Query query= new Query();
        Criteria criteria=  Criteria.where("createdBy").is(userId);
        query.addCriteria(criteria);
        return (List<Organization>) getDocuments(Organization.class,query);
    }

    public Organization saveOrUpdateOrganization(Organization organization){
        return (Organization) saveOrUpdateDocument(organization);
    }
}
