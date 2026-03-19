package com.example.demo_3001.controller;

import com.example.demo_3001.model.User;
import com.example.demo_3001.model.Voucher;
import com.example.demo_3001.repository.UserRepository;
import com.example.demo_3001.service.EmailService;
import com.example.demo_3001.service.VoucherService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/vouchers")
public class VoucherController {

    private final VoucherService voucherService;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public VoucherController(VoucherService voucherService, UserRepository userRepository, EmailService emailService) {
        this.voucherService = voucherService;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    // Admin Views
    @GetMapping
    public String listVouchers(Model model) {
        model.addAttribute("vouchers", voucherService.getAllVouchers());
        return "vouchers/voucher-list";
    }

    @GetMapping("/create")
    public String createVoucherForm(Model model) {
        model.addAttribute("voucher", new Voucher());
        return "vouchers/voucher-form";
    }

    @PostMapping("/save")
    public String saveVoucher(@ModelAttribute Voucher voucher) {
        voucherService.saveVoucher(voucher);
        return "redirect:/vouchers";
    }

    @GetMapping("/edit/{id}")
    public String editVoucherForm(@PathVariable Long id, Model model) {
        voucherService.getVoucherById(id).ifPresent(voucher -> model.addAttribute("voucher", voucher));
        return "vouchers/voucher-form";
    }

    @GetMapping("/delete/{id}")
    public String deleteVoucher(@PathVariable Long id) {
        voucherService.deleteVoucher(id);
        return "redirect:/vouchers";
    }

    // User Views
    @GetMapping("/redeem")
    public String redeemView(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/login";
        }
        User user = userRepository.findByUsername(auth.getName()).orElse(null);
        if (user == null) return "redirect:/login";
        
        model.addAttribute("vouchers", voucherService.getAllVouchers());
        model.addAttribute("user", user);
        return "vouchers/redeem";
    }

    @PostMapping("/redeem/{id}")
    public String redeemVoucherRequest(@PathVariable Long id, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByUsername(auth.getName()).orElse(null);
        
        if (user != null) {
            // Kiểm tra điểm trước khi gửi OTP
            Voucher voucher = voucherService.getVoucherById(id).orElse(null);
            if (voucher == null) return "redirect:/vouchers/redeem?error=not_found";
            
            int requiredPoints = (int) (voucher.getDiscountAmount() / 15000) * 2;
            if (user.getRewardPoints() < requiredPoints) {
                return "redirect:/vouchers/redeem?error=points_insufficient";
            }

            String otp = emailService.generateOTP(user.getEmail());
            emailService.sendOTP(user.getEmail(), otp);
            return "redirect:/vouchers/verify-otp/" + id;
        }
        return "redirect:/login";
    }

    @GetMapping("/verify-otp/{id}")
    public String verifyOtpView(@PathVariable Long id, Model model) {
        model.addAttribute("voucherId", id);
        return "vouchers/verify-otp";
    }

    @PostMapping("/verify-otp/{id}")
    public String verifyOtp(@PathVariable Long id, @RequestParam("otp") String otp) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByUsername(auth.getName()).orElse(null);

        if (user != null && emailService.validateOTP(user.getEmail(), otp)) {
            if (voucherService.redeemVoucher(user, id)) {
                emailService.clearOTP(user.getEmail());
                return "redirect:/vouchers/redeem?success";
            }
            return "redirect:/vouchers/redeem?error=points_insufficient";
        }
        return "redirect:/vouchers/verify-otp/" + id + "?error=invalid_otp";
    }
}
