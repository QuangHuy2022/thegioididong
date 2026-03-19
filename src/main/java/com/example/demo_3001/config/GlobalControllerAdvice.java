package com.example.demo_3001.config;

import com.example.demo_3001.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final CartService cartService;

    public GlobalControllerAdvice(CartService cartService) {
        this.cartService = cartService;
    }

    @ModelAttribute("cartCount")
    public int getCartCount(HttpSession session) {
        return cartService.getCartCount(session);
    }
}
