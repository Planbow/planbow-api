package com.planbow.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.planbow.documents.planboard.ActionItemAggregation;
import com.planbow.documents.planboard.ActionItems;
import com.planbow.documents.planboard.PlanboardNodes;
import com.planbow.entities.user.UserEntity;
import com.planbow.repository.ActionItemApiRepository;
import com.planbow.repository.PlanbowHibernateRepository;
import com.planbow.util.json.handler.response.ResponseJsonHandler;
import com.planbow.util.json.handler.response.util.ResponseJsonUtil;
import com.planbow.utility.PlanbowUtility;
import lombok.extern.log4j.Log4j2;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@Log4j2
public class ActionItemApiService {

    private ObjectMapper objectMapper;
    private ActionItemApiRepository actionItemApiRepository;
    private PlanbowHibernateRepository planbowHibernateRepository;


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

        List<String> userIds  = actionItems.stream().map(ActionItemAggregation::getUserId).collect(Collectors.toList());
        List<UserEntity> userEntities = planbowHibernateRepository.getUserEntities(null,userIds);
        ArrayNode data  = objectMapper.createArrayNode();
        actionItems.parallelStream().forEach(e->{
                    Set<String> ids = e.getChildren().parallelStream().map(ActionItems::getId).collect(Collectors.toSet());
                    ObjectNode node  = objectMapper.createObjectNode();

                    node.put("id",e.getId());
                    node.put("title",e.getTitle());
                    node.put("description",e.getDescription());
                    node.put("planboardId",e.getPlanboardId());
                    node.put("nodeId",e.getNodeId());
                    node.put("parentId",e.getParentId());
                    node.put("createdOn",PlanbowUtility.formatInstantToString(e.getCreatedOn(),null));
                    node.set("childIds",objectMapper.valueToTree(ids));

                    ObjectNode createdBy  = objectMapper.createObjectNode();
                    UserEntity userEntity  = PlanbowUtility.getUserEntity(userEntities,Long.valueOf(e.getUserId()));
                    createdBy.put("id",userEntity.getId());
                    createdBy.put("name",userEntity.getName());
                    createdBy.put("email",userEntity.getEmail());
                    createdBy.put("profilePic",userEntity.getProfilePic());
                    createdBy.put("gender",userEntity.getGender());
                    node.set("createdBy",createdBy);

                    data.add(node);
                }
        );
        return ResponseJsonUtil.getResponse(HttpStatus.OK,data);
    }
}
