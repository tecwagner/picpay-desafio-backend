package com.picpay.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import com.picpay.domain.user.User;
import com.picpay.domain.user.UserType;
import com.picpay.dtos.TransactionDTO;
import com.picpay.repositories.TransactionRepository;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private UserService userService;
    
    @Mock
    private TransactionRepository repository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AuthorizationService authService;

    @Autowired
    @InjectMocks
    private TransactionService transactionService;
 
    @Test
    @DisplayName("should create transaction successfully. When everything is OK")    
    void createTransactionSuccess() throws Exception{
        User payer = new User(1L, "Wagner", "Rodrigues", "12345678998", "wagner@gmail.com", "1234", new BigDecimal(100), UserType.COMMON);
        User payee = new User(2L, "Amanda", "Rodrigues", "12345678999", "amanda@gmail.com", "1234", new BigDecimal(100), UserType.MERCHANT);
    
        when(userService.findUserById(1L)).thenReturn(payer);
        when(userService.findUserById(2L)).thenReturn(payee);

        when(authService.authorizeTransaction(any(), any())).thenReturn(true);

        TransactionDTO request = new TransactionDTO(new BigDecimal(50), 1L, 2L);
        transactionService.createTransaction(request);

        verify(repository, times(1)).save(any());

        payer.setBalance(new BigDecimal(50));
        verify(userService, times(1)).saveUser(payer);

        payee.setBalance(new BigDecimal(150));
        verify(userService, times(1)).saveUser(payee);

        verify(notificationService, times(1)).sendNotification(payee, "Transação recebida com sucesso.");
        verify(notificationService, times(1)).sendNotification(payer, "Transação realizada com sucesso.");

        }

    @Test
    @DisplayName("should throw Exception when Transaction is not allowed")    
    void createTransactionIsNotAllowed() throws Exception{
        User payer = new User(1L, "Wagner", "Rodrigues", "12345678998", "wagner@gmail.com", "1234", new BigDecimal(100), UserType.COMMON);
        User payee = new User(2L, "Amanda", "Rodrigues", "12345678999", "amanda@gmail.com", "1234", new BigDecimal(100), UserType.MERCHANT);
    
        when(userService.findUserById(1L)).thenReturn(payer);
        when(userService.findUserById(2L)).thenReturn(payee);

        when(authService.authorizeTransaction(any(), any())).thenReturn(false);

        Exception thrown = assertThrows(Exception.class, () -> {
            TransactionDTO request = new TransactionDTO(new BigDecimal(50), 1L, 2L);            
            transactionService.createTransaction(request);
        });

        assertEquals("Transação não autorizada!", thrown.getMessage());
    }
}
