package com.planbow.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.planbow.documents.core.Domain;
import com.planbow.documents.core.SubDomain;
import com.planbow.documents.open.ai.NodeData;
import com.planbow.documents.open.ai.PromptValidation;
import com.planbow.documents.planboard.TemporaryPlanboard;
import com.planbow.documents.prompts.PromptResults;
import com.planbow.repository.AdminApiRepository;
import com.planbow.repository.PlanboardApiRepository;
import com.planbow.util.json.handler.response.ResponseJsonHandler;
import com.planbow.util.json.handler.response.util.ResponseJsonUtil;
import com.planbow.utility.PlanbowUtility;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.ai.parser.BeanOutputParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static com.planbow.utility.PlanbowUtility.preparePromptResult;
import static com.planbow.utility.PlanbowUtility.prepareTemporaryPlanboard;

@Service
@Log4j2
public class PlanboardApiService {

    private PlanboardApiRepository planboardApiRepository;
    private AdminApiRepository adminApiRepository;
    private OpenAiChatClient chatClient;
    private ObjectMapper objectMapper;


    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Autowired
    public void setAdminApiRepository(AdminApiRepository adminApiRepository) {
        this.adminApiRepository = adminApiRepository;
    }

    @Autowired
    public void setChatClient(OpenAiChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Autowired
    public void setPlanboardApiRepository(PlanboardApiRepository planboardApiRepository) {
        this.planboardApiRepository = planboardApiRepository;
    }


    public ResponseEntity<ResponseJsonHandler> validatePrompt(String domainId, String subdomainId, String scope, String geography,String userId){
        Domain domain = adminApiRepository.getDomainById(domainId);
        if(domain==null)
            return ResponseJsonUtil.getResponse(HttpStatus.NOT_FOUND,"Provided domainId does not exists");

        SubDomain subDomain = adminApiRepository.getSubdomainById(subdomainId);
        if(subDomain==null)
            return ResponseJsonUtil.getResponse(HttpStatus.NOT_FOUND,"Provided subdomainId does not exists");

        ObjectNode node  = objectMapper.createObjectNode();
        PromptResults promptResults  = planboardApiRepository.getPromptResult(domainId,subdomainId,scope,geography);
        if(promptResults!=null){
            if(promptResults.getPromptValidation()!=null){
                if(promptResults.getPromptValidation().getStatus().equalsIgnoreCase("negative")){
                    PromptValidation promptValidation=openAiPromptValidation(domain.getName(), subDomain.getName(),scope,geography);
                    promptResults = preparePromptResult(planboardApiRepository,domain.getId(),subDomain.getId(),scope,geography,userId,promptResults,promptValidation);
                    TemporaryPlanboard temporaryPlanboard = prepareTemporaryPlanboard(planboardApiRepository,promptResults.getId(),userId);
                    node.put("status",promptValidation.getStatus());
                    node.put("reason",promptValidation.getReason());
                    node.put("planboardId",temporaryPlanboard.getId());
                    if(promptValidation.getStatus().equalsIgnoreCase("positive")){
                        PromptResults finalPromptResults = promptResults;
                        new Thread(()->openAiStrategicNodes(domain,subDomain,scope,geography,userId, finalPromptResults) ).start();
                    }
                }
                else {
                    if(CollectionUtils.isEmpty(promptResults.getStrategicNodes())){
                        TemporaryPlanboard temporaryPlanboard = prepareTemporaryPlanboard(planboardApiRepository,promptResults.getId(),userId);
                        node.put("status",promptResults.getPromptValidation().getStatus());
                        node.put("reason",promptResults.getPromptValidation().getReason());
                        node.put("planboardId",temporaryPlanboard.getId());
                        PromptResults finalPromptResults = promptResults;
                        new Thread(()->openAiStrategicNodes(domain,subDomain,scope,geography,userId, finalPromptResults) ).start();
                    }else{
                        TemporaryPlanboard temporaryPlanboard = prepareTemporaryPlanboard(planboardApiRepository,promptResults.getId(),userId);
                        node.put("status",promptResults.getPromptValidation().getStatus());
                        node.put("reason",promptResults.getPromptValidation().getReason());
                        node.put("planboardId",temporaryPlanboard.getId());
                    }
                }
            }
            else{
                PromptValidation promptValidation=openAiPromptValidation(domain.getName(), subDomain.getName(),scope,geography);
                promptResults = preparePromptResult(planboardApiRepository,domain.getId(),subDomain.getId(),scope,geography,userId,promptResults,promptValidation);
                TemporaryPlanboard temporaryPlanboard = prepareTemporaryPlanboard(planboardApiRepository,promptResults.getId(),userId);
                node.put("status",promptValidation.getStatus());
                node.put("reason",promptValidation.getReason());
                node.put("planboardId",temporaryPlanboard.getId());

                if(promptValidation.getStatus().equalsIgnoreCase("positive")){
                    PromptResults finalPromptResults = promptResults;
                    new Thread(()->openAiStrategicNodes(domain,subDomain,scope,geography,userId, finalPromptResults) ).start();
                }
            }
        }
        else{
            PromptValidation promptValidation=openAiPromptValidation(domain.getName(), subDomain.getName(),scope,geography);
            promptResults = preparePromptResult(planboardApiRepository,domain.getId(),subDomain.getId(),scope,geography,userId,null,promptValidation);
            TemporaryPlanboard temporaryPlanboard = prepareTemporaryPlanboard(planboardApiRepository,promptResults.getId(),userId);
            node.put("status",promptValidation.getStatus());
            node.put("reason",promptValidation.getReason());
            node.put("planboardId",temporaryPlanboard.getId());
            if(promptValidation.getStatus().equalsIgnoreCase("positive")){
                PromptResults finalPromptResults = promptResults;
                new Thread(()->openAiStrategicNodes(domain,subDomain,scope,geography,userId, finalPromptResults) ).start();
            }
        }
        return ResponseJsonUtil.getResponse(HttpStatus.OK,node);
    }

    private void openAiStrategicNodes(Domain domain,SubDomain subdomain,String scope,String geography,String userId,PromptResults promptResults) {
        log.info("Executing openAiStrategicNodes() method");
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
        map.put("domain",domain.getName());
        map.put("subdomain",subdomain.getName());
        map.put("geography",geography);
        map.put("scope",scope);
        map.put("format",outputParser.getFormat());
        PromptTemplate promptTemplate = new PromptTemplate(query,map);
        Prompt prompt = promptTemplate.create();
        Generation generation = chatClient.call(prompt).getResult();
        NodeData nodeData;
        try{
            nodeData = outputParser.parse(generation.getOutput().getContent());
           promptResults.setStrategicNodes(nodeData.getNodeResponses());
        }catch (Exception e){
            log.error("Exception occurred in generateNodes() method : {}",e.getMessage());
        }
        planboardApiRepository.saveOrUpdatePromptResults(promptResults);

    }


    private  PromptValidation openAiPromptValidation(String domain, String subdomain, String scope, String geography){
        log.info("Executing openAiPromptValidation() method");
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
        return promptValidation;
    }

}
