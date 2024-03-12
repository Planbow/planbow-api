package com.planbow.repository;

import com.planbow.documents.global.Organization;
import com.planbow.documents.planboard.Planboard;
import com.planbow.documents.workspace.Workspace;
import com.planbow.util.data.support.repository.MongoDbRepository;
import io.micrometer.common.util.StringUtils;
import jakarta.transaction.Transactional;
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
@Transactional
public class WorkspaceApiRepository extends MongoDbRepository {

    public boolean isWorkspaceExists(String name,String userId,String organizationId){
        Query query= new Query();
        Criteria criteria=  Criteria.where("name").regex("^"+name+"$","i");
        criteria = criteria.and("userId").is(userId);
        criteria = criteria.and("organizationId").is(organizationId);
        query.addCriteria(criteria);
        return mongoTemplate.exists(query, Workspace.class);
    }

    public Workspace saveOrUpdateWorkspace(Workspace workspace){
        return (Workspace) saveOrUpdateDocument(workspace);
    }

    public List<Workspace> getWorkspaces(String organizationId,int index,int itemsPerIndex,String search, String sort,String userId){
        String patternRegex = ".*" + search + ".*";
        Pageable pageable = PageRequest.of(index, itemsPerIndex);
        Query query = new Query();
        Criteria criteria= Criteria.where("userId").is(userId);
        criteria= criteria.and("organizationId").is(organizationId);
        if(!StringUtils.isEmpty(search)){
            criteria=criteria.andOperator(
                    Criteria.where("name").regex(search,"i"));
        }
        query.addCriteria(criteria);

        if(!StringUtils.isEmpty(sort)){
            if(sort.equals("Created With"))
                query.with(Sort.by(Sort.Direction.DESC, "createdOn"));
            if(sort.equals("Alphabetical"))
                query.with(Sort.by(Sort.Direction.ASC, "name"));
        }else{
            query.with(Sort.by(Sort.Direction.DESC, "createdOn"));
        }

        query.with(pageable);
        return (List<Workspace>) getDocuments(Workspace.class,query);
    }

    public Workspace getWorkSpaceById(String id){
        return (Workspace) getDocument(Workspace.class,id);
    }

    public long getPlanboardCount(String workspaceId,String userId){
        Query query = new Query();
        Criteria criteria= Criteria.where("userId").is(userId);
        criteria= criteria.and("workspaceId").is(workspaceId);
        query.addCriteria(criteria);
        return mongoTemplate.count(query, Planboard.class);
    }
}
