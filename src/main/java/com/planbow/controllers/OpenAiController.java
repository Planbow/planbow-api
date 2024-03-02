package com.planbow.controllers;


import com.planbow.services.OpenAiService;
import com.planbow.util.json.handler.request.RequestJsonHandler;
import com.planbow.util.json.handler.response.ResponseJsonHandler;
import com.planbow.util.json.handler.response.util.ResponseJsonUtil;
import io.micrometer.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("open.ai")
public class OpenAiController {

    private OpenAiService openAiService;

    @Autowired
    public void setOpenAiService(OpenAiService openAiService) {
        this.openAiService = openAiService;
    }

    @PostMapping("/prompt")
    public ResponseEntity<ResponseJsonHandler> prompt(@RequestBody RequestJsonHandler requestJsonHandler){
        String prompt = requestJsonHandler.getStringValue("prompt");
        if(StringUtils.isEmpty(prompt))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide prompt message");
        return openAiService.prompt(prompt);
    }

    @PostMapping("/validate-prompt")
    public ResponseEntity<ResponseJsonHandler> validatePrompt(@RequestBody RequestJsonHandler requestJsonHandler){
        String domain = requestJsonHandler.getStringValue("domain");
        if(StringUtils.isEmpty(domain))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide domain");

        String subdomain = requestJsonHandler.getStringValue("subdomain");
        if(StringUtils.isEmpty(subdomain))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide subdomain");

        String scope = requestJsonHandler.getStringValue("scope");
        String geography = requestJsonHandler.getStringValue("geography");
        return openAiService.validatePrompt(domain.trim(),subdomain.trim(),scope,geography);
    }

}
