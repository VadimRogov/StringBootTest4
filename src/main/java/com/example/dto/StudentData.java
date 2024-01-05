package com.example.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class StudentData implements Serializable {

    @JsonProperty("lastName")
    @ApiModelProperty("Имя студента")
    private String lastName;

    @JsonProperty("firstName")
    @ApiModelProperty("Фамилия студента")
    private String firstName;

    @JsonProperty("grade")
    @ApiModelProperty("Оценка студента")
    private String grade;
}
