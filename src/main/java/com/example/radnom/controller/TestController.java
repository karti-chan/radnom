package com.example.radnom.controller;  // ← NA PEWNO główny package!

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    
    public TestController() {
        System.out.println("TESTCONTROLLER utworzony!");
    }
    
    @GetMapping("/api/test/simple")
    public String simpleTest() {
        System.out.println("SIMPLE TEST wywołany!");
        return "DZIAŁA! Spring widzi kontrolery!";
    }
}
//możliwe że pójdzie do wywałki xD