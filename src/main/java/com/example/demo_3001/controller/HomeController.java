package com.example.demo_3001.controller;

import com.example.demo_3001.model.Category;
import com.example.demo_3001.model.Product;
import com.example.demo_3001.service.CategoryService;
import com.example.demo_3001.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private final ProductService productService;
    private final CategoryService categoryService;

    public HomeController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping("/product/{id}")
    public String productDetail(@org.springframework.web.bind.annotation.PathVariable("id") Long id, Model model) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
        model.addAttribute("product", product);
        
        // Thêm các sản phẩm liên quan (cùng danh mục)
        if (product.getCategory() != null) {
            List<Product> relatedProducts = productService.getProductsByCategory(product.getCategory().getId())
                    .stream()
                    .filter(p -> !p.getId().equals(id))
                    .limit(6)
                    .collect(Collectors.toList());
            model.addAttribute("relatedProducts", relatedProducts);
        }
        
        // Lấy lại menu danh mục cho header
        List<Category> allCategories = categoryService.getAllCategories();
        List<String> level1Names = Arrays.asList(
                "Điện thoại", "Laptop", "Phụ kiện", "Smartwatch", "Đồng hồ",
                "Tablet", "Máy cũ Thu cũ", "Màn hình máy in", "Sim Thẻ Cào", "Dịch vụ tiện ích"
        );
        Map<String, List<Category>> categoryMap = allCategories.stream()
                .filter(c -> c.getParentCategory() != null && level1Names.contains(c.getParentCategory().getName()))
                .collect(Collectors.groupingBy(
                        c -> c.getParentCategory().getName(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
        model.addAttribute("categoryMap", categoryMap);
        
        return "products/product-detail";
    }

    @GetMapping("/")
    public String home(Model model) {

        // Flash Sale
        List<Product> flashSaleProducts = productService.getProductsByType("KHUYEN_MAI")
                .stream().limit(12).collect(Collectors.toList());

        // Gợi ý
        List<Product> suggestedProducts = productService.getProductsByType("NONE")
                .stream().limit(12).collect(Collectors.toList());

        // Quà tặng
        List<Product> giftProducts = productService.getProductsByType("QUA_TANG")
                .stream().limit(12).collect(Collectors.toList());

        model.addAttribute("flashSaleProducts", flashSaleProducts);
        model.addAttribute("suggestedProducts", suggestedProducts);
        model.addAttribute("giftProducts", giftProducts);

        List<Category> allCategories = categoryService.getAllCategories();
        model.addAttribute("categories", allCategories);

        // Danh mục cấp 1
        List<String> level1Names = Arrays.asList(
                "Điện thoại", "Laptop", "Phụ kiện", "Smartwatch", "Đồng hồ",
                "Tablet", "Máy cũ Thu cũ", "Màn hình máy in", "Sim Thẻ Cào", "Dịch vụ tiện ích"
        );

        // ================= LỚP 2 =================
        Map<String, List<Category>> level2Map = allCategories.stream()
                .filter(c -> c.getParentCategory() != null
                        && level1Names.contains(c.getParentCategory().getName()))
                .collect(Collectors.groupingBy(
                        c -> c.getParentCategory().getName(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        // Tập tên lớp 2
        Set<String> level2Names = allCategories.stream()
                .filter(c -> c.getParentCategory() != null
                        && level1Names.contains(c.getParentCategory().getName()))
                .map(Category::getName)
                .collect(Collectors.toSet());

        // ================= LỚP 3 =================
        Map<String, List<Category>> level3Map = allCategories.stream()
                .filter(c -> c.getParentCategory() != null
                        && level2Names.contains(c.getParentCategory().getName()))
                .collect(Collectors.groupingBy(
                        c -> c.getParentCategory().getName(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        // ================= TẠO MAP 2 CẤP CHO MENU =================
        Map<String, List<Category>> categoryMap = new LinkedHashMap<>();

        for (String l1 : level1Names) {
            List<Category> l2List = level2Map.getOrDefault(l1, new ArrayList<>());
            if (!l2List.isEmpty()) {
                categoryMap.put(l1, l2List);
            }
        }

        model.addAttribute("categoryMap", categoryMap);

        // ================= MAP 2 CẤP (fallback) =================
        Map<String, List<Category>> categoryMap2 = allCategories.stream()
                .filter(c -> c.getParentCategory() != null)
                .collect(Collectors.groupingBy(
                        c -> c.getParentCategory().getName(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        model.addAttribute("categoryMap2", categoryMap2);

        return "index";
    }
}