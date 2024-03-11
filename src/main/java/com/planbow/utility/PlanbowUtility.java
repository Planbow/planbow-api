package com.planbow.utility;


import com.planbow.documents.open.ai.PromptValidation;
import com.planbow.documents.planboard.Members;
import com.planbow.documents.planboard.TemporaryPlanboard;
import com.planbow.documents.prompts.PromptResults;
import com.planbow.entities.user.UserEntity;
import com.planbow.repository.PlanboardApiRepository;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j2
public class PlanbowUtility {


    public static final String DIRECTORY_ROOT="planbow";
    public static final String DIRECTORY_BOARDS="boards";

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

    public static Instant formatStringToInstant(String date){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate localDate = LocalDate.parse(date, formatter);
        return localDate.atStartOfDay(ZoneId.of("UTC")).toInstant();
    }

    public static String formatInstantToString(Instant instant){
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return localDateTime.format(formatter);
    }

    public static UserEntity getUserEntity(List<UserEntity> userEntityList,Long id){
        return userEntityList.stream().filter(f-> Objects.equals(f.getId(), id)).findFirst().orElse(null);
    }

    public static boolean validateMemberAndRoles(List<Members> members){
        return members.stream()
                .allMatch(
                        member ->
                                isValidRole(member.getRole()) &&
                               isEmailOrUserIdProvided(member.getUserId(),member.getEmailId())
                );

    }

    private static boolean isValidRole(String role) {
        return !StringUtils.isEmpty(role) &&  (role.equals("Creator") || role.equals("Contributor") || role.equals("Viewer"));
    }

    private static boolean isEmailOrUserIdProvided(String userId,String emailId) {
        return !StringUtils.isEmpty(userId) || (!StringUtils.isEmpty(emailId) && isValidEmail(emailId));
    }

    public static boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " bytes";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }
}
