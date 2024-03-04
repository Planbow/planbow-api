package com.planbow.repository;

import com.planbow.documents.core.Domain;
import com.planbow.documents.core.SubDomain;
import com.planbow.documents.global.Organization;
import com.planbow.documents.workspace.Workspace;
import com.planbow.util.data.support.repository.MongoDbRepository;
import io.micrometer.common.util.StringUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
        Criteria criteria=  Criteria.where("userId").is(userId);
        query.addCriteria(criteria);
        return (List<Organization>) getDocuments(Organization.class,query);
    }

    public Organization saveOrUpdateOrganization(Organization organization){
        return (Organization) saveOrUpdateDocument(organization);
    }

    public Organization getOrganizationById(String id){
        return (Organization) getDocument(Organization.class,id);
    }

    public List<Domain> getDomains( String search){
        Query query = new Query();
        Criteria criteria= Criteria.where("active").is(true);
        if(!StringUtils.isEmpty(search)){
            criteria=criteria.andOperator(
                    Criteria.where("name").regex(search,"i"));
        }
        query.addCriteria(criteria);
        return (List<Domain>) getDocuments(Domain.class,query);
    }

    public List<SubDomain> getSubDomains(String domainId,String search){
        Query query = new Query();
        Criteria criteria= Criteria.where("active").is(true);
         criteria= criteria.and("domainId").is(domainId);
        if(!StringUtils.isEmpty(search)){
            criteria=criteria.andOperator(
                    Criteria.where("name").regex(search,"i"));
        }
        query.addCriteria(criteria);
        return (List<SubDomain>) getDocuments(SubDomain.class,query);
    }
}
