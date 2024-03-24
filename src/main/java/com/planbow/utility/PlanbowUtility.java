package com.planbow.utility;


import com.planbow.documents.open.ai.PromptValidation;
import com.planbow.documents.planboard.*;
import com.planbow.documents.prompts.PromptResults;
import com.planbow.entities.user.UserEntity;
import com.planbow.repository.PlanboardApiRepository;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.planbow.documents.planboard.ActionItems.STATUS_IN_TODO;
import static com.planbow.documents.planboard.Tasks.STATUS_COMPLETED;
import static com.planbow.documents.planboard.Tasks.STATUS_IN_PROGRESS;

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

    public static Instant formatStringToInstant(String date,String pattern){
        if(StringUtils.isEmpty(pattern))
            pattern= "dd-MM-yyyy";
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern(pattern)
                .toFormatter(Locale.ENGLISH);
        LocalDate localDate = LocalDate.parse(date, formatter);
        return localDate.atStartOfDay(ZoneId.of("UTC")).toInstant();
    }

    public static Instant formatStringTimeToInstant(String time,String pattern){
        if(StringUtils.isEmpty(pattern))
            pattern="hh:mm a";

        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern(pattern)
                .toFormatter(Locale.ENGLISH);
        LocalTime localTime = LocalTime.parse(time, formatter);
        ZonedDateTime zonedDateTime = ZonedDateTime.of(LocalDate.now(), localTime, ZoneId.systemDefault());
        return zonedDateTime.toInstant();
    }




    public static String formatInstantToString(Instant instant,String pattern){
        if(StringUtils.isEmpty(pattern))
            pattern="dd-MM-yyyy";
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
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

    public static boolean isValidRole(String role) {
        return !StringUtils.isEmpty(role) &&  (role.equals("Creator") || role.equals("Contributor") || role.equals("Viewer"));
    }

    public static boolean isEmailOrUserIdProvided(String userId,String emailId) {
        return !StringUtils.isEmpty(userId) || (!StringUtils.isEmpty(emailId) && isValidEmail(emailId));
    }

    public static boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static Instant convertStringToInstantUTC(String dateTimeString) {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern("dd-MM-yyyy hh:mm a")
                .toFormatter(Locale.ENGLISH);
        LocalDateTime localDateTime = LocalDateTime.parse(dateTimeString, formatter);
        ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, ZoneId.systemDefault());
        return zonedDateTime.withZoneSameInstant(ZoneOffset.UTC).toInstant();
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

    public static String extractNameFromEmail(String emailAddress) {
        Pattern pattern = Pattern.compile("^(.+)@.*$");
        Matcher matcher = pattern.matcher(emailAddress);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return null;
    }
    public static String formatDate(Date date){
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy");
        return  outputFormat.format(date);
    }


    public static ActionItems handleActionItemStatus(List<Tasks> tasks,ActionItems actionItems){
        if(tasks.isEmpty()){
            actionItems.setStatus(STATUS_IN_TODO);
        }else{
            if(tasks.stream().allMatch(f-> f.getStatus().equals(ActionItems.STATUS_COMPLETED))){
                actionItems.setStatus(STATUS_COMPLETED);
            }else {
                if (tasks.stream().anyMatch(f -> f.getStatus().equals(ActionItems.STATUS_IN_PROGRESS))) {
                    actionItems.setStatus(STATUS_IN_PROGRESS);
                } else {
                    if (tasks.stream().allMatch(f -> f.getStatus().equals(STATUS_IN_TODO))) {
                        actionItems.setStatus(STATUS_IN_TODO);
                    } else {
                        actionItems.setStatus(STATUS_IN_PROGRESS);
                    }
                }
            }
        }
        return actionItems;
    }

}
