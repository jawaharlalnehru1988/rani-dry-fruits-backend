package com.asknehru.fruitsapi.controller;

import com.asknehru.fruitsapi.dto.FruitGalleryResponse;
import com.asknehru.fruitsapi.dto.FruitGalleryWriteRequest;
import com.asknehru.fruitsapi.service.FruitGalleryService;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fruits-gallery")
public class FruitGalleryController {

    private final FruitGalleryService fruitGalleryService;

    public FruitGalleryController(FruitGalleryService fruitGalleryService) {
        this.fruitGalleryService = fruitGalleryService;
    }

    @GetMapping
    public ResponseEntity<List<FruitGalleryResponse>> getAll() {
        return ResponseEntity.ok(fruitGalleryService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FruitGalleryResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(fruitGalleryService.getById(id));
    }

    @PostMapping
    public ResponseEntity<FruitGalleryResponse> create(@RequestBody FruitGalleryWriteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fruitGalleryService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FruitGalleryResponse> update(@PathVariable Long id, @RequestBody FruitGalleryWriteRequest request) {
        return ResponseEntity.ok(fruitGalleryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        fruitGalleryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
