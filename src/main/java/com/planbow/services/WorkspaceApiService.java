package com.planbow.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.planbow.documents.workspace.Workspace;
import com.planbow.repository.WorkspaceApiRepository;
import com.planbow.util.json.handler.response.ResponseJsonHandler;
import com.planbow.util.json.handler.response.util.ResponseJsonUtil;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@Log4j2
@Transactional
public class WorkspaceApiService {
    private WorkspaceApiRepository workspaceApiRepository;

    @Autowired
    public void setWorkspaceApiRepository(WorkspaceApiRepository workspaceApiRepository) {
        this.workspaceApiRepository = workspaceApiRepository;
    }

    private ObjectMapper objectMapper;

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ResponseEntity<ResponseJsonHandler> createWorkspace(String workspaceName,String organizationId, String userId) {

        if(workspaceApiRepository.isWorkspaceExists(workspaceName,userId)){
            return ResponseJsonUtil.getResponse(HttpStatus.CONFLICT,"Provided workspace name already exists");
        }
        Workspace workspace  = new Workspace();
        workspace.setCreatedOn(Instant.now());
        workspace.setModifiedOn(Instant.now());
        workspace.setActive(true);
        workspace.setUserId(userId);
        workspace.setOrganizationId(organizationId);
        workspace.setName(workspaceName);
        workspace = workspaceApiRepository.saveOrUpdateWorkspace(workspace);
        ObjectNode data  =objectMapper.createObjectNode();
        data.put("id",workspace.getId());
        data.put("name",workspace.getName());
        return ResponseJsonUtil.getResponse(HttpStatus.OK,data);
    }

    public ResponseEntity<ResponseJsonHandler> getWorkspaces(String organizationId,int index,int itemsPerIndex,String search,String sort, String userId) {
        List<Workspace> workspaces  = workspaceApiRepository.getWorkspaces(organizationId,index,itemsPerIndex,search,sort,userId);
        if(workspaces.isEmpty()){
            return ResponseJsonUtil.getResponse(HttpStatus.NOT_FOUND,"No workspace found");
        }
        ArrayNode data  = objectMapper.createArrayNode();
        workspaces.forEach(e->{
            ObjectNode node = objectMapper.createObjectNode();
            node.put("id",e.getId());
            node.put("name",e.getName());
            node.put("description",e.getDescription());
            node.put("active",e.isActive());
            node.put("pinned",e.isPinned());
            data.add(node);
        });
        return ResponseJsonUtil.getResponse(HttpStatus.OK,data);
    }
}
