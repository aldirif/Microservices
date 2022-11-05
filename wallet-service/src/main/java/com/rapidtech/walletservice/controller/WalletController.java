package com.rapidtech.walletservice.controller;

import com.rapidtech.walletservice.dto.WalletRequest;
import com.rapidtech.walletservice.dto.WalletResponse;
import com.rapidtech.walletservice.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;

    @GetMapping
    public List<WalletResponse> getAllWallet() {
        return walletService.getAllWallet();
    }

    @GetMapping("/checkbalance")
    @ResponseStatus(HttpStatus.OK)
    public WalletResponse checkBalance(@RequestParam String username){
        return walletService.checkBalance(username);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String insertWallet(@RequestBody WalletRequest walletRequest){
        walletService.insertWallet(walletRequest);
        return "Data wallet added";
    }
    @PostMapping("/topupwallet")
    @ResponseStatus(HttpStatus.CREATED)
    public WalletResponse topUpWallet (@RequestBody WalletRequest walletRequest){
        return walletService.topUpWallet(walletRequest);
    }

    @PostMapping("/decreasebalance")
    @ResponseStatus(HttpStatus.CREATED)
    public WalletResponse decreaseBalance (@RequestBody WalletRequest walletRequest){
        return walletService.decreaseBalance(walletRequest);
    }
}
