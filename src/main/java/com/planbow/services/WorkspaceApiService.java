package com.planbow.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.planbow.documents.global.Organization;
import com.planbow.documents.workspace.Workspace;
import com.planbow.repository.GlobalApiRepository;
import com.planbow.repository.WorkspaceApiRepository;
import com.planbow.util.json.handler.request.RequestJsonHandler;
import com.planbow.util.json.handler.response.ResponseJsonHandler;
import com.planbow.util.json.handler.response.util.ResponseJsonUtil;
import io.micrometer.common.util.StringUtils;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@Log4j2
@Transactional
public class WorkspaceApiService {
    private WorkspaceApiRepository workspaceApiRepository;
    private GlobalApiRepository globalApiRepository;

    @Autowired
    public void setGlobalApiRepository(GlobalApiRepository globalApiRepository) {
        this.globalApiRepository = globalApiRepository;
    }

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

        Organization organization  = globalApiRepository.getOrganizationById(organizationId);
        if(organization==null){
            return ResponseJsonUtil.getResponse(HttpStatus.NOT_FOUND,"Provided organizationId doesn't exists");
        }
        if(!organization.getUserId().equals(userId)){
            return ResponseJsonUtil.getResponse(HttpStatus.UNAUTHORIZED,"Provided organizationId doesn't belong to logged in user");
        }

        if(workspaceApiRepository.isWorkspaceExists(workspaceName,userId,organizationId)){
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

        Organization organization  = globalApiRepository.getOrganizationById(organizationId);
        if(organization==null){
            return ResponseJsonUtil.getResponse(HttpStatus.NOT_FOUND,"Provided organizationId doesn't exists");
        }
        if(!organization.getUserId().equals(userId)){
            return ResponseJsonUtil.getResponse(HttpStatus.UNAUTHORIZED,"Provided organizationId doesn't belong to logged in user");
        }

        List<Workspace> workspaces  = workspaceApiRepository.getWorkspaces(organizationId,index,itemsPerIndex,search,sort,userId);
        if(workspaces.isEmpty()){
            return ResponseJsonUtil.getResponse(HttpStatus.NOT_FOUND,"No workspace found");
        }
        ArrayNode data  = objectMapper.createArrayNode();
        ArrayNode boards  = objectMapper.createArrayNode();
        workspaces.forEach(e->{
            ObjectNode node = objectMapper.createObjectNode();
            node.put("id",e.getId());
            node.put("name",e.getName());
            node.put("description",e.getDescription());
            node.put("active",e.isActive());
            node.put("pinned",e.isPinned());
            node.set("createdOn",objectMapper.valueToTree(e.getCreatedOn()));

            node.put("planBoardCount",0);
            node.set("planBoards",boards);
            data.add(node);
        });
        return ResponseJsonUtil.getResponse(HttpStatus.OK,data);
    }

    public ResponseEntity<ResponseJsonHandler> updateWorkspace(String workspaceId, String userId, RequestJsonHandler requestJsonHandler) {

        Workspace workspace  = workspaceApiRepository.getWorkSpaceById(workspaceId);
        if(workspace==null){
            return ResponseJsonUtil.getResponse(HttpStatus.NOT_FOUND,"Provided workspaceId doesn't exists");
        }
        if(!workspace.getUserId().equals(userId)){
            return ResponseJsonUtil.getResponse(HttpStatus.UNAUTHORIZED,"Provided workspaceId doesn't belong to logged in user");
        }

        String workspaceName  = requestJsonHandler.getStringValue("workspaceName");
        if(!StringUtils.isEmpty(workspaceName)){
            if(!workspaceName.equals(workspace.getName())){
                if(workspaceApiRepository.isWorkspaceExists(workspaceName,userId,workspace.getOrganizationId())){
                    return ResponseJsonUtil.getResponse(HttpStatus.CONFLICT,"Provided workspace name already exists");
                }
                else{
                    workspace.setName(workspaceName.trim());
                }
            }
        }
        String description  = requestJsonHandler.getStringValue("description");
        if(!StringUtils.isEmpty(description)){
            if(!description.equals(workspace.getDescription())){
                workspace.setDescription(description.trim());
            }
        }
        Map<String,Object> data  = requestJsonHandler.getData();
        if(data.containsKey("active")){
            workspace.setActive(requestJsonHandler.getBooleanValue("active"));
        }
        if(data.containsKey("pinned")){
            workspace.setPinned(requestJsonHandler.getBooleanValue("pinned"));
        }


        workspace.setModifiedOn(Instant.now());
        workspaceApiRepository.saveOrUpdateWorkspace(workspace);
        return ResponseJsonUtil.getResponse(HttpStatus.OK,"Workspace successfully updated");
    }
}
