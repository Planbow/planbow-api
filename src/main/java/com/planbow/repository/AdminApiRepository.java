package com.planbow.repository;

import com.planbow.documents.core.Domain;
import com.planbow.documents.core.SubDomain;
import com.planbow.documents.global.Organization;
import com.planbow.util.data.support.repository.MongoDbRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;


@Repository
@Log4j2
public class AdminApiRepository extends MongoDbRepository {

    public boolean isDomainExists(String name){
        Query query= new Query();
        Criteria criteria=  Criteria.where("name").regex("^"+name+"$","i");
        query.addCriteria(criteria);
        return mongoTemplate.exists(query, Domain.class);
    }

    public boolean isSubDomainExists(String name,String domainId){
        Query query= new Query();
        Criteria criteria=  Criteria.where("name").regex("^"+name+"$","i");
        criteria = criteria.and("domainId").is(domainId);
        query.addCriteria(criteria);
        return mongoTemplate.exists(query, SubDomain.class);
    }

    public Domain saveOrUpdateDomain(Domain domain){
        return (Domain) saveOrUpdateDocument(domain);
    }

    public SubDomain saveOrUpdateSubDomain(SubDomain domain){
        return (SubDomain) saveOrUpdateDocument(domain);
    }

    public Domain getDomainById(String id){
        return (Domain) getDocument(Domain.class,id);
    }


    public SubDomain getSubdomainById(String id){
        return (SubDomain) getDocument(SubDomain.class,id);
    }
}
