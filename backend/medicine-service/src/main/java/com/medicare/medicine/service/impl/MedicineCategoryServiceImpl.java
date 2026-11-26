package com.medicare.medicine.service.impl;

import com.medicare.common.exception.BadRequestException;
import com.medicare.common.exception.ResourceNotFoundException;
import com.medicare.medicine.dto.MedicineCategoryDto;
import com.medicare.medicine.entity.MedicineCategory;
import com.medicare.medicine.repository.MedicineCategoryRepository;
import com.medicare.medicine.service.MedicineCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicineCategoryServiceImpl implements MedicineCategoryService {

    private final MedicineCategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MedicineCategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MedicineCategoryDto createCategory(MedicineCategoryDto dto) {
        if (categoryRepository.existsByName(dto.getName())) {
            throw new BadRequestException("Category with name '" + dto.getName() + "' already exists");
        }
        MedicineCategory category = MedicineCategory.builder()
                .name(dto.getName().trim())
                .description(dto.getDescription())
                .build();
        return mapToDto(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public MedicineCategoryDto updateCategory(Long id, MedicineCategoryDto dto) {
        MedicineCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MedicineCategory", "id", id));
        category.setName(dto.getName().trim());
        category.setDescription(dto.getDescription());
        return mapToDto(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("MedicineCategory", "id", id);
        }
        categoryRepository.deleteById(id);
    }

    private MedicineCategoryDto mapToDto(MedicineCategory cat) {
        return MedicineCategoryDto.builder()
                .id(cat.getId())
                .name(cat.getName())
                .description(cat.getDescription())
                .createdAt(cat.getCreatedAt())
                .build();
    }
}
