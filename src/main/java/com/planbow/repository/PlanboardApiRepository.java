package com.planbow.repository;

import com.planbow.documents.global.Organization;
import com.planbow.documents.planboard.*;
import com.planbow.documents.prompts.PromptResults;
import com.planbow.documents.workspace.Workspace;
import com.planbow.util.data.support.repository.MongoDbRepository;
import io.micrometer.common.util.StringUtils;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.bson.Document;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
@Log4j2
@Transactional
public class PlanboardApiRepository extends MongoDbRepository {


    public PromptResults getPromptResult(String domainId,String subdomainId,String scope,String geography){
        Query query = new Query();
        Criteria criteria= Criteria.where("active").is(true);
        criteria= criteria.and("domainId").is(domainId);
        criteria= criteria.and("subdomainId").is(subdomainId);
        if(!StringUtils.isEmpty(scope)){
            criteria=criteria.and("scope").regex("^"+scope+"$","i");
        }
        if(!StringUtils.isEmpty(geography)){
            criteria=criteria.and("geography").regex("^"+geography+"$","i");
        }
        query.addCriteria(criteria);
        return  mongoTemplate.findOne (query,PromptResults.class);
    }

    public boolean isPlanboardExists(String name,String userId,String workspaceId){
        Query query= new Query();
        Criteria criteria=  Criteria.where("name").regex("^"+name+"$","i");
        criteria= criteria.and("userId").is(userId);
        criteria= criteria.and("workspaceId").is(workspaceId);
        query.addCriteria(criteria);
        return mongoTemplate.exists(query,Planboard.class);
    }

    public Planboard saveOrUpdatePlanboard(Planboard planboard){
        return (Planboard) saveOrUpdateDocument(planboard);
    }

    public Planboard  getPlanboardById(String id){
        return (Planboard) getDocument(Planboard.class,id);
    }

    public PromptResults  getPromptResultsById(String id){
        return (PromptResults) getDocument(PromptResults.class,id);
    }

    public PlanboardNodes  saveOrUpdatePlanboardNodes(PlanboardNodes planboardNodes){
        return (PlanboardNodes) saveOrUpdateDocument(planboardNodes);
    }

    public TemporaryPlanboard  getTemporaryPlanboardById(String id){
        return (TemporaryPlanboard) getDocument(TemporaryPlanboard.class,id);
    }

    public List<PlanboardNodesAggregation>  getPlanboardNodes(String planboardId){
        Query query= new Query();
        Criteria criteria=  Criteria.where("active").is(true);
        criteria= criteria.and("planboardId").is(planboardId);
        AddFieldsOperation addFieldsOperation  = Aggregation.addFields().addField("tmpId").withValue(new Document("$toString","$_id")).build();
        MatchOperation matchOperation  = Aggregation.match(criteria);
        AggregationOperation graphLookupOperation = Aggregation.graphLookup("planboardNodes")
                .startWith("tmpId")
                .connectFrom("tmpId")
                .connectTo("parentId")
                .as("children");
        TypedAggregation<Document> aggregation = TypedAggregation.newAggregation(Document.class,matchOperation,addFieldsOperation, graphLookupOperation);
        return mongoTemplate.aggregate(aggregation, "planboardNodes", PlanboardNodesAggregation.class).getMappedResults();
    }

    public PromptResults saveOrUpdatePromptResults(PromptResults promptResults){
        return (PromptResults) saveOrUpdateDocument(promptResults);
    }

    public TemporaryPlanboard saveOrUpdateTemporaryPlanboard(TemporaryPlanboard planboard){
        return (TemporaryPlanboard) saveOrUpdateDocument(planboard);
    }

    public Attachments saveOrUpdateAttachment(Attachments attachments){
        return (Attachments) saveOrUpdateDocument(attachments);
    }

    public void saveEvents(List<Events> events){
        saveDocuments(events);
    }

    public Events getEventByPlanboardId(String planboardId){
        Query query = new Query();
        Criteria criteria= Criteria.where("active").is(true);
        criteria= criteria.and("planboardId").is(planboardId);
        query.addCriteria(criteria);
        return mongoTemplate.findOne(query, Events.class);
    }

    public List<Attachments> getAttachments(String planboardId,String type){
        Query query = new Query();
        Criteria criteria= Criteria.where("active").is(true);
        criteria= criteria.and("planboardId").is(planboardId);
        criteria= criteria.and("type").is(type);
        query.addCriteria(criteria);
        return (List<Attachments>) getDocuments(Attachments.class,query);
    }



}
