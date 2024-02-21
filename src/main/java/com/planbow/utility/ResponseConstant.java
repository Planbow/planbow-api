package com.planbow.utility;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseConstant {

    SUCCESS("success"),
    ERROR_IN_PROCESSING_REQUEST("Error in processing request, please try after some time"),

    INVALID_PASSWORD("Invalid credential is provided, Please provide valid credentials"),


    EMAIL_ERROR("Please provide email address"),
    EMAIL_ID_ALREAY_EXISTS("Email address already exists"),
    INVALID_EMAIL("Invalid email pattern"),

    NAME_NOT_GIVEN("Please provide name"),
    CONTACT_NO_NOT_GIVEN("Please provide contact number"),
    CONTACT_NO_ALREADY_EXISTS("Provided contact number already exists"),
    CONTACT_NO_NOT_FOUND("Contact number you are looking for does not exists with us"),
    PASSWORD_NOT_GIVEN("Please provide login password"),

    INDEX_NO_NOT_GIVEN("Please provide index number"),
    ITEM_PER_INDEX_NOT_GIVEN("Please provide number of items per index"),


    ;
    private String status;
    public String getStatus(){
        return this.status;
    }

}
