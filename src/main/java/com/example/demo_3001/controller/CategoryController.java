package com.example.demo_3001.controller;

import com.example.demo_3001.model.Category;
import com.example.demo_3001.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // ===================== LIST =====================
    @GetMapping
    public String listCategories(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        return "categories/categories-list";
    }

    // ===================== ADD =====================
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("allCategories", categoryService.getAllCategories());
        return "categories/add-category";
    }

    @PostMapping("/add")
    public String addCategory(@Valid @ModelAttribute Category category,
                              BindingResult result,
                              Model model,
                              @RequestParam("imageCategory") MultipartFile imageCategory) {

        if (result.hasErrors()) {
            model.addAttribute("allCategories", categoryService.getAllCategories());
            return "categories/add-category";
        }

        saveImage(imageCategory, category);
        categoryService.addCategory(category);

        return "redirect:/categories";
    }

    // ===================== EDIT =====================
    @GetMapping("/edit/{id}")
    public String showUpdateForm(@PathVariable Long id, Model model) {

        Category category = categoryService.getCategoryById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid category Id: " + id));

        model.addAttribute("category", category);
        model.addAttribute("allCategories", categoryService.getAllCategories());

        return "categories/update-category";
    }

    @PostMapping("/update/{id}")
    public String updateCategory(@PathVariable Long id,
                                 @Valid @ModelAttribute Category category,
                                 BindingResult result,
                                 Model model,
                                 @RequestParam("imageCategory") MultipartFile imageCategory) {

        if (result.hasErrors()) {
            category.setId(id);
            model.addAttribute("allCategories", categoryService.getAllCategories());
            return "categories/update-category";
        }

        Category existing = categoryService.getCategoryById(id).orElse(null);

        if (existing != null) {

            if (imageCategory.isEmpty()) {
                category.setImage(existing.getImage());
            } else {
                saveImage(imageCategory, category);
            }

            categoryService.updateCategory(category);
        }

        return "redirect:/categories";
    }

    // ===================== DELETE =====================
    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id) {

        categoryService.deleteCategoryById(id);

        return "redirect:/categories";
    }

    // ===================== SAVE IMAGE =====================
    private void saveImage(MultipartFile file, Category category) {

        if (file == null || file.isEmpty()) return;

        try {

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

            Path srcPath = Paths.get("src/main/resources/static/images/" + fileName);
            Files.createDirectories(srcPath.getParent());
            Files.write(srcPath, file.getBytes());

            Path targetPath = Paths.get("target/classes/static/images/" + fileName);
            Files.createDirectories(targetPath.getParent());
            Files.write(targetPath, file.getBytes());

            category.setImage(fileName);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}