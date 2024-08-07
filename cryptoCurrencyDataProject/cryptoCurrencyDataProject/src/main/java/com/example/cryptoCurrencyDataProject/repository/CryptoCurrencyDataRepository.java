package com.example.cryptoCurrencyDataProject.repository;

import com.example.cryptoCurrencyDataProject.entity.CryptoCurrencyData;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.Optional;

public interface CryptoCurrencyDataRepository extends JpaRepository<CryptoCurrencyData, Long> {
    Optional<CryptoCurrencyData> findByCurrencyAndTimestamp(String currency, LocalDateTime timestamp);
}
