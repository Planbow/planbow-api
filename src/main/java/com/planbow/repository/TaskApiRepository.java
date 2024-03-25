package com.planbow.repository;

import com.planbow.documents.planboard.ActionItems;
import com.planbow.documents.planboard.Tasks;
import com.planbow.util.data.support.repository.MongoDbRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Repository
@Log4j2
@Transactional
public class TaskApiRepository extends MongoDbRepository {



    public long getTaskCount(String actionItemId){
        Query query= new Query();
        Criteria criteria=  Criteria.where("active").is(true);
        criteria= criteria.and("actionItemId").is(actionItemId);
        query.addCriteria(criteria);
        return mongoTemplate.count(query, Tasks.class);
    }

    public boolean isTaskExists(String title,String planboardId,String nodeId,String actionItemId){
        Query query= new Query();
        Criteria criteria=  Criteria.where("title").is(title);
        criteria= criteria.and("planboardId").is(planboardId);
        criteria= criteria.and("nodeId").is(nodeId);
        criteria= criteria.and("actionItemId").is(actionItemId);
        criteria= criteria.and("active").is(true);
        query.addCriteria(criteria);
        return mongoTemplate.exists(query, Tasks.class);
    }

    public List<Tasks> getTasks(String actionItemId,String userId){
        Query query= new Query();
        Criteria criteria=  Criteria.where("active").is(true);
        criteria= criteria.and("actionItemId").is(actionItemId);
        criteria= criteria.and("userId").is(userId);
        query.addCriteria(criteria);
        return (List<Tasks>) getDocuments(Tasks.class,query);
    }

    public long getTasksByActionItemId(String actionItemId){
        Query query= new Query();
        Criteria criteria=  Criteria.where("active").is(true);
        criteria= criteria.and("actionItemId").is(actionItemId);
        criteria= criteria.and("status").ne(Tasks.STATUS_COMPLETED);
        query.addCriteria(criteria);
        return mongoTemplate.count(query, Tasks.class);
    }

    public void updateTaskStatus(String taskId,String status){
        Query query= new Query();
        Criteria criteria=  Criteria.where("active").is(true);
        criteria= criteria.and("id").is(taskId);
        query.addCriteria(criteria);
        Update update  = new Update();
        update.set("status",status);
        mongoTemplate.updateFirst(query, update, Tasks.class);
    }


    public Tasks saveOrUpdateTasks(Tasks tasks){
        return (Tasks) saveOrUpdateDocument(tasks);
    }

    public Tasks getTasks(String id){
        return (Tasks) getDocument(Tasks.class,id);
    }

}
