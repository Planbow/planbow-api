package com.planbow.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.planbow.documents.planboard.NodeMetaData;
import com.planbow.documents.planboard.Planboard;
import com.planbow.documents.planboard.PlanboardNodes;
import com.planbow.documents.planboard.PlanboardNodesAggregation;
import com.planbow.entities.user.UserEntity;
import com.planbow.repository.NodeApiRepository;
import com.planbow.repository.PlanboardApiRepository;
import com.planbow.repository.PlanbowHibernateRepository;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.planbow.utility.PlanbowUtility.formatStringToInstant;

@Service
@Transactional
@Log4j2
public class NodeApiService {

    private ObjectMapper objectMapper;
    private NodeApiRepository nodeApiRepository;
    private PlanboardApiRepository planboardApiRepository;
    private PlanbowHibernateRepository planbowHibernateRepository;


    @Autowired
    public void setPlanbowHibernateRepository(PlanbowHibernateRepository planbowHibernateRepository) {
        this.planbowHibernateRepository = planbowHibernateRepository;
    }

    @Autowired
    public void setPlanboardApiRepository(PlanboardApiRepository planboardApiRepository) {
        this.planboardApiRepository = planboardApiRepository;
    }

    @Autowired
    public void setNodeApiRepository(NodeApiRepository nodeApiRepository) {
        this.nodeApiRepository = nodeApiRepository;
    }

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }



    public ResponseEntity<ResponseJsonHandler> setNodesMetaData(String userId, String planboardId, List<PlanboardNodes> planboardNodes) {
        Planboard planboard  = planboardApiRepository.getPlanboardById(planboardId);
        if(planboard==null)
            return ResponseJsonUtil.getResponse(HttpStatus.NOT_FOUND,"Provided planboardId does not exists");
        if(!planboard.getUserId().equals(userId))
            return ResponseJsonUtil.getResponse(HttpStatus.UNAUTHORIZED,"You are not authorized to access this planboard");
        new Thread(()-> {
            planboardNodes.forEach(e->{
                PlanboardNodes node  = nodeApiRepository.getPlanboardNode(e.getId());
                if(node!=null){
                    node.setMetaData(e.getMetaData());
                    node.setModifiedOn(Instant.now());
                    nodeApiRepository.saveOrUpdatePlanboardNodes(node);
                }
            });
        }).start();
        planboard.setNodeInitialization(true);
        planboardApiRepository.saveOrUpdatePlanboard(planboard);
        return ResponseJsonUtil.getResponse(HttpStatus.OK,"Planboard node's meta data updated successfully");
    }



    public ResponseEntity<ResponseJsonHandler> addNode(String userId, String planboardId, String parentId, String title, String description, NodeMetaData nodeMetaData, RequestJsonHandler requestJsonHandler) {
        Planboard planboard  = planboardApiRepository.getPlanboardById(planboardId);
        if(planboard==null)
            return ResponseJsonUtil.getResponse(HttpStatus.NOT_FOUND,"Provided planboardId does not exists");
        if(!planboard.getUserId().equals(userId))
            return ResponseJsonUtil.getResponse(HttpStatus.UNAUTHORIZED,"You are not authorized to access this planboard");

        if(nodeApiRepository.isPlanboardNodeExists(title, planboardId)){
            return ResponseJsonUtil.getResponse(HttpStatus.CONFLICT,"Provided node name already exists in this planboard");
        }

        if(!StringUtils.isEmpty(parentId)){
            PlanboardNodes parentNode  = nodeApiRepository.getPlanboardNode(parentId);
            if(parentNode==null)
                return ResponseJsonUtil.getResponse(HttpStatus.NOT_FOUND,"Provided parentId does not exists");
        }else{
            PlanboardNodes parentNode  = nodeApiRepository.getPlanboardNodeByParentId(parentId);
            if(parentNode.getParentId()==null)
                return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide parentId");
        }

        PlanboardNodes planboardNodes  = new PlanboardNodes();
        planboardNodes.setPlanboardId(planboardId);
        planboardNodes.setTitle(title);
        planboardNodes.setDescription(description);
        planboardNodes.setParentId(parentId);

        if(nodeMetaData==null){
            nodeMetaData  = new NodeMetaData();
        }

        String assignedTo  = requestJsonHandler.getStringValue("assignedTo");
        if(!StringUtils.isEmpty(assignedTo)){
            planboardNodes.setAssignedTo(assignedTo);
        }

        String endDate  = requestJsonHandler.getStringValue("endDate");
        if(!StringUtils.isEmpty(endDate)){
            planboardNodes.setEndDate(formatStringToInstant(endDate,null));
        }



        planboardNodes.setMetaData(nodeMetaData);
        planboardNodes.setCreatedOn(Instant.now());
        planboardNodes.setModifiedOn(Instant.now());
        planboardNodes.setActive(true);
        planboardNodes  = nodeApiRepository.saveOrUpdatePlanboardNodes(planboardNodes);
        ObjectNode data  = objectMapper.createObjectNode();
        data.put("nodeId",planboardNodes.getId());
        return ResponseJsonUtil.getResponse(HttpStatus.OK,data);
    }
    public ResponseEntity<ResponseJsonHandler> getNodeDetails(String userId, String nodeId) {
        PlanboardNodes planboardNodes  = nodeApiRepository.getPlanboardNode(nodeId);

        if(planboardNodes==null)
            return ResponseJsonUtil.getResponse(HttpStatus.NOT_FOUND,"Provided nodeId does not exists");


        Set<String> userIds=new HashSet<>();
         userIds.add(planboardNodes.getUserId());
        if(!StringUtils.isEmpty(planboardNodes.getAssignedTo()))
            userIds.add(planboardNodes.getAssignedTo());

        List<UserEntity> userEntities = planbowHibernateRepository.getUserEntities(null,new ArrayList<>(userIds));

        ObjectNode node  = objectMapper.createObjectNode();
        node.put("id",planboardNodes.getId());
        node.put("title",planboardNodes.getTitle());
        node.put("description",planboardNodes.getDescription());
        node.put("parentId",planboardNodes.getParentId());
        node.put("planboardId",planboardNodes.getPlanboardId());
        node.set("createdOn",objectMapper.valueToTree(planboardNodes.getCreatedOn()));
        node.set("endDate",objectMapper.valueToTree(planboardNodes.getEndDate()));
        node.set("metaData",objectMapper.valueToTree(planboardNodes.getMetaData()));

        node.put("actionItems",planboardApiRepository.getActionItemCount(planboardNodes.getPlanboardId(),planboardNodes.getId()));
        node.put("subTasks",planboardApiRepository.getTaskCount(planboardNodes.getPlanboardId(),planboardNodes.getId()));
        node.put("events",0);

        ObjectNode createdBy  = objectMapper.createObjectNode();
        UserEntity userEntity  = PlanbowUtility.getUserEntity(userEntities,Long.valueOf(planboardNodes.getUserId()));
        createdBy.put("id",userEntity.getId());
        createdBy.put("name",userEntity.getName());
        createdBy.put("email",userEntity.getEmail());
        createdBy.put("profilePic",userEntity.getProfilePic());
        createdBy.put("gender",userEntity.getGender());
        node.set("createdBy",createdBy);


        ObjectNode assignedTo  = objectMapper.createObjectNode();
        userEntity= PlanbowUtility.getUserEntity(userEntities,!StringUtils.isEmpty(planboardNodes.getAssignedTo())? Long.parseLong(planboardNodes.getAssignedTo()):0L);
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
        return ResponseJsonUtil.getResponse(HttpStatus.OK,node);
    }


    public ResponseEntity<ResponseJsonHandler> updateNode(String userId,String nodeId, String planboardId, String title, String description, NodeMetaData nodeMetaData,RequestJsonHandler requestJsonHandler) {
        Planboard planboard  = planboardApiRepository.getPlanboardById(planboardId);
        if(planboard==null)
            return ResponseJsonUtil.getResponse(HttpStatus.NOT_FOUND,"Provided planboardId does not exists");
        if(!planboard.getUserId().equals(userId))
            return ResponseJsonUtil.getResponse(HttpStatus.UNAUTHORIZED,"You are not authorized to access this planboard");

        PlanboardNodes planboardNodes  = nodeApiRepository.getPlanboardNode(nodeId);
        if(planboardNodes==null)
            return ResponseJsonUtil.getResponse(HttpStatus.NOT_FOUND,"Provided nodeId does not exists");


        if(!StringUtils.isEmpty(title)){
            if(!planboardNodes.getTitle().equalsIgnoreCase(title)){
                if(nodeApiRepository.isPlanboardNodeExists(title, planboardId)){
                    return ResponseJsonUtil.getResponse(HttpStatus.CONFLICT,"Provided node name already exists in this planboard");
                }else{
                    planboardNodes.setTitle(title.trim());
                }
            }
        }

        if(!StringUtils.isEmpty(description)){
            planboardNodes.setDescription(description.trim());
        }

        if(nodeMetaData!=null){
          NodeMetaData metaData= planboardNodes.getMetaData();
          if(metaData==null)
              metaData  = new NodeMetaData();
          if(!StringUtils.isEmpty(nodeMetaData.getColor()))
                  metaData.setColor(nodeMetaData.getColor());

          if(nodeMetaData.getWidth()>=0)
                  metaData.setWidth(nodeMetaData.getWidth());

          if(nodeMetaData.getHeight()>=0)
                metaData.setHeight(nodeMetaData.getHeight());

          if(nodeMetaData.getX_position()>=0)
                metaData.setX_position(nodeMetaData.getX_position());

          if(nodeMetaData.getY_position()>=0)
                metaData.setY_position(nodeMetaData.getY_position());

           planboardNodes.setMetaData(metaData);
        }

        String assignedTo  = requestJsonHandler.getStringValue("assignedTo");
        if(!StringUtils.isEmpty(assignedTo)){
            planboardNodes.setAssignedTo(assignedTo);
        }

        String endDate  = requestJsonHandler.getStringValue("endDate");
        if(!StringUtils.isEmpty(endDate)){
            planboardNodes.setEndDate(formatStringToInstant(endDate,null));
        }

        planboardNodes.setModifiedOn(Instant.now());
        planboardNodes.setActive(true);
        planboardNodes  = nodeApiRepository.saveOrUpdatePlanboardNodes(planboardNodes);
        ObjectNode data  = objectMapper.createObjectNode();
        data.put("nodeId",planboardNodes.getId());
        return ResponseJsonUtil.getResponse(HttpStatus.OK,data);
    }


    public ResponseEntity<ResponseJsonHandler> deleteNode(String userId, String planboardId, String nodeId) {
        Planboard planboard  = planboardApiRepository.getPlanboardById(planboardId);
        if(planboard==null)
            return ResponseJsonUtil.getResponse(HttpStatus.NOT_FOUND,"Provided planboardId does not exists");
        if(!planboard.getUserId().equals(userId))
            return ResponseJsonUtil.getResponse(HttpStatus.UNAUTHORIZED,"You are not authorized to access this planboard");

        PlanboardNodes planboardNodes  = nodeApiRepository.getPlanboardNode(nodeId,true);
        if(planboardNodes==null)
            return ResponseJsonUtil.getResponse(HttpStatus.NOT_FOUND,"Provided nodeId does not exists");
        if(!planboardNodes.getPlanboardId().equals(planboardId))
            return ResponseJsonUtil.getResponse(HttpStatus.NOT_FOUND,"Provided nodeId does not belong to given planboardId");
        if(!planboardNodes.isActive())
            return ResponseJsonUtil.getResponse(HttpStatus.NOT_FOUND,"Provided nodeId does not belong to given planboardId");
        planboardNodes.setActive(false);
        nodeApiRepository.saveOrUpdatePlanboardNodes(planboardNodes);
        return ResponseJsonUtil.getResponse(HttpStatus.OK,"Node successfully deleted");

    }

    public ResponseEntity<ResponseJsonHandler> connectEdge(String userId, String planboardId, String sourceNodeId, String targetNodeId) {

        Planboard planboard  = planboardApiRepository.getPlanboardById(planboardId);
        if(planboard==null)
            return ResponseJsonUtil.getResponse(HttpStatus.NOT_FOUND,"Provided planboardId does not exists");
        if(!planboard.getUserId().equals(userId))
            return ResponseJsonUtil.getResponse(HttpStatus.UNAUTHORIZED,"You are not authorized to access this planboard");

        PlanboardNodes targetNode  = nodeApiRepository.getPlanboardNode(targetNodeId,true);
        if(targetNode==null)
            return ResponseJsonUtil.getResponse(HttpStatus.NOT_FOUND,"Provided targetNodeId does not exists");

        PlanboardNodes sourceNode  = nodeApiRepository.getPlanboardNode(sourceNodeId,true);
        if(sourceNode==null)
            return ResponseJsonUtil.getResponse(HttpStatus.NOT_FOUND,"Provided sourceNodeId does not exists");

        targetNode.setParentId(sourceNode.getId());
        nodeApiRepository.saveOrUpdatePlanboardNodes(targetNode);
        return ResponseJsonUtil.getResponse(HttpStatus.OK,"Node edge successfully connected");
    }
    public ResponseEntity<ResponseJsonHandler> disconnectEdge(String userId, String planboardId, String sourceNodeId, String targetNodeId) {

        Planboard planboard  = planboardApiRepository.getPlanboardById(planboardId);
        if(planboard==null)
            return ResponseJsonUtil.getResponse(HttpStatus.NOT_FOUND,"Provided planboardId does not exists");
        if(!planboard.getUserId().equals(userId))
            return ResponseJsonUtil.getResponse(HttpStatus.UNAUTHORIZED,"You are not authorized to access this planboard");

        PlanboardNodes planboardNodes  = nodeApiRepository.getPlanboardNode(targetNodeId,true);

        if(planboardNodes==null)
            return ResponseJsonUtil.getResponse(HttpStatus.NOT_FOUND,"Provided targetNodeId does not exists");
        if(!StringUtils.isEmpty(planboardNodes.getParentId())){
            if(!planboardNodes.getParentId().equalsIgnoreCase(sourceNodeId)){
                return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Provided sourceNodeId is not parent of targetNodeId");
            }
        }
        planboardNodes.setParentId(null);
        nodeApiRepository.saveOrUpdatePlanboardNodes(planboardNodes);
        return ResponseJsonUtil.getResponse(HttpStatus.OK,"Node edge successfully disconnected");
    }


}
