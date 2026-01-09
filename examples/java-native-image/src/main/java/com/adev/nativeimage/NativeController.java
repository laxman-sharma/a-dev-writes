package com.adev.nativeimage;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class NativeController {

    private final PersonRepository repository;

    public NativeController(PersonRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello from Native Executable!";
    }

    @GetMapping("/people")
    public List<Person> people() {
        if (repository.count() == 0) {
            repository.save(new Person("Laxman"));
            repository.save(new Person("Developer"));
        }
        return repository.findAll();
    }
}
