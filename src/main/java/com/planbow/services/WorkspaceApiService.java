package com.planbow.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.planbow.documents.global.Organization;
import com.planbow.documents.planboard.Members;
import com.planbow.documents.planboard.Planboard;
import com.planbow.documents.workspace.Workspace;
import com.planbow.entities.user.UserEntity;
import com.planbow.repository.GlobalApiRepository;
import com.planbow.repository.PlanboardApiRepository;
import com.planbow.repository.PlanbowHibernateRepository;
import com.planbow.repository.WorkspaceApiRepository;
import com.planbow.util.json.handler.request.RequestJsonHandler;
import com.planbow.util.json.handler.response.ResponseJsonHandler;
import com.planbow.util.json.handler.response.util.ResponseJsonUtil;
import com.planbow.utility.PlanbowUtility;
import io.micrometer.common.util.StringUtils;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Log4j2
@Transactional
public class WorkspaceApiService {
    private WorkspaceApiRepository workspaceApiRepository;
    private GlobalApiRepository globalApiRepository;
    private PlanbowHibernateRepository planbowHibernateRepository;

    private PlanboardApiRepository planboardApiRepository;


    @Autowired
    public void setPlanboardApiRepository(PlanboardApiRepository planboardApiRepository) {
        this.planboardApiRepository = planboardApiRepository;
    }

    @Autowired
    public void setPlanbowHibernateRepository(PlanbowHibernateRepository planbowHibernateRepository) {
        this.planbowHibernateRepository = planbowHibernateRepository;
    }

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

        workspaces.forEach(workspace->{
            ArrayNode boards  = objectMapper.createArrayNode();
            ObjectNode node = objectMapper.createObjectNode();
            node.put("id",workspace.getId());
            node.put("name",workspace.getName());
            node.put("description",workspace.getDescription());
            node.put("active",workspace.isActive());
            node.put("pinned",workspace.isPinned());
            node.set("createdOn",objectMapper.valueToTree(workspace.getCreatedOn()));

            List<Planboard> planboards = workspaceApiRepository.getPlanboards(workspace.getId(),workspace.getUserId());
            node.put("planBoardCount",planboards.size());

            Set<String> ids  =planboards.stream().filter(dt-> !CollectionUtils.isEmpty(dt.getMembers())).flatMap(e-> e.getMembers().stream().filter(ft-> !StringUtils.isEmpty(ft.getUserId())).map(Members::getUserId)).collect(Collectors.toSet());
            List<UserEntity> userEntities  = planbowHibernateRepository.getUserEntities(null,new ArrayList<>(ids));
            node.put("planBoardCount",planboards.size());

            planboards.forEach(e->{
                ObjectNode pbNode  = objectMapper.createObjectNode();

                pbNode.put("planboardId",e.getId());
                pbNode.put("name",e.getName());
                pbNode.put("description",e.getDescription());
                pbNode.put("endDate", PlanbowUtility.formatInstantToString(e.getEndDate(),null));
                pbNode.put("createdOn", PlanbowUtility.formatInstantToString(e.getCreatedOn(),null));

                pbNode.put("events", workspaceApiRepository.getEventCounts(e.getId()));
                pbNode.put("actionItems",planboardApiRepository.getActionItemCount(e.getId(),e.getId()));
                pbNode.put("focusAreas",planboardApiRepository.getPlanboardNodesCount(e.getId()));


                //node.put("actionItems",planboardApiRepository.getActionItemCount(e.getPlanboardId(),e.getId()));
                //node.put("subTasks",planboardApiRepository.getTaskCount(e.getPlanboardId(),e.getId()));


                ArrayNode members  =objectMapper.createArrayNode();
                if(!CollectionUtils.isEmpty(e.getMembers())){
                    e.getMembers().forEach(member->{
                        ObjectNode memberNode  = objectMapper.createObjectNode();
                        memberNode.put("userId",member.getUserId());
                        memberNode.put("email",member.getEmailId());
                        memberNode.put("status",member.getStatus());
                        memberNode.put("role",member.getRole());

                        UserEntity userEntity  = PlanbowUtility.getUserEntity(userEntities,StringUtils.isEmpty(member.getUserId())?null : Long.valueOf(member.getUserId()));
                        if(userEntity!=null){
                            memberNode.put("name",userEntity.getName());
                            memberNode.put("profilePic",userEntity.getProfilePic());
                            memberNode.put("gender",userEntity.getGender());
                        }else{
                            memberNode.set("name",objectMapper.valueToTree(null));
                            memberNode.set("profilePic",objectMapper.valueToTree(null));
                            memberNode.set("gender",objectMapper.valueToTree(null));
                        }

                        members.add(memberNode);
                    });
                }
                pbNode.set("members",members);
                boards.add(pbNode);
            });

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

    public ResponseEntity<ResponseJsonHandler> getWorkspace(String workspaceId, String organizationId, String userId) {

        Workspace workspace  = workspaceApiRepository.getWorkSpaceById(workspaceId);
        if(workspace==null){
            return ResponseJsonUtil.getResponse(HttpStatus.NOT_FOUND,"Provided workspaceId doesn't exists");
        }
        if(!workspace.getUserId().equals(userId)){
            return ResponseJsonUtil.getResponse(HttpStatus.UNAUTHORIZED,"Provided workspaceId doesn't belong to logged in user");
        }
        if(!workspace.getOrganizationId().equals(organizationId)){
            return ResponseJsonUtil.getResponse(HttpStatus.UNAUTHORIZED,"Provided workspaceId doesn't belong to given organization");
        }

        ArrayNode boards  = objectMapper.createArrayNode();
        ObjectNode node = objectMapper.createObjectNode();
        node.put("id",workspace.getId());
        node.put("name",workspace.getName());
        node.put("description",workspace.getDescription());
        node.put("active",workspace.isActive());
        node.put("pinned",workspace.isPinned());
        node.set("createdOn",objectMapper.valueToTree(workspace.getCreatedOn()));
        List<Planboard> planboards = workspaceApiRepository.getPlanboards(workspace.getId(),workspace.getUserId());
        Set<String> ids  =planboards.stream().filter(dt-> !CollectionUtils.isEmpty(dt.getMembers())).flatMap(e-> e.getMembers().stream().map(Members::getUserId).filter(id ->!StringUtils.isEmpty(id))).collect(Collectors.toSet());
        List<UserEntity> userEntities  = planbowHibernateRepository.getUserEntities(null,new ArrayList<>(ids));
        node.put("planBoardCount",planboards.size());

        planboards.forEach(e->{
           ObjectNode data  = objectMapper.createObjectNode();

            data.put("planboardId",e.getId());
            data.put("name",e.getName());
            data.put("description",e.getDescription());
            data.put("endDate", PlanbowUtility.formatInstantToString(e.getEndDate(),null));
            data.put("createdOn", PlanbowUtility.formatInstantToString(e.getCreatedOn(),null));

            data.put("events", workspaceApiRepository.getEventCounts(e.getId()));
            data.put("actionItems",0);
            data.put("focusAreas",0);

            ArrayNode members  =objectMapper.createArrayNode();
            if(!CollectionUtils.isEmpty(e.getMembers())){
                e.getMembers().forEach(member->{
                    ObjectNode memberNode  = objectMapper.createObjectNode();
                    memberNode.put("userId",member.getUserId());
                    memberNode.put("email",member.getEmailId());
                    memberNode.put("status",member.getStatus());
                    memberNode.put("role",member.getRole());

                    UserEntity userEntity  = PlanbowUtility.getUserEntity(userEntities,StringUtils.isEmpty(member.getUserId())?null : Long.valueOf(member.getUserId()));
                    if(userEntity!=null){
                        memberNode.put("name",userEntity.getName());
                        memberNode.put("profilePic",userEntity.getProfilePic());
                        memberNode.put("gender",userEntity.getGender());
                    }else{
                        memberNode.set("name",objectMapper.valueToTree(null));
                        memberNode.set("profilePic",objectMapper.valueToTree(null));
                        memberNode.set("gender",objectMapper.valueToTree(null));
                    }

                    members.add(memberNode);
                });
            }
            data.set("members",members);
            boards.add(data);
        });

        node.set("planBoards",boards);
        return ResponseJsonUtil.getResponse(HttpStatus.OK,node);

    }
}
