package com.planbow.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.planbow.documents.planboard.ActionItemAggregation;
import com.planbow.documents.planboard.ActionItems;
import com.planbow.documents.planboard.PlanboardNodes;
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
import org.apache.catalina.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.planbow.utility.PlanbowUtility.formatStringToInstant;

@Service
@Transactional
@Log4j2
public class ActionItemApiService {

    private ObjectMapper objectMapper;
    private ActionItemApiRepository actionItemApiRepository;
    private PlanbowHibernateRepository planbowHibernateRepository;

    private TaskApiRepository taskApiRepository;


    @Autowired
    public void setTaskApiRepository(TaskApiRepository taskApiRepository) {
        this.taskApiRepository = taskApiRepository;
    }

    @Autowired
    public void setPlanbowHibernateRepository(PlanbowHibernateRepository planbowHibernateRepository) {
        this.planbowHibernateRepository = planbowHibernateRepository;
    }

    @Autowired
    public void setActionItemApiRepository(ActionItemApiRepository actionItemApiRepository) {
        this.actionItemApiRepository = actionItemApiRepository;
    }

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ResponseEntity<ResponseJsonHandler> getActionItems(String userId, String planboardId, String nodeId) {
        List<ActionItemAggregation> actionItems  = actionItemApiRepository.getActionItems(planboardId,nodeId);
        if(CollectionUtils.isEmpty(actionItems))
            return ResponseJsonUtil.getResponse(HttpStatus.NOT_FOUND,"No action items found for this node");

        Set<String> assignedTos=actionItems.stream().map(ActionItemAggregation::getAssignedTo).filter(assignedTo ->!StringUtils.isEmpty(assignedTo)).collect(Collectors.toSet());
        Set<String> userIds  = actionItems.stream().map(ActionItemAggregation::getUserId).collect(Collectors.toSet());
        assignedTos.addAll(userIds);
        List<UserEntity> userEntities = planbowHibernateRepository.getUserEntities(null,new ArrayList<>(assignedTos));
        ArrayNode data  = objectMapper.createArrayNode();
        actionItems.forEach(e->{
                    Set<String> ids = e.getChildren().stream().map(ActionItems::getId).collect(Collectors.toSet());
                    ObjectNode node  = objectMapper.createObjectNode();

                    node.put("id",e.getId());
                    node.put("title",e.getTitle());
                    node.put("description",e.getDescription());
                    node.put("planboardId",e.getPlanboardId());
                    node.put("nodeId",e.getNodeId());
                    node.put("parentId",e.getParentId());
                    node.set("endDate",objectMapper.valueToTree(e.getEndDate()));
                    node.set("createdOn",objectMapper.valueToTree(e.getCreatedOn()));

                    node.put("status",e.getStatus());
                    node.put("priority",e.getPriority());
                    node.set("endDate",objectMapper.valueToTree(e.getEndDate()));
                    node.set("createdOn",objectMapper.valueToTree(e.getCreatedOn()));

                    node.set("childIds",objectMapper.valueToTree(ids));

                    node.put("taskCount",taskApiRepository.getTaskCount(e.getId()));

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
                }
        );
        return ResponseJsonUtil.getResponse(HttpStatus.OK,data);
    }

    public ResponseEntity<ResponseJsonHandler> addActionItem
            (String userId, String planboardId, String nodeId, String title, RequestJsonHandler requestJsonHandler) {

        if(actionItemApiRepository.isActionItemExists(title, planboardId,nodeId)){
            return ResponseJsonUtil.getResponse(HttpStatus.CONFLICT,"Provided action item name already exists in this node");
        }

        ActionItems actionItems  = new ActionItems();
        actionItems.setTitle(title);
        actionItems.setDescription(requestJsonHandler.getStringValue("description"));

        actionItems.setPlanboardId(planboardId);
        actionItems.setUserId(userId);
        actionItems.setNodeId(nodeId);
        actionItems.setParentId(requestJsonHandler.getStringValue("parentId"));

        actionItems.setStatus(ActionItems.STATUS_IN_PROGRESS);
        actionItems.setPriority(ActionItems.PRIORITY_LOW);

        String endDate  = requestJsonHandler.getStringValue("endDate");
        if(!StringUtils.isEmpty(endDate)){
            actionItems.setEndDate(formatStringToInstant(endDate,null));
        }

        String assignedTo  = requestJsonHandler.getStringValue("assignedTo");
        if(!StringUtils.isEmpty(assignedTo)){
            actionItems.setAssignedTo(assignedTo);
        }

        actionItems.setActive(true);
        actionItems.setCreatedOn(Instant.now());
        actionItems.setModifiedOn(Instant.now());
        actionItems  = actionItemApiRepository.saveOrUpdateActionItems(actionItems);
        ObjectNode data  = objectMapper.createObjectNode();
        data.put("id",actionItems.getId());
        return ResponseJsonUtil.getResponse(HttpStatus.OK,data);
    }

    public ResponseEntity<ResponseJsonHandler> updateActionItem(String userId, String actionItemId, RequestJsonHandler requestJsonHandler) {
        ActionItems actionItems  = actionItemApiRepository.getActionItems(actionItemId);
        if(actionItems==null)
            return ResponseJsonUtil.getResponse(HttpStatus.NOT_FOUND,"Provided actionItemId does not exists");

        String title  = requestJsonHandler.getStringValue("title");
        if(!StringUtils.isEmpty(title)){
            if(!actionItems.getTitle().equals(title)){
                if(actionItemApiRepository.isActionItemExists(title, actionItems.getPlanboardId(),actionItems.getNodeId())){
                    return ResponseJsonUtil.getResponse(HttpStatus.CONFLICT,"Provided action item name already exists in this node");
                }else{
                    actionItems.setTitle(title.trim());
                }
            }
        }

        String description  = requestJsonHandler.getStringValue("description");
        if(!StringUtils.isEmpty(description)){
            actionItems.setDescription(description.trim());
        }

        String parentId  = requestJsonHandler.getStringValue("parentId");
        if(!StringUtils.isEmpty(parentId)){
            actionItems.setParentId(parentId.trim());
        }

        String priority  = requestJsonHandler.getStringValue("priority");
        if(!StringUtils.isEmpty(priority)){
            actionItems.setPriority(priority);
        }

        String endDate  = requestJsonHandler.getStringValue("endDate");
        if(!StringUtils.isEmpty(endDate)){
            actionItems.setEndDate(formatStringToInstant(endDate,null));
        }

        String assignedTo  = requestJsonHandler.getStringValue("assignedTo");
        if(!StringUtils.isEmpty(assignedTo)){
            actionItems.setAssignedTo(assignedTo);
        }

        actionItems.setModifiedOn(Instant.now());
        actionItems  = actionItemApiRepository.saveOrUpdateActionItems(actionItems);
        ObjectNode data  = objectMapper.createObjectNode();
        data.put("id",actionItems.getId());
        return ResponseJsonUtil.getResponse(HttpStatus.OK,data);
    }

    public ResponseEntity<ResponseJsonHandler> deleteActionItem(String userId, String actionItemId) {
        ActionItems actionItems  = actionItemApiRepository.getActionItems(actionItemId);
        if(actionItems==null)
            return ResponseJsonUtil.getResponse(HttpStatus.NOT_FOUND,"Provided actionItemId does not exists");
        actionItems.setActive(false);
        actionItemApiRepository.saveOrUpdateActionItems(actionItems);
        return ResponseJsonUtil.getResponse(HttpStatus.OK,"Action item deleted successfully");
    }
}
