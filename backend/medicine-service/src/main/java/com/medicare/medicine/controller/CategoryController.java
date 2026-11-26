package com.medicare.medicine.controller;

import com.medicare.common.dto.ApiResponse;
import com.medicare.medicine.dto.MedicineCategoryDto;
import com.medicare.medicine.service.MedicineCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Medicine Categories", description = "Categories management for pharmaceutical classification")
public class CategoryController {

    private final MedicineCategoryService categoryService;

    @GetMapping
    @Operation(summary = "List all medicine categories")
    public ResponseEntity<ApiResponse<List<MedicineCategoryDto>>> getAllCategories() {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getAllCategories()));
    }

    @PostMapping
    @Operation(summary = "Create a new medicine category")
    public ResponseEntity<ApiResponse<MedicineCategoryDto>> createCategory(@Valid @RequestBody MedicineCategoryDto dto) {
        return new ResponseEntity<>(ApiResponse.success(categoryService.createCategory(dto)), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing medicine category")
    public ResponseEntity<ApiResponse<MedicineCategoryDto>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody MedicineCategoryDto dto) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.updateCategory(id, dto)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a medicine category")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Category deleted successfully"));
    }
}
