package com.picpay.services;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.picpay.domain.user.User;

@Service
public class AuthorizationService {
    
    @Autowired
    private RestTemplate restTemplate;

      public boolean authorizeTransaction(User payer, BigDecimal bigDecimal){
       ResponseEntity<Map> authorizationResponse = restTemplate.getForEntity("https://run.mocky.io/v3/5794d450-d2e2-4412-8131-73d0293ac1cc", Map.class);

       if(authorizationResponse.getStatusCode() == HttpStatus.OK) {
        String message = (String) authorizationResponse.getBody().get("message");

        return "Autorizado".equalsIgnoreCase(message);
       } else {
        return false;
       }
    }
}
