package com.planbow.repository;

import com.planbow.documents.planboard.Planboard;
import com.planbow.documents.planboard.PlanboardNodes;
import com.planbow.util.data.support.repository.MongoDbRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;


@Repository
@Transactional
@Log4j2
public class NodeApiRepository extends MongoDbRepository {

    public PlanboardNodes saveOrUpdatePlanboardNodes(PlanboardNodes planboardNodes){
        return (PlanboardNodes) saveOrUpdateDocument(planboardNodes);
    }

    public PlanboardNodes getPlanboardNodeByParentId(String parentId){
        Query query= new Query();
        Criteria criteria=  Criteria.where("active").is(true);
        criteria= criteria.and("parentId").is(parentId);
        query.addCriteria(criteria);
        return mongoTemplate.findOne(query,PlanboardNodes.class);
    }

    public PlanboardNodes getPlanboardNode(String id){
        return (PlanboardNodes) getDocument(PlanboardNodes.class,id);
    }

    public boolean isPlanboardNodeExists(String title,String planboardId){
        Query query= new Query();
        Criteria criteria=  Criteria.where("title").regex("^"+title+"$","i");
        criteria= criteria.and("planboardId").is(planboardId);
        criteria= criteria.and("active").is(true);
        query.addCriteria(criteria);
        return mongoTemplate.exists(query, PlanboardNodes.class);
    }

}
