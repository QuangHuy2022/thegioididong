package com.example.demo_3001.controller;

import com.example.demo_3001.model.CartItem;
import com.example.demo_3001.model.Product;
import com.example.demo_3001.service.CartService;
import com.example.demo_3001.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final ProductService productService;

    public CartController(CartService cartService, ProductService productService) {
        this.cartService = cartService;
        this.productService = productService;
    }

    @GetMapping
    public String showCart(HttpSession session, Model model) {
        List<CartItem> cart = cartService.getCartItems(session);
        model.addAttribute("cart", cart);
        model.addAttribute("totalPrice", cartService.getTotalPrice(session));
        return "cart/cart";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam("productId") Long productId, @RequestParam("quantity") int quantity, HttpSession session) {
        Product product = productService.getProductById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + productId));
        cartService.addToCart(session, product, quantity);
        return "redirect:/cart";
    }

    @GetMapping("/remove/{id}")
    public String removeFromCart(@PathVariable("id") Long id, HttpSession session) {
        cartService.removeFromCart(session, id);
        return "redirect:/cart";
    }

    @PostMapping("/update")
    public String updateQuantity(@RequestParam("productId") Long productId, @RequestParam("quantity") int quantity, HttpSession session) {
        cartService.updateQuantity(session, productId, quantity);
        return "redirect:/cart";
    }
}
