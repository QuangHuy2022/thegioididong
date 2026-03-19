package com.example.demo_3001.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class MomoService {

    @Value("${momo.partner-code}")
    private String partnerCode;

    @Value("${momo.access-key}")
    private String accessKey;

    @Value("${momo.secret-key}")
    private String secretKey;

    @Value("${momo.endpoint}")
    private String endpoint;

    @Value("${momo.return-url}")
    private String returnUrl;

    @Value("${momo.notify-url}")
    private String notifyUrl;

    public Map<String, Object> createPayment(String orderId, long amount, String orderInfo) throws Exception {
        String requestId = orderId;
        String requestType = "captureMoMoWallet";
        String extraData = "";

        // Raw signature order for captureMoMoWallet:
        // partnerCode=$partnerCode&accessKey=$accessKey&requestId=$requestId&amount=$amount&orderId=$orderId&orderInfo=$orderInfo&returnUrl=$returnUrl&notifyUrl=$notifyUrl&extraData=$extraData
        String rawSignature = "partnerCode=" + partnerCode +
                "&accessKey=" + accessKey +
                "&requestId=" + requestId +
                "&amount=" + amount +
                "&orderId=" + orderId +
                "&orderInfo=" + orderInfo +
                "&returnUrl=" + returnUrl +
                "&notifyUrl=" + notifyUrl +
                "&extraData=" + extraData;

        String signature = hmacSha256(rawSignature, secretKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("partnerCode", partnerCode);
        requestBody.put("accessKey", accessKey);
        requestBody.put("requestId", requestId);
        requestBody.put("amount", String.valueOf(amount));
        requestBody.put("orderId", orderId);
        requestBody.put("orderInfo", orderInfo);
        requestBody.put("returnUrl", returnUrl);
        requestBody.put("notifyUrl", notifyUrl);
        requestBody.put("extraData", extraData);
        requestBody.put("requestType", requestType);
        requestBody.put("signature", signature);

        RestTemplate restTemplate = new RestTemplate();
        try {
            Map<String, Object> response = restTemplate.postForObject(endpoint, requestBody, Map.class);
            System.out.println("MoMo API Response: " + response);
            return response;
        } catch (Exception e) {
            System.err.println("Error calling MoMo API: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private String hmacSha256(String data, String key) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        return toHexString(sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }

    private String toHexString(byte[] bytes) {
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }
}
