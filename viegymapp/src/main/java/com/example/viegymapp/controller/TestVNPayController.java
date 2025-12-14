package com.example.viegymapp.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/api/test")
@Slf4j
public class TestVNPayController {

    @Value("${vnpay.tmn-code:DEMOV210}")
    private String vnpayTmnCode;

    @Value("${vnpay.hash-secret:RAOEXHYVSDDIIENYWSLDIIZTANXUXZFJ}")
    private String vnpayHashSecret;

    @Value("${vnpay.url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String vnpayUrl;

    @Value("${vnpay.return-url:http://localhost:5173/payment/callback}")
    private String vnpayReturnUrl;

    @GetMapping("/vnpay")
    public Map<String, Object> testVNPay() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String orderId = "TEST_" + System.currentTimeMillis();
            
            Map<String, String> vnpParams = new TreeMap<>();
            vnpParams.put("vnp_Version", "2.1.0");
            vnpParams.put("vnp_Command", "pay");
            vnpParams.put("vnp_TmnCode", vnpayTmnCode);
            vnpParams.put("vnp_Amount", "10000000"); // 100,000 VND
            vnpParams.put("vnp_CurrCode", "VND");
            vnpParams.put("vnp_TxnRef", orderId);
            vnpParams.put("vnp_OrderInfo", "Test thanh toan VNPay");
            vnpParams.put("vnp_OrderType", "other");
            vnpParams.put("vnp_Locale", "vn");
            vnpParams.put("vnp_ReturnUrl", vnpayReturnUrl);
            vnpParams.put("vnp_IpAddr", "127.0.0.1");
            
            // Create date
            java.util.Calendar cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Etc/GMT+7"));
            java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyyMMddHHmmss");
            String vnpCreateDate = formatter.format(cal.getTime());
            vnpParams.put("vnp_CreateDate", vnpCreateDate);
            
            // Expire date (15 minutes from now)
            cal.add(java.util.Calendar.MINUTE, 15);
            String vnpExpireDate = formatter.format(cal.getTime());
            vnpParams.put("vnp_ExpireDate", vnpExpireDate);

            // Sắp xếp params theo key
            List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
            Collections.sort(fieldNames);

            // Build hashData (KHÔNG encode) và query string (có encode)
            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();
            
            for (String fieldName : fieldNames) {
                String fieldValue = vnpParams.get(fieldName);
                
                // HashData: KHÔNG encode
                if (hashData.length() > 0) {
                    hashData.append('&');
                }
                hashData.append(fieldName).append('=').append(fieldValue);
                
                // Query string: CÓ encode
                if (query.length() > 0) {
                    query.append('&');
                }
                query.append(fieldName).append('=').append(java.net.URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
            }

            log.info("=== VNPay Test ===");
            log.info("TmnCode: {}", vnpayTmnCode);
            log.info("HashSecret: {}", vnpayHashSecret);
            log.info("HashData: {}", hashData.toString());
            
            String vnpSecureHash = hmacSHA512(hashData.toString(), vnpayHashSecret);
            log.info("SecureHash: {}", vnpSecureHash);
            
            String paymentUrl = vnpayUrl + "?" + query.toString() + "&vnp_SecureHash=" + vnpSecureHash;
            
            result.put("success", true);
            result.put("tmnCode", vnpayTmnCode);
            result.put("hashSecret", vnpayHashSecret);
            result.put("hashData", hashData.toString());
            result.put("secureHash", vnpSecureHash);
            result.put("paymentUrl", paymentUrl);
            result.put("params", vnpParams);
            
            log.info("Payment URL: {}", paymentUrl);
            
        } catch (Exception e) {
            log.error("Error testing VNPay", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    private String hmacSHA512(String data, String key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA512");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
        mac.init(secretKeySpec);
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder();
        for (byte b : hash) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
