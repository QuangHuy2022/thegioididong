package com.example.demo_3001.service;

import com.example.demo_3001.model.Product;
import com.example.demo_3001.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Page<Product> getProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findAll(pageable);
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Product saveProduct(Product product) {
        if (product.isPromotion()) {
            List<Product> promoProducts = productRepository.findByIsPromotionTrue();
            if (promoProducts.size() >= 20 && !promoProducts.contains(product)) {
                throw new RuntimeException("Tối đa 20 sản phẩm khuyến mãi!");
            }
        }
        return productRepository.save(product);
    }

    public List<Product> getProductsByType(String productType) {
        return productRepository.findByProductType(productType);
    }

    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    public void deleteProductById(Long id) {
        productRepository.deleteById(id);
    }
}
