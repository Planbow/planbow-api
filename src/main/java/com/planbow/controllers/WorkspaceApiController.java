package com.planbow.controllers;


import com.planbow.services.WorkspaceApiService;
import com.planbow.util.json.handler.request.RequestJsonHandler;
import com.planbow.util.json.handler.response.ResponseJsonHandler;
import com.planbow.util.json.handler.response.util.ResponseJsonUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.http.util.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/workspace")
@RestController
@Log4j2
public class WorkspaceApiController {

    private WorkspaceApiService workspaceApiService;

    @Autowired
    public void setWorkspaceApiService(WorkspaceApiService workspaceApiService) {
        this.workspaceApiService = workspaceApiService;
    }

    @PostMapping("create-workspace")
    public ResponseEntity<ResponseJsonHandler> createWorkspace(@RequestBody RequestJsonHandler requestJsonHandler){
        String organizationId  = requestJsonHandler.getStringValue("organizationId");
        if(TextUtils.isEmpty(organizationId))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide organizationId");

        String workspaceName  = requestJsonHandler.getStringValue("workspaceName");
        if(TextUtils.isEmpty(workspaceName))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide workspaceName");
        String userId =requestJsonHandler.getStringValue("userId");
        return workspaceApiService.createWorkspace(workspaceName.trim(),organizationId.trim(),userId);
    }


    @PostMapping("get-workspaces")
    public ResponseEntity<ResponseJsonHandler> getWorkspaces(@RequestBody RequestJsonHandler requestJsonHandler){
        String userId =requestJsonHandler.getStringValue("userId");
        String organizationId  = requestJsonHandler.getStringValue("organizationId");
        if(TextUtils.isEmpty(organizationId))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide organizationId");
        Integer index  = requestJsonHandler.getIntegerValue("index");
        if(index==null)
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST, "Please provide index");

        Integer itemsPerIndex  = requestJsonHandler.getIntegerValue("itemsPerIndex");
        if(itemsPerIndex==null)
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST, "Please provide itemsPerIndex");

        String search  = requestJsonHandler.getStringValue("search");
        String sort  = requestJsonHandler.getStringValue("sort");
        return workspaceApiService.getWorkspaces(organizationId.trim(),index,itemsPerIndex,search,sort,userId);
    }

    @PostMapping("update-workspace")
    public ResponseEntity<ResponseJsonHandler> updateWorkspace(@RequestBody RequestJsonHandler requestJsonHandler){
        String userId =requestJsonHandler.getStringValue("userId");
        String workspaceId  = requestJsonHandler.getStringValue("workspaceId");
        if(TextUtils.isEmpty(workspaceId))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide workspaceId");
        return workspaceApiService.updateWorkspace(workspaceId.trim(),userId,requestJsonHandler);
    }

}
