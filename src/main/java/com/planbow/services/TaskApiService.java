package com.planbow.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.internal.bulk.UpdateRequest;
import com.planbow.documents.planboard.ActionItemAggregation;
import com.planbow.documents.planboard.ActionItems;
import com.planbow.documents.planboard.Tasks;
import com.planbow.entities.user.UserEntity;
import com.planbow.repository.ActionItemApiRepository;
import com.planbow.repository.PlanbowHibernateRepository;
import com.planbow.repository.TaskApiRepository;
import com.planbow.util.json.handler.request.RequestJsonHandler;
import com.planbow.util.json.handler.response.ResponseJsonHandler;
import com.planbow.util.json.handler.response.util.ResponseJsonUtil;
import com.planbow.utility.PlanbowUtility;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.config.Task;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.planbow.documents.planboard.ActionItems.STATUS_IN_TODO;
import static com.planbow.documents.planboard.Tasks.STATUS_COMPLETED;
import static com.planbow.documents.planboard.Tasks.STATUS_IN_PROGRESS;
import static com.planbow.utility.PlanbowUtility.*;

@Service
@Log4j2
public class TaskApiService {

    private ObjectMapper objectMapper;
    private PlanbowHibernateRepository planbowHibernateRepository;
    private TaskApiRepository taskApiRepository;

    private ActionItemApiRepository actionItemApiRepository;


    @Autowired
    public void setActionItemApiRepository(ActionItemApiRepository actionItemApiRepository) {
        this.actionItemApiRepository = actionItemApiRepository;
    }

    @Autowired
    public void setPlanbowHibernateRepository(PlanbowHibernateRepository planbowHibernateRepository) {
        this.planbowHibernateRepository = planbowHibernateRepository;
    }

    @Autowired
    public void setTaskApiRepository(TaskApiRepository taskApiRepository) {
        this.taskApiRepository = taskApiRepository;
    }

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ResponseEntity<ResponseJsonHandler> addTask(String userId, String planboardId, String nodeId, String actionItemId, String title, RequestJsonHandler requestJsonHandler) {

        if(taskApiRepository.isTaskExists(title,planboardId,nodeId,actionItemId)){
            return ResponseJsonUtil.getResponse(HttpStatus.CONFLICT,"Provided task name name exists in this action item");
        }

        ActionItems actionItems  = actionItemApiRepository.getActionItems(actionItemId);
        if(actionItems==null)
            return ResponseJsonUtil.getResponse(HttpStatus.NOT_FOUND,"Provided actionItemId does not exists");

        Tasks tasks  = new Tasks();
        tasks.setTitle(title);
        tasks.setDescription(requestJsonHandler.getStringValue("description"));

        tasks.setPlanboardId(planboardId);
        tasks.setNodeId(nodeId);
        tasks.setActionItemId(actionItemId);
        tasks.setUserId(userId);
        tasks.setParentId(requestJsonHandler.getStringValue("parentId"));

        tasks.setStatus(STATUS_IN_TODO);
        Integer progress  = requestJsonHandler.getIntegerValue("progress");
        if(progress!=null){
            tasks.setProgress(progress);
            if(progress==0)
                tasks.setStatus(STATUS_IN_TODO);
            else if(progress>0 && progress<=99)
                tasks.setStatus(STATUS_IN_PROGRESS);
            else
                tasks.setStatus(STATUS_COMPLETED);
        }
        tasks.setPriority(ActionItems.PRIORITY_LOW);

        String endDate  = requestJsonHandler.getStringValue("endDate");
        if(!StringUtils.isEmpty(endDate)){
            tasks.setEndDate(formatStringToInstant(endDate,null));
        }

        String assignedTo  = requestJsonHandler.getStringValue("assignedTo");
        if(!StringUtils.isEmpty(assignedTo)){
            tasks.setAssignedTo(assignedTo);
        }

        tasks.setActive(true);
        tasks.setCreatedOn(Instant.now());
        tasks.setModifiedOn(Instant.now());
        tasks  = taskApiRepository.saveOrUpdateTasks(tasks);

        List<Tasks> tasksList = taskApiRepository.getTasks(actionItemId,userId);
        handleActionItemStatus(tasksList, actionItems);
        actionItemApiRepository.saveOrUpdateActionItems(actionItems);
        ObjectNode data  = objectMapper.createObjectNode();
        data.put("id",tasks.getId());
        ObjectNode actionItem  = objectMapper.createObjectNode();
        actionItem.put("id",actionItems.getId());
        actionItem.put("status",actionItems.getStatus());
        data.set("actionItem",actionItem);
        return ResponseJsonUtil.getResponse(HttpStatus.OK,data);
    }

    public ResponseEntity<ResponseJsonHandler> getTasks(String userId, String actionItemId) {

        List<Tasks> tasks =taskApiRepository.getTasks(actionItemId,userId);
        if(CollectionUtils.isEmpty(tasks))
            return ResponseJsonUtil.getResponse(HttpStatus.NOT_FOUND,"No task found for given actionItemId");
        ArrayNode data  = objectMapper.createArrayNode();
        Set<String> assignedTos=tasks.stream().map(Tasks::getAssignedTo).filter(assignedTo ->!StringUtils.isEmpty(assignedTo)).collect(Collectors.toSet());
        Set<String> userIds  = tasks.stream().map(Tasks::getUserId).collect(Collectors.toSet());
        assignedTos.addAll(userIds);
        List<UserEntity> userEntities = planbowHibernateRepository.getUserEntities(null,new ArrayList<>(assignedTos));
        tasks.forEach(e->{
            ObjectNode node  = objectMapper.createObjectNode();

            node.put("id",e.getId());

            node.put("title",e.getTitle());
            node.put("description",e.getDescription());

            node.put("planboardId",e.getPlanboardId());
            node.put("nodeId",e.getNodeId());
            node.put("actionItemId",e.getActionItemId());
            node.put("parentId",e.getParentId());

            node.put("status",e.getStatus());
            node.put("priority",e.getPriority());
            node.put("progress",e.getProgress());
            node.set("endDate",objectMapper.valueToTree(e.getEndDate()));
            if(e.getEndDate()!=null){
                if(isDatePassed(e.getEndDate())){
                    node.put("status", Tasks.STATUS_DELAYED);
                    new Thread(()-> taskApiRepository.updateTaskStatus(e.getId(),Tasks.STATUS_DELAYED));
                }
            }

            node.set("createdOn",objectMapper.valueToTree(e.getCreatedOn()));
            ObjectNode createdBy  = objectMapper.createObjectNode();
            UserEntity userEntity  = PlanbowUtility.getUserEntity(userEntities,Long.valueOf(e.getUserId()));
            createdBy.put("id",userEntity.getId());
            createdBy.put("name",userEntity.getName());
            createdBy.put("email",userEntity.getEmail());
            createdBy.put("profilePic",userEntity.getProfilePic());
            createdBy.put("gender",userEntity.getGender());
            node.set("createdBy",createdBy);


            ObjectNode assignedTo  = objectMapper.createObjectNode();
            userEntity= PlanbowUtility.getUserEntity(userEntities,!StringUtils.isEmpty(e.getAssignedTo())? Long.parseLong(e.getAssignedTo()):0L);
            if(userEntity!=null){
                assignedTo.put("id",userEntity.getId());
                assignedTo.put("name",userEntity.getName());
                assignedTo.put("email",userEntity.getEmail());
                assignedTo.put("profilePic",userEntity.getProfilePic());
                assignedTo.put("gender",userEntity.getGender());
                node.set("assignedTo",assignedTo);
            }else{
                node.set("assignedTo",objectMapper.valueToTree(null));
            }
            data.add(node);

        });

        return ResponseJsonUtil.getResponse(HttpStatus.OK,data);
    }

    public ResponseEntity<ResponseJsonHandler> updateTask(String userId, String taskId,RequestJsonHandler requestJsonHandler) {

        Tasks tasks  = taskApiRepository.getTasks(taskId);
        if(tasks==null)
            return ResponseJsonUtil.getResponse(HttpStatus.NOT_FOUND,"Provided taskId does not exists");

        String title  = requestJsonHandler.getStringValue("title");
        if(!StringUtils.isEmpty(title)){
            if(!tasks.getTitle().equals(title)){
                if(taskApiRepository.isTaskExists(title, tasks.getPlanboardId(),tasks.getNodeId(),tasks.getActionItemId())){
                    return ResponseJsonUtil.getResponse(HttpStatus.CONFLICT,"Provided task item name already exists in this action item");
                }else{
                    tasks.setTitle(title.trim());
                }
            }
        }

        String description  = requestJsonHandler.getStringValue("description");
        if(!StringUtils.isEmpty(description)){
            tasks.setDescription(description.trim());
        }

        String priority  = requestJsonHandler.getStringValue("priority");
        if(!StringUtils.isEmpty(priority)){
            tasks.setPriority(priority);
        }

        String endDate  = requestJsonHandler.getStringValue("endDate");
        if(!StringUtils.isEmpty(endDate)){
            tasks.setEndDate(formatStringToInstant(endDate,null));
        }

        Integer progress  = requestJsonHandler.getIntegerValue("progress");
        if(progress!=null){
            tasks.setProgress(progress);
            if(progress==0)
                tasks.setStatus(STATUS_IN_TODO);
            else if(progress>0 && progress<=99)
                tasks.setStatus(STATUS_IN_PROGRESS);
            else
                tasks.setStatus(STATUS_COMPLETED);

        }else{
            tasks.setStatus(STATUS_IN_TODO);
        }


        String assignedTo  = requestJsonHandler.getStringValue("assignedTo");
        if(!StringUtils.isEmpty(assignedTo)){
            tasks.setAssignedTo(assignedTo);
        }

        tasks.setModifiedOn(Instant.now());
        tasks  = taskApiRepository.saveOrUpdateTasks(tasks);

        List<Tasks> tasksList = taskApiRepository.getTasks(tasks.getActionItemId(),userId);
        ActionItems actionItems  = actionItemApiRepository.getActionItems(tasks.getActionItemId());
        handleActionItemStatus(tasksList, actionItems);
        actionItemApiRepository.saveOrUpdateActionItems(actionItems);

        ObjectNode data  = objectMapper.createObjectNode();
        data.put("id",tasks.getId());
        ObjectNode actionItem  = objectMapper.createObjectNode();
        actionItem.put("id",actionItems.getId());
        actionItem.put("status",actionItems.getStatus());
        data.set("actionItem",actionItem);
        return ResponseJsonUtil.getResponse(HttpStatus.OK,data);
    }


    public ResponseEntity<ResponseJsonHandler> deleteTask(String userId, String taskId) {
        Tasks tasks  = taskApiRepository.getTasks(taskId);
        if(tasks==null)
            return ResponseJsonUtil.getResponse(HttpStatus.NOT_FOUND,"Provided taskId does not exists");
        tasks.setActive(false);
        taskApiRepository.saveOrUpdateTasks(tasks);
        List<Tasks> tasksList = taskApiRepository.getTasks(tasks.getActionItemId(),userId);
        ActionItems actionItems  = actionItemApiRepository.getActionItems(tasks.getActionItemId());
        handleActionItemStatus(tasksList, actionItems);
        actionItemApiRepository.saveOrUpdateActionItems(actionItems);

        ObjectNode data = objectMapper.createObjectNode();
        ObjectNode actionItem  = objectMapper.createObjectNode();
        actionItem.put("id",actionItems.getId());
        actionItem.put("status",actionItems.getStatus());
        data.set("actionItem",actionItem);
        data.put("id",tasks.getId());
        data.put("message","Task deleted successfully");
        return ResponseJsonUtil.getResponse(HttpStatus.OK,data);
    }

}
