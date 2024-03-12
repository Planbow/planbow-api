package com.planbow.documents.open.ai;


import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class PromptValidation {
    private String status;
    private String reason;
}
