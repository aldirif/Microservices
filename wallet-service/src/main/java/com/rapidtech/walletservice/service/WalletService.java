package com.rapidtech.walletservice.service;

import com.rapidtech.walletservice.dto.WalletRequest;
import com.rapidtech.walletservice.dto.WalletResponse;
import com.rapidtech.walletservice.event.OrderPlacedEvent;
import com.rapidtech.walletservice.model.Wallet;
import com.rapidtech.walletservice.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WalletService {
    private final WalletRepository walletRepository;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    public void insertWallet(WalletRequest walletRequest) {
    Wallet wallet = new Wallet();
    wallet.setUsername(walletRequest.getUsername());
    wallet.setBalance(walletRequest.getBalance());
    walletRepository.save(wallet);
}

    public WalletResponse topUpWallet(WalletRequest walletRequest) {
        Wallet topUp = Wallet.builder()
                .username(walletRequest.getUsername())
                .balance(walletRequest.getBalance())
                .build();
        Wallet wallet = walletRepository.findById(topUp.getUsername()).get();
        Double balance = wallet.getBalance() + topUp.getBalance();
        topUp.setBalance(balance);
        Wallet result = walletRepository.save(topUp);
        return WalletResponse.builder()
                .username(result.getUsername())
                .balance(result.getBalance())
                .build();
    }

    public WalletResponse decreaseBalance(WalletRequest walletRequest) {
        Wallet decrease = Wallet.builder()
                .username(walletRequest.getUsername())
                .balance(walletRequest.getBalance())
                .build();
        Wallet wallet = walletRepository.findById(decrease.getUsername()).get();
        Double balance = wallet.getBalance() - decrease.getBalance();
        decrease.setBalance(balance);
        Wallet result = walletRepository.save(decrease);
        kafkaTemplate.send("notificationTopic",new OrderPlacedEvent("Wallet: - "+wallet.getUsername()+" "+wallet.getBalance()));
        return WalletResponse.builder()
                .username(result.getUsername())
                .balance(result.getBalance())
                .build();
    }

    public WalletResponse checkBalance(String username) {
        //log.info("Mulai menunggu");
        //Thread.sleep(10000);
        //log.info("Selesai menunggu");
        Wallet wallet = walletRepository.findById(username).get();
        return WalletResponse.builder()
                .username(wallet.getUsername())
                .balance(wallet.getBalance())
                .build();
    }

    public List<WalletResponse> getAllWallet() {
        List<WalletResponse> walletResponseList = new ArrayList<>();
        List<Wallet> walletList = walletRepository.findAll();
        for(Wallet wallet : walletList){
            walletResponseList.add(WalletResponse.builder()
                    .username(wallet.getUsername())
                    .balance(wallet.getBalance())
                    .build());
        }
        return walletResponseList;
    }
}


