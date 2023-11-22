package com.picpay.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.picpay.domain.transaction.Transaction;
import com.picpay.domain.user.User;
import com.picpay.dtos.TransactionDTO;
import com.picpay.repositories.TransactionRepository;


@Service
public class TransactionService {

    @Autowired
    private UserService userService;
    
    @Autowired
    private TransactionRepository repository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private RestTemplate restTemplate;
    
    public Transaction createTransaction(TransactionDTO transaction) throws Exception {

        User payer = this.userService.findUserById(transaction.payerId());
        User payee = this.userService.findUserById(transaction.payeeId());

        userService.validateTransaction(payer, transaction.value());

        boolean isAuthorized = this.authorizeTransaction(payer, transaction.value());
        if(!isAuthorized){
            throw new Exception("Transação não autorizada!");
        }

        Transaction newTransaction = new Transaction();
        newTransaction.setAmount(transaction.value());
        newTransaction.setPayer(payer);
        newTransaction.setPayee(payee);
        newTransaction.setTimestamp(LocalDateTime.now());

        payer.setBalance(payer.getBalance().subtract(transaction.value()));
        payee.setBalance(payee.getBalance().add(transaction.value()));

        this.repository.save(newTransaction);
        this.userService.saveUser(payer);
        this.userService.saveUser(payee);

        this.notificationService.sendNotification(payer, "Transação realizada com sucesso.");
        this.notificationService.sendNotification(payee, "Transação recebida com sucesso.");


        return newTransaction;

    }

    public boolean authorizeTransaction(User payer, BigDecimal value){
       ResponseEntity<Map> authorizationResponse = restTemplate.getForEntity("https://run.mocky.io/v3/5794d450-d2e2-4412-8131-73d0293ac1cc", Map.class);

       if(authorizationResponse.getStatusCode() == HttpStatus.OK) {
        String message = (String) authorizationResponse.getBody().get("message");

        return "Autorizado".equalsIgnoreCase(message);
       } else {
        return false;
       }
    }
}
