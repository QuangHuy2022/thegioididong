package com.example.demo_3001.controller;

import com.example.demo_3001.model.Product;
import com.example.demo_3001.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {
    
    private final ProductService productService;
    private final com.example.demo_3001.service.CategoryService categoryService;

    @Autowired
    public ProductController(ProductService productService, com.example.demo_3001.service.CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String listProducts(Model model) {
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);
        return "products/product-list";
    }

    @GetMapping("/new")
    public String showProductForm(Model model) {
        Product product = new Product();
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "products/product-form";
    }

    @PostMapping("/save")
    public String saveProduct(@ModelAttribute("product") Product product, 
                              @org.springframework.web.bind.annotation.RequestParam("imageProduct") org.springframework.web.multipart.MultipartFile imageProduct) {
        if (!imageProduct.isEmpty()) {
            try {
                String fileName = imageProduct.getOriginalFilename();
                java.nio.file.Path srcPath = java.nio.file.Paths.get("src/main/resources/static/images/" + fileName);
                java.nio.file.Files.createDirectories(srcPath.getParent());
                java.nio.file.Files.write(srcPath, imageProduct.getBytes());

                java.nio.file.Path targetPath = java.nio.file.Paths.get("target/classes/static/images/" + fileName);
                java.nio.file.Files.createDirectories(targetPath.getParent());
                java.nio.file.Files.write(targetPath, imageProduct.getBytes());
                
                product.setImage(fileName);
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }
        productService.saveProduct(product);
        return "redirect:/products";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@org.springframework.web.bind.annotation.PathVariable("id") Long id, Model model) {
        Product product = productService.getProductById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "products/product-form";
    }

    @GetMapping("/delete/{id}")
    public String deleteProduct(@org.springframework.web.bind.annotation.PathVariable("id") Long id) {
        productService.deleteProductById(id);
        return "redirect:/products";
    }
}
