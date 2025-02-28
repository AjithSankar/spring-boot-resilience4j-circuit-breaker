package dev.ak.springbootresilience4j.circuitbreaker.model;


public record Person(String id, String firstName, String lastName, String email, String phoneNumber, Address address) {

}