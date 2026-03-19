package com.example.demo_3001.config;

import com.example.demo_3001.model.Category;
import com.example.demo_3001.model.Product;
import com.example.demo_3001.model.Role;
import com.example.demo_3001.model.User;
import com.example.demo_3001.repository.CategoryRepository;
import com.example.demo_3001.repository.ProductRepository;
import com.example.demo_3001.repository.RoleRepository;
import com.example.demo_3001.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(CategoryRepository categoryRepository, RoleRepository roleRepository, UserRepository userRepository, ProductRepository productRepository, PasswordEncoder passwordEncoder) {
        this.categoryRepository = categoryRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Init Roles
        String[] roles = {"ROLE_USER", "ROLE_ADMIN", "ROLE_STAFF", "ROLE_MANAGER"};
        for (String roleName : roles) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                roleRepository.save(new Role(roleName));
            }
        }

        // Init Admin User
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@example.com");
            admin.setFullName("Administrator");
            Role adminRole = roleRepository.findByName("ROLE_ADMIN").get();
            admin.setRoles(new HashSet<>(Arrays.asList(adminRole)));
            userRepository.save(admin);
        }

        // Init Manager User
        if (userRepository.findByUsername("manager").isEmpty()) {
            User manager = new User();
            manager.setUsername("manager");
            manager.setPassword(passwordEncoder.encode("manager123"));
            manager.setEmail("manager@example.com");
            manager.setFullName("Product Manager");
            Role managerRole = roleRepository.findByName("ROLE_MANAGER").get();
            manager.setRoles(new HashSet<>(Arrays.asList(managerRole)));
            userRepository.save(manager);
        }

        // Init Categories
        List<String> level1Names = Arrays.asList(
                "Điện thoại", "Laptop", "Phụ kiện", "Smartwatch", "Đồng hồ",
                "Tablet", "Máy cũ Thu cũ", "Màn hình máy in", "Sim Thẻ Cào", "Dịch vụ tiện ích"
        );

        for (String name : level1Names) {
            if (!categoryRepository.findByName(name).isPresent()) {
                Category cat = new Category();
                cat.setName(name);
                categoryRepository.save(cat);
            }
        }

        // Init Sample Products
        if (productRepository.count() <= 3) { // If only initial samples exist
            Optional<Category> phoneCat = categoryRepository.findByName("Điện thoại");
            Optional<Category> accessoryCat = categoryRepository.findByName("Phụ kiện");
            
            if (phoneCat.isPresent()) {
                // 5 iPhone Promotion Products
                for (int i = 1; i <= 5; i++) {
                    Product p = new Product();
                    p.setName("iPhone 15 Pro Khuyến Mãi " + i);
                    p.setPrice(25000000 - (i * 100000));
                    p.setOriginalPrice(30000000);
                    p.setDescription("Sản phẩm iPhone khuyến mãi cực sốc phiên bản " + i);
                    p.setCategory(phoneCat.get());
                    p.setImage("1773366757288_tải xuống.jpg");
                    p.setProductType("KHUYEN_MAI");
                    p.setPromotion(true);
                    p.setPromotionQuantity(10 + i);
                    p.setDiscount(20);
                    productRepository.save(p);
                }

                // 5 Normal Products
                for (int i = 1; i <= 5; i++) {
                    Product p = new Product();
                    p.setName("Điện thoại Android Model " + i);
                    p.setPrice(15000000 + (i * 500000));
                    p.setOriginalPrice(15000000 + (i * 500000));
                    p.setDescription("Điện thoại Android chất lượng cao, màn hình sắc nét.");
                    p.setCategory(phoneCat.get());
                    p.setImage("1773455678579_images.png");
                    p.setProductType("NONE");
                    p.setPromotion(false);
                    p.setDiscount(0);
                    productRepository.save(p);
                }
            }

            if (accessoryCat.isPresent()) {
                // 5 Gift Products
                for (int i = 1; i <= 5; i++) {
                    Product p = new Product();
                    p.setName("Quà tặng đặc biệt " + i);
                    p.setPrice(0); // Quà tặng thường có giá 0 hoặc rất thấp
                    p.setOriginalPrice(500000);
                    p.setDescription("Quà tặng tri ân khách hàng thân thiết " + i);
                    p.setCategory(accessoryCat.get());
                    p.setImage("images.png");
                    p.setProductType("QUA_TANG");
                    p.setPromotion(false);
                    p.setDiscount(100);
                    productRepository.save(p);
                }
            }
        }
    }
}
