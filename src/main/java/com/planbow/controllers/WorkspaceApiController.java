package com.planbow.controllers;


import com.planbow.services.WorkspaceApiService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
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
}
