package fr.eni.bookhub.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class BookHubController {
    @GetMapping
    public String welcomeAPI() {
        return "Bienvenue sur l'API du projet BookHub !";
    }
}
