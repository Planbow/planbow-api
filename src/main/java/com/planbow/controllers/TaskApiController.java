package com.planbow.controllers;


import com.planbow.services.TaskApiService;
import com.planbow.util.json.handler.request.RequestJsonHandler;
import com.planbow.util.json.handler.response.ResponseJsonHandler;
import com.planbow.util.json.handler.response.util.ResponseJsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/tasks")
@RestController
public class TaskApiController {

    private TaskApiService taskApiService;

    @Autowired
    public void setTaskApiService(TaskApiService taskApiService) {
        this.taskApiService = taskApiService;
    }

    @PostMapping("/add-task")
    public ResponseEntity<ResponseJsonHandler> addTask(@RequestBody RequestJsonHandler requestJsonHandler){
        String userId  = requestJsonHandler.getStringValue("userId");
        String planboardId  = requestJsonHandler.getStringValue("planboardId");
        if(StringUtils.isEmpty(planboardId))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide planboardId");
        String nodeId  = requestJsonHandler.getStringValue("nodeId");
        if(StringUtils.isEmpty(nodeId))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide nodeId");
        String actionItemId  = requestJsonHandler.getStringValue("actionItemId");
        if(StringUtils.isEmpty(actionItemId))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide actionItemId");
        String title  = requestJsonHandler.getStringValue("title");
        if(StringUtils.isEmpty(title))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide title");
        return  taskApiService.addTask(userId.trim(),planboardId.trim(),nodeId.trim(),actionItemId.trim(),title.trim(),requestJsonHandler);
    }

    @PostMapping("/get-tasks")
    public ResponseEntity<ResponseJsonHandler> getTasks(@RequestBody RequestJsonHandler requestJsonHandler){
        String userId  = requestJsonHandler.getStringValue("userId");
        String actionItemId  = requestJsonHandler.getStringValue("actionItemId");
        if(StringUtils.isEmpty(actionItemId))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide actionItemId");
        return  taskApiService.getTasks(userId.trim(),actionItemId.trim());
    }


    @PostMapping("/update-task")
    public ResponseEntity<ResponseJsonHandler> updateTask(@RequestBody RequestJsonHandler requestJsonHandler){
        String userId  = requestJsonHandler.getStringValue("userId");
        String taskId  = requestJsonHandler.getStringValue("taskId");
        if(StringUtils.isEmpty(taskId))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide taskId");
        return  taskApiService.updateTask(userId.trim(),taskId.trim(),requestJsonHandler);
    }

    @PostMapping("/delete-task")
    public ResponseEntity<ResponseJsonHandler> deleteTask(@RequestBody RequestJsonHandler requestJsonHandler){
        String userId  = requestJsonHandler.getStringValue("userId");
        String taskId  = requestJsonHandler.getStringValue("taskId");
        if(StringUtils.isEmpty(taskId))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide taskId");
        return taskApiService.deleteTask(userId.trim(),taskId.trim());
    }

}
