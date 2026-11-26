package com.medicare.medicine.service;

import com.medicare.medicine.dto.MedicineCategoryDto;

import java.util.List;

public interface MedicineCategoryService {
    List<MedicineCategoryDto> getAllCategories();
    MedicineCategoryDto createCategory(MedicineCategoryDto dto);
    MedicineCategoryDto updateCategory(Long id, MedicineCategoryDto dto);
    void deleteCategory(Long id);
}
