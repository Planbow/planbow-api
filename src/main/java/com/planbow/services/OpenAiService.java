package com.planbow.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.planbow.documents.open.ai.NodeData;
import com.planbow.documents.open.ai.PromptValidation;
import com.planbow.util.json.handler.response.ResponseJsonHandler;
import com.planbow.util.json.handler.response.util.ResponseJsonUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.Generation;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.ai.parser.BeanOutputParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
@Log4j2
public class OpenAiService {

    private OpenAiChatClient chatClient;
    private ObjectMapper objectMapper;

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Autowired
    public void setChatClient(OpenAiChatClient chatClient) {
        this.chatClient = chatClient;
    }


    public ResponseEntity<ResponseJsonHandler> prompt(String promptMsg){
        BeanOutputParser<PromptValidation> outputParser = new BeanOutputParser<>(PromptValidation.class);


        String query =promptMsg+
                """
                {format}
                """;


        PromptTemplate promptTemplate = new PromptTemplate(query, Map.of( "format", outputParser.getFormat() ));
        Prompt prompt = promptTemplate.create();
        Generation generation = chatClient.call(prompt).getResult();
        PromptValidation promptValidation;
        try{
            promptValidation = outputParser.parse(generation.getOutput().getContent());
        }catch (Exception e){
            log.error("Exception occurred in validatePrompt() method : {}",e.getMessage());
            promptValidation = new PromptValidation();
            promptValidation.setStatus("negative");
            promptValidation.setReason("Unable to process prompt "+e.getMessage());
        }

        return ResponseJsonUtil.getResponse(HttpStatus.OK,promptValidation);
    }
    public ResponseEntity<ResponseJsonHandler> validatePrompt(String domain,String subdomain,String scope,String geography){
        BeanOutputParser<PromptValidation> outputParser = new BeanOutputParser<>(PromptValidation.class);
        if(scope==null)
            scope=" ";

        if(geography==null)
            geography=" ";
        String query =
                """
                Validate this prompt from the business point of view and respond with positive or negative if the prompt makes sense. If this comes out to be negative then provide one line reason as well -\s
                Provide Business strategy steps for {domain} business focusing on {subdomain} focusing in {geography} market. Key departments to focus on {scope}.
                {format}
                """;

        Map<String,Object> map  = new HashMap<>();
        map.put("domain",domain);
        map.put("subdomain",subdomain);
        map.put("geography",geography);
        map.put("scope",scope);
        map.put("format",outputParser.getFormat());
        PromptTemplate promptTemplate = new PromptTemplate(query,map);
        Prompt prompt = promptTemplate.create();
        Generation generation = chatClient.call(prompt).getResult();
        PromptValidation promptValidation;
        try{
             promptValidation = outputParser.parse(generation.getOutput().getContent());
        }catch (Exception e){
            log.error("Exception occurred in validatePrompt() method : {}",e.getMessage());
            promptValidation = new PromptValidation();
            promptValidation.setStatus("negative");
            promptValidation.setReason("Unable to process prompt "+e.getMessage());
        }
        System.out.println(promptValidation);
        return ResponseJsonUtil.getResponse(HttpStatus.OK,promptValidation);
    }
    public ResponseEntity<ResponseJsonHandler> generateNodes(String domain,String subdomain,String scope,String geography){
        BeanOutputParser<NodeData> outputParser = new BeanOutputParser<>(NodeData.class);
        if(scope==null)
            scope=" ";

        if(geography==null)
            geography=" ";
        String query =
                """
                Provide Only 3 Business strategy steps for {domain} business focusing on {subdomain} focusing in {geography} market. Key departments to focus on {scope}.format pointers in a flat sequential structure.
                {format}
                """;

        Map<String,Object> map  = new HashMap<>();
        map.put("domain",domain);
        map.put("subdomain",subdomain);
        map.put("geography",geography);
        map.put("scope",scope);
        map.put("format",outputParser.getFormat());
        PromptTemplate promptTemplate = new PromptTemplate(query,map);
        Prompt prompt = promptTemplate.create();
        Generation generation = chatClient.call(prompt).getResult();
        NodeData nodeData;
        ObjectNode node  = objectMapper.createObjectNode();
        try{
            nodeData = outputParser.parse(generation.getOutput().getContent());
            node.put("status","positive");
            node.set("reason",objectMapper.valueToTree(null));
            node.set("results",objectMapper.valueToTree(nodeData.getNodeResponses()));
        }catch (Exception e){
            log.error("Exception occurred in generateNodes() method : {}",e.getMessage());
            node.put("status","negative");
            node.put("reason","Unable to process prompt "+e.getMessage());
            node.set("results",objectMapper.createArrayNode());
        }
        return ResponseJsonUtil.getResponse(HttpStatus.OK,node);
    }
}
