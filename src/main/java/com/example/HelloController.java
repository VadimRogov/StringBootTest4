package com.example;

import com.example.dto.StudentData;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hello")
public class HelloController {
    //Метод для передачи данных
    @GetMapping("/student")
    @ApiOperation(value = "Приветствие студента")
    public String helloStudent() {
        return "Hello, student!";
    }
    // Метод который приветствует по имени (позволяет динамически менять name)
    @RequestMapping(value = "/greetings/{name}")
    @ApiOperation(value = "Приветствие студента по имени")
    public String greetingsByName(@PathVariable String name) {
        return "Hello, " + name;
    }

    @RequestMapping(value = "/student/submit")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Обратная связь по оценке студента")
    // Аннотация @RequestBody говорит о том, что данный объект представлен в запросе как форм-дата
    // и Spring MVC будет пытаться сконвертировать данные из форм-дата из запроса в данный класс
    public String giveMeFeedbackAboutGrade(@RequestBody StudentData studentData) {
        return "You are great with your grade " + studentData.getFirstName()
                + studentData.getLastName() + studentData.getGrade();
    }
}