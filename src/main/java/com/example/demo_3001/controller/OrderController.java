package com.example.demo_3001.controller;

import com.example.demo_3001.model.CartItem;
import com.example.demo_3001.model.Order;
import com.example.demo_3001.model.Product;
import com.example.demo_3001.model.User;
import com.example.demo_3001.model.Voucher;
import com.example.demo_3001.repository.UserRepository;
import com.example.demo_3001.service.CartService;
import com.example.demo_3001.service.MomoService;
import com.example.demo_3001.service.OrderService;
import com.example.demo_3001.service.VoucherService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;
    private final UserRepository userRepository;
    private final MomoService momoService;
    private final VoucherService voucherService;

    public OrderController(OrderService orderService, CartService cartService, UserRepository userRepository, MomoService momoService, VoucherService voucherService) {
        this.orderService = orderService;
        this.cartService = cartService;
        this.userRepository = userRepository;
        this.momoService = momoService;
        this.voucherService = voucherService;
    }

    @GetMapping("/checkout")
    public String checkout(HttpSession session, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = null;
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            user = userRepository.findByUsername(auth.getName()).orElse(null);
        }

        List<CartItem> cart = cartService.getCartItems(session);
        if (cart.isEmpty()) {
            return "redirect:/cart";
        }
        
        double subtotal = 0;
        int totalQuantity = 0;
        for (CartItem item : cart) {
            Product product = item.getProduct();
            int quantity = item.getQuantity();
            
            double itemTotal;
            if (product.isPromotion()) {
                int promoQty = product.getPromotionQuantity();
                if (quantity <= promoQty) {
                    itemTotal = product.getPrice() * quantity;
                } else {
                    itemTotal = (product.getPrice() * promoQty) + (product.getOriginalPrice() * (quantity - promoQty));
                }
            } else {
                itemTotal = product.getPrice() * quantity;
            }
            
            subtotal += itemTotal;
            totalQuantity += quantity;
        }
        
        double shippingFee = (subtotal >= 1000000 && totalQuantity >= 2) ? 0 : 30000;
        double totalPrice = subtotal + shippingFee;
        int rewardPoints = (int) (subtotal / 15000) * 2;

        model.addAttribute("cart", cart);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("shippingFee", shippingFee);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("rewardPoints", rewardPoints);
        model.addAttribute("userVouchers", user != null ? user.getVouchers() : null);
        return "orders/checkout";
    }

    @PostMapping("/place")
    public String placeOrder(@RequestParam("customerName") String customerName, 
                            @RequestParam(value = "paymentMethod", defaultValue = "COD") String paymentMethod,
                            @RequestParam(value = "voucherId", required = false) Long voucherId,
                            HttpSession session, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = null;
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            user = userRepository.findByUsername(auth.getName()).orElse(null);
        }
        
        List<CartItem> cart = cartService.getCartItems(session);
        if (cart.isEmpty()) {
            return "redirect:/cart";
        }
        
        Voucher voucher = null;
        if (voucherId != null) {
            voucher = voucherService.getVoucherById(voucherId).orElse(null);
        }

        Order order = orderService.createOrder(user, cart, customerName, voucher);
        
        if ("MOMO".equals(paymentMethod)) {
            // Đối với mục đích test: luôn ghi nhận là đã thanh toán khi nhấn nút
            order.setStatus("PAID");
            orderService.saveOrder(order);
            
            try {
                String orderId = "MOMO_" + order.getId() + "_" + UUID.randomUUID().toString().substring(0, 8);
                Map<String, Object> momoResponse = momoService.createPayment(
                        orderId, 
                        (long) order.getTotalAmount(), 
                        "Thanh toan don hang #" + order.getId()
                );
                
                if (momoResponse != null && (momoResponse.containsKey("payUrl") || momoResponse.containsKey("shortLink"))) {
                    String payUrl = (String) momoResponse.get("payUrl");
                    if (payUrl == null) payUrl = (String) momoResponse.get("shortLink");
                    
                    order.setMomoOrderId(orderId);
                    order.setMomoRequestId((String) momoResponse.get("requestId"));
                    orderService.saveOrder(order);
                    
                    cartService.clearCart(session); // Clear cart before redirecting
                    return "redirect:" + payUrl;
                } else {
                    System.out.println("Momo Error Response: " + momoResponse);
                    order.setStatus("FAILED");
                    orderService.saveOrder(order);
                    return "redirect:/orders/checkout?error=momo_error";
                }
            } catch (Exception e) {
                e.printStackTrace();
                order.setStatus("FAILED");
                orderService.saveOrder(order);
                return "redirect:/orders/checkout?error=server_error";
            }
        }
        
        cartService.clearCart(session);
        return "redirect:/orders/success";
    }

    @PostMapping("/cancel/{id}")
    public String cancelOrder(@org.springframework.web.bind.annotation.PathVariable("id") Long id) {
        orderService.cancelOrder(id);
        return "redirect:/orders/my-orders";
    }

    @GetMapping("/momo-callback")
    public String momoCallback(@RequestParam Map<String, String> params, HttpSession session) {
        System.out.println("MoMo Callback received at: " + new java.util.Date());
        System.out.println("MoMo Callback Params: " + params);
        
        String momoOrderId = params.get("orderId");
        String message = params.get("message");
        
        // Luôn ghi nhận là đã thanh toán cho mục đích test
        orderService.getOrderByMomoOrderId(momoOrderId).ifPresentOrElse(order -> {
            System.out.println("Forcing order #" + order.getId() + " status to PAID for testing");
            order.setStatus("PAID");
            orderService.saveOrder(order);
        }, () -> System.out.println("Order not found for momoOrderId: " + momoOrderId));
        
        cartService.clearCart(session);
        return "redirect:/orders/success";
    }

    @GetMapping("/success")
    public String orderSuccess() {
        return "orders/success";
    }

    @GetMapping("/my-orders")
    public String myOrders(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        List<Order> orders = orderService.getOrdersByUser(user);
        model.addAttribute("orders", orders);
        return "orders/order-history";
    }

    @GetMapping
    public String listAllOrders(Model model) {
        List<Order> orders = orderService.getAllOrders();
        model.addAttribute("orders", orders);
        return "orders/admin-order-list";
    }
}
