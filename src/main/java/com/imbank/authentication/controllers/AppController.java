package com.imbank.authentication.controllers;

import com.imbank.authentication.dtos.AllowedAppDto;
import com.imbank.authentication.entities.AllowedApp;
import com.imbank.authentication.services.AppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/apps")
public class AppController {

    @Autowired
    private AppService appService;

    @PostMapping("")
    public ResponseEntity<AllowedApp> createApp(@RequestBody @Valid AllowedAppDto allowedAppDto) {
        return ResponseEntity.ok().body(appService.createApp(allowedAppDto));
    }

    @PutMapping("{id}")
    public ResponseEntity<AllowedApp> updateApp(@PathVariable Long id, @RequestBody @Valid AllowedAppDto allowedAppDto) {
        return ResponseEntity.ok().body(appService.updateApp(id, allowedAppDto));
    }

    @GetMapping("")
    public ResponseEntity<List<AllowedApp>> getAllApps() {
        return ResponseEntity.ok().body(appService.getApps());
    }

    @GetMapping("{id}")
    public ResponseEntity<AllowedApp> getAppById(@PathVariable Long id) {
        return ResponseEntity.ok().body(appService.getApp(id));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<String> deleteApp(@PathVariable Long id) {
        appService.deleteApp(id);
        return ResponseEntity.ok().body("Deleted");
    }
}
