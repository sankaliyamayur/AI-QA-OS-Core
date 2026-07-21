package com.aiqaos.dashboard.controller;

import com.aiqaos.core.entity.ModuleEntity;
import com.aiqaos.core.repository.ModuleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard/modules")
public class ModuleController {

    private final ModuleRepository moduleRepository;

    public ModuleController(ModuleRepository moduleRepository) {
        this.moduleRepository = moduleRepository;
    }

    @GetMapping
    public ResponseEntity<List<ModuleEntity>> getAllModules() {
        return ResponseEntity.ok(moduleRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ModuleEntity> getModuleById(@PathVariable("id") String id) {
        return moduleRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
