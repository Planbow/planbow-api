package com.planbow.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.planbow.documents.planboard.ActionItemAggregation;
import com.planbow.documents.planboard.ActionItems;
import com.planbow.documents.planboard.Tasks;
import com.planbow.entities.user.UserEntity;
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
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import static com.planbow.utility.PlanbowUtility.formatStringToInstant;

@Service
@Log4j2
public class TaskApiService {

    private ObjectMapper objectMapper;
    private PlanbowHibernateRepository planbowHibernateRepository;
    private TaskApiRepository taskApiRepository;


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

        Tasks tasks  = new Tasks();
        tasks.setTitle(title);
        tasks.setDescription(requestJsonHandler.getStringValue("description"));

        tasks.setPlanboardId(planboardId);
        tasks.setNodeId(nodeId);
        tasks.setActionItemId(actionItemId);
        tasks.setUserId(userId);
        tasks.setParentId(requestJsonHandler.getStringValue("parentId"));

        tasks.setStatus(ActionItems.STATUS_IN_PROGRESS);
        tasks.setPriority(ActionItems.PRIORITY_LOW);

        String endDate  = requestJsonHandler.getStringValue("endDate");
        if(!StringUtils.isEmpty(endDate)){
            tasks.setEndDate(formatStringToInstant(endDate,null));
        }
        tasks.setActive(true);
        tasks.setCreatedOn(Instant.now());
        tasks.setModifiedOn(Instant.now());
        tasks  = taskApiRepository.saveOrUpdateTasks(tasks);
        ObjectNode data  = objectMapper.createObjectNode();
        data.put("id",tasks.getId());
        return ResponseJsonUtil.getResponse(HttpStatus.OK,data);

    }

    public ResponseEntity<ResponseJsonHandler> getTasks(String userId, String actionItemId) {

        List<Tasks> tasks =taskApiRepository.getTasks(actionItemId,userId);
        if(CollectionUtils.isEmpty(tasks))
            return ResponseJsonUtil.getResponse(HttpStatus.NOT_FOUND,"No task found for given actionItemId");
        ArrayNode data  = objectMapper.createArrayNode();

        List<String> userIds  = tasks.stream().map(Tasks::getUserId).collect(Collectors.toList());
        List<UserEntity> userEntities = planbowHibernateRepository.getUserEntities(null,userIds);

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
            node.put("endDate",PlanbowUtility.formatInstantToString(e.getEndDate(),null));
            node.put("createdOn",PlanbowUtility.formatInstantToString(e.getCreatedOn(),null));

            ObjectNode createdBy  = objectMapper.createObjectNode();
            UserEntity userEntity  = PlanbowUtility.getUserEntity(userEntities,Long.valueOf(e.getUserId()));
            createdBy.put("id",userEntity.getId());
            createdBy.put("name",userEntity.getName());
            createdBy.put("email",userEntity.getEmail());
            createdBy.put("profilePic",userEntity.getProfilePic());
            createdBy.put("gender",userEntity.getGender());
            node.set("createdBy",createdBy);

            data.add(node);

        });

        return ResponseJsonUtil.getResponse(HttpStatus.OK,data);
    }
}
