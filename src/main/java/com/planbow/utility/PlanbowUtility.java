package com.planbow.utility;


import com.planbow.documents.open.ai.PromptValidation;
import com.planbow.documents.planboard.TemporaryPlanboard;
import com.planbow.documents.prompts.PromptResults;
import com.planbow.repository.PlanboardApiRepository;
import lombok.extern.log4j.Log4j2;
import java.time.Instant;

@Log4j2
public class PlanbowUtility {


    public static PromptResults preparePromptResult(PlanboardApiRepository planboardApiRepository,String domainId, String subdomainId, String scope , String geography, String userId,PromptResults promptResults, PromptValidation promptValidation){
        if(promptResults==null)
           promptResults  = new PromptResults();

        promptResults.setDomainId(domainId);
        promptResults.setSubdomainId(subdomainId);
        promptResults.setScope(scope);
        promptResults.setGeography(geography);
        promptResults.setUserId(userId);

        promptResults.setPromptValidation(promptValidation);
        promptResults.setActive(true);
        promptResults.setCreatedOn(Instant.now());
        promptResults.setModifiedOn(Instant.now());
        promptResults = planboardApiRepository.saveOrUpdatePromptResults(promptResults);
        return promptResults;
    }

    public static TemporaryPlanboard prepareTemporaryPlanboard(PlanboardApiRepository planboardApiRepository,String promptId, String userId){
        TemporaryPlanboard temporaryPlanboard  = new TemporaryPlanboard();
        temporaryPlanboard.setPromptId(promptId);
        temporaryPlanboard.setUserId(userId);
        temporaryPlanboard.setCreatedOn(Instant.now());
        temporaryPlanboard.setModifiedOn(Instant.now());
        temporaryPlanboard.setActive(true);
        temporaryPlanboard = planboardApiRepository.saveOrUpdateTemporaryPlanboard(temporaryPlanboard);
        return temporaryPlanboard;
    }
}
