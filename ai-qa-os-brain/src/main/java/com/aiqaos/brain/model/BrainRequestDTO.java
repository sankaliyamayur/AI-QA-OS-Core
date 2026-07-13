package com.aiqaos.brain.model;

import com.aiqaos.core.dto.BaseDTO;

public class BrainRequestDTO implements BaseDTO {
    private String userInput;

    public String getUserInput() { return userInput; }
    public void setUserInput(String userInput) { this.userInput = userInput; }
}