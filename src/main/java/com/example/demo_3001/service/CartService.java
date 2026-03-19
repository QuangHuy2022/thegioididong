package com.example.demo_3001.service;

import com.example.demo_3001.model.CartItem;
import com.example.demo_3001.model.Product;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartService {
    private static final String CART_SESSION_KEY = "cart";

    public List<CartItem> getCartItems(HttpSession session) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute(CART_SESSION_KEY);
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute(CART_SESSION_KEY, cart);
        }
        return cart;
    }

    public void addToCart(HttpSession session, Product product, int quantity) {
        List<CartItem> cart = getCartItems(session);
        boolean found = false;
        for (CartItem item : cart) {
            if (item.getProduct().getId().equals(product.getId())) {
                item.setQuantity(item.getQuantity() + quantity);
                found = true;
                break;
            }
        }
        if (!found) {
            cart.add(new CartItem(product, quantity));
        }
    }

    public void removeFromCart(HttpSession session, Long productId) {
        List<CartItem> cart = getCartItems(session);
        cart.removeIf(item -> item.getProduct().getId().equals(productId));
    }

    public void updateQuantity(HttpSession session, Long productId, int quantity) {
        List<CartItem> cart = getCartItems(session);
        for (CartItem item : cart) {
            if (item.getProduct().getId().equals(productId)) {
                item.setQuantity(quantity);
                break;
            }
        }
    }

    public void clearCart(HttpSession session) {
        session.removeAttribute(CART_SESSION_KEY);
    }

    public double getTotalPrice(HttpSession session) {
        List<CartItem> cart = getCartItems(session);
        return cart.stream().mapToDouble(CartItem::getTotalPrice).sum();
    }

    public int getCartCount(HttpSession session) {
        List<CartItem> cart = getCartItems(session);
        return cart.stream().mapToInt(CartItem::getQuantity).sum();
    }
}
