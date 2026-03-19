package com.example.demo_3001.service;

import com.example.demo_3001.model.User;
import com.example.demo_3001.model.Voucher;
import com.example.demo_3001.repository.UserRepository;
import com.example.demo_3001.repository.VoucherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class VoucherService {

    private final VoucherRepository voucherRepository;
    private final UserRepository userRepository;

    public VoucherService(VoucherRepository voucherRepository, UserRepository userRepository) {
        this.voucherRepository = voucherRepository;
        this.userRepository = userRepository;
    }

    public List<Voucher> getAllVouchers() {
        return voucherRepository.findAll();
    }

    public Voucher saveVoucher(Voucher voucher) {
        return voucherRepository.save(voucher);
    }

    public Optional<Voucher> getVoucherById(Long id) {
        return voucherRepository.findById(id);
    }

    public void deleteVoucher(Long id) {
        voucherRepository.deleteById(id);
    }

    @Transactional
    public boolean redeemVoucher(User user, Long voucherId) {
        Voucher voucher = voucherRepository.findById(voucherId).orElse(null);
        if (voucher == null || !voucher.isActive()) return false;

        // 2 điểm = 15k. Giả sử 1 voucher có giá trị đổi bằng điểm tương ứng với discountAmount
        // Ví dụ: Voucher giảm 15k tốn 2 điểm, giảm 30k tốn 4 điểm.
        int requiredPoints = (int) (voucher.getDiscountAmount() / 15000) * 2;
        
        if (user.getRewardPoints() >= requiredPoints) {
            user.setRewardPoints(user.getRewardPoints() - requiredPoints);
            user.getVouchers().add(voucher);
            userRepository.save(user);
            return true;
        }
        return false;
    }
}
