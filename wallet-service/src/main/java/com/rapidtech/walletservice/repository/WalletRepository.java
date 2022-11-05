package com.rapidtech.walletservice.repository;

import com.rapidtech.walletservice.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletRepository extends JpaRepository<Wallet,String> {
}
