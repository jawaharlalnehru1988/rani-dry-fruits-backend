package com.asknehru.myclientsapi.ranicashew;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fruit-combos")
public class FruitComboController {

    private final FruitComboService fruitComboService;

    public FruitComboController(FruitComboService fruitComboService) {
        this.fruitComboService = fruitComboService;
    }

    @GetMapping
    public ResponseEntity<List<FruitComboResponse>> getAll(
        @RequestParam(name = "activeOnly", required = false, defaultValue = "false") Boolean activeOnly
    ) {
        return ResponseEntity.ok(fruitComboService.getAll(activeOnly));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FruitComboResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(fruitComboService.getById(id));
    }

    @PostMapping
    public ResponseEntity<FruitComboResponse> create(@RequestBody FruitComboWriteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fruitComboService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FruitComboResponse> update(
        @PathVariable Long id,
        @RequestBody FruitComboWriteRequest request
    ) {
        return ResponseEntity.ok(fruitComboService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        fruitComboService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
