package com.planbow.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.planbow.documents.planboard.NodeMetaData;
import com.planbow.documents.planboard.Planboard;
import com.planbow.documents.planboard.PlanboardNodes;
import com.planbow.repository.NodeApiRepository;
import com.planbow.repository.PlanboardApiRepository;
import com.planbow.util.json.handler.response.ResponseJsonHandler;
import com.planbow.util.json.handler.response.util.ResponseJsonUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
@Log4j2
public class NodeApiService {

    private ObjectMapper objectMapper;
    private NodeApiRepository nodeApiRepository;
    private PlanboardApiRepository planboardApiRepository;


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



    public ResponseEntity<ResponseJsonHandler> addNode(String userId, String planboardId, String parentId, String title, String description, NodeMetaData nodeMetaData) {
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

        planboardNodes.setMetaData(nodeMetaData);
        planboardNodes.setCreatedOn(Instant.now());
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

    public ResponseEntity<ResponseJsonHandler> connectEdge(String trim, String trim1, String trim2, String trim3) {

        return null;
    }


}
