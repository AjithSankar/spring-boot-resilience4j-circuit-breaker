package dev.ak.springbootresilience4j.circuitbreaker.controller;

import dev.ak.springbootresilience4j.circuitbreaker.model.Person;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/person")
@Slf4j
public class PersonController {

    private String GET_PERSON_BY_ID_API = "http://localhost:8095/api/persons/";

    private RestTemplate restTemplate;

    public PersonController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/{id}")
    @CircuitBreaker(name = "getPersonById", fallbackMethod = "getPersonByIdFallback")
    public String getPersonById(@PathVariable("id") Integer id) {
        Person person = restTemplate.getForObject(GET_PERSON_BY_ID_API + id, Person.class);
        return person.firstName() + " " + person.lastName();
    }

    public String getPersonByIdFallback(Integer id, Throwable throwable) {
        log.error("Fallback method called for id: {} and exception: {}", id, throwable);
        return "Fallback: " + id;
    }


}
