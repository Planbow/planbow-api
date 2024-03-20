package com.planbow.repository;

import com.planbow.documents.planboard.ActionItemAggregation;
import com.planbow.documents.planboard.ActionItems;
import com.planbow.documents.planboard.PlanboardNodesAggregation;
import com.planbow.util.data.support.repository.MongoDbRepository;
import lombok.extern.log4j.Log4j2;
import org.bson.Document;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Repository
@Transactional
@Log4j2
public class ActionItemApiRepository extends MongoDbRepository {

    public ActionItems getActionItems(String id){
        return (ActionItems) getDocument(ActionItems.class,id);
    }

/*    public List<ActionItems> getActionItems(String planboardId,String nodeId){
        Query query  = new Query();
        Criteria criteria = Criteria.where("active").is(true);
        criteria = criteria.and("planboardId").is(planboardId);
        criteria = criteria.and("nodeId").is(nodeId);
        query.addCriteria(criteria);
        return (List<ActionItems>) getDocuments(ActionItems.class,query);
    }*/

    public List<ActionItemAggregation>  getActionItems(String planboardId,String nodeId){
        Query query= new Query();
        Criteria criteria=  Criteria.where("active").is(true);
        criteria= criteria.and("planboardId").is(planboardId);
        criteria= criteria.and("nodeId").is(nodeId);
        AddFieldsOperation addFieldsOperation  = Aggregation.addFields().addField("tmpId").withValue(new Document("$toString","$_id")).build();
        MatchOperation matchOperation  = Aggregation.match(criteria);
        AggregationOperation graphLookupOperation = Aggregation.graphLookup("actionItems")
                .startWith("tmpId")
                .connectFrom("tmpId")
                .connectTo("parentId")
                .as("children");
        TypedAggregation<Document> aggregation = TypedAggregation.newAggregation(Document.class,matchOperation,addFieldsOperation, graphLookupOperation);
        return mongoTemplate.aggregate(aggregation, "actionItems", ActionItemAggregation.class).getMappedResults();
    }
}
