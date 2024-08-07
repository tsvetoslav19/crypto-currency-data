package com.example.cryptoCurrencyDataProject.service;

import com.example.cryptoCurrencyDataProject.entity.CryptoCurrencyData;
import com.example.cryptoCurrencyDataProject.repository.CryptoCurrencyDataRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CryptoCurrencyDataService {
    private static final Logger logger = LoggerFactory.getLogger(CryptoCurrencyDataService.class);
    private static final String COINCAP_API_URL = "https://api.coincap.io/v2/assets";
    private static final String BINANCE_API_URL = "https://api.binance.com/api/v3/klines";

    private final CryptoCurrencyDataRepository cryptoCurrencyDataRepository;

    @Value("${api.key}")
    private String apiKey;

    public CryptoCurrencyDataService(CryptoCurrencyDataRepository cryptoCurrencyDataRepository) {
        this.cryptoCurrencyDataRepository = cryptoCurrencyDataRepository;
    }

    @Transactional
    public List<CryptoCurrencyData> fetchAndSaveCryptoCurrencyData() {
        logger.info("Fetching crypto data from CoinCap API...");
        List<CryptoCurrencyData> cryptoCurrencyDataList = new ArrayList<>();
        HttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(COINCAP_API_URL);

        httpGet.addHeader("Authorization", "Bearer " + apiKey);

        try {
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                try (InputStream inputStream = entity.getContent()) {
                    JsonNode rootNode = objectMapper.readTree(inputStream);
                    JsonNode dataNode = rootNode.get("data");

                    if (dataNode != null && dataNode.isArray()) {
                        for (JsonNode asset : dataNode) {
                            CryptoCurrencyData cryptoCurrencyData = new CryptoCurrencyData();
                            cryptoCurrencyData.setCurrency(asset.get("id").asText());
                            cryptoCurrencyData.setPrice(asset.get("priceUsd").asDouble());
                            cryptoCurrencyData.setMarketCap(asset.get("marketCapUsd").asDouble());
                            cryptoCurrencyData.setVolume(asset.get("volumeUsd24Hr").asDouble());
                            cryptoCurrencyData.setTimestamp(LocalDateTime.now());

                            // Check if an entry with the same currency and timestamp already exists
                            Optional<CryptoCurrencyData> existingData = cryptoCurrencyDataRepository
                                    .findByCurrencyAndTimestamp(cryptoCurrencyData.getCurrency(), cryptoCurrencyData.getTimestamp());

                            if (existingData.isEmpty()) {
                                cryptoCurrencyDataList.add(cryptoCurrencyData);
                            }
                        }
                    } else {
                        logger.warn("No data found in the response.");
                    }

                    cryptoCurrencyDataRepository.saveAll(cryptoCurrencyDataList);
                    logger.info("Crypto Currency Data saved to database.");
                }
            }
        } catch (IOException e) {
            logger.error("Error fetching Crypto Currency Data", e);
        }
        return cryptoCurrencyDataList;
    }

    public List<CryptoCurrencyData> fetchHistoricalData(String symbol, String interval) {
        logger.info("Fetching historical crypto data from Binance API...");
        List<CryptoCurrencyData> historicalDataList = new ArrayList<>();
        HttpClient httpClient = HttpClients.createDefault();

        String apiUrl = BINANCE_API_URL + "?symbol=" + symbol + "&interval=" + interval + "&limit=1000";
        HttpGet httpGet = new HttpGet(apiUrl);

        try {
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                try (InputStream inputStream = entity.getContent()) {
                    JsonNode rootNode = objectMapper.readTree(inputStream);

                    if (rootNode.isArray()) {
                        for (JsonNode data : rootNode) {
                            CryptoCurrencyData cryptoCurrencyData = new CryptoCurrencyData();
                            cryptoCurrencyData.setCurrency(symbol);
                            cryptoCurrencyData.setPrice(data.get(4).asDouble());
                            cryptoCurrencyData.setTimestamp(LocalDateTime.ofEpochSecond(data.get(0).asLong() / 1000, 0, ZoneOffset.UTC));

                            // Check if an entry with the same currency and timestamp already exists
                            Optional<CryptoCurrencyData> existingData = cryptoCurrencyDataRepository
                                    .findByCurrencyAndTimestamp(cryptoCurrencyData.getCurrency(), cryptoCurrencyData.getTimestamp());

                            if (existingData.isEmpty()) {
                                historicalDataList.add(cryptoCurrencyData);
                            }
                        }
                    } else {
                        logger.warn("No historical data found in the response.");
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Error fetching historical Crypto Currency Data", e);
        }
        return historicalDataList;
    }

    public List<CryptoCurrencyData> getSmoothedCryptoData() {
        List<CryptoCurrencyData> dataList = cryptoCurrencyDataRepository.findAll();
        int windowSize = 5; // Window size for moving average

        List<CryptoCurrencyData> smoothedData = new ArrayList<>();

        for (int i = 0; i < dataList.size(); i++) {
            int start = Math.max(0, i - windowSize + 1);
            int end = i + 1;

            double sum = 0;
            for (int j = start; j < end; j++) {
                sum += dataList.get(j).getPrice();
            }
            double average = sum / (end - start);

            CryptoCurrencyData smoothedEntry = new CryptoCurrencyData();
            smoothedEntry.setCurrency(dataList.get(i).getCurrency());
            smoothedEntry.setPrice(average);
            smoothedEntry.setMarketCap(dataList.get(i).getMarketCap());
            smoothedEntry.setVolume(dataList.get(i).getVolume());
            smoothedEntry.setTimestamp(dataList.get(i).getTimestamp());

            smoothedData.add(smoothedEntry);
        }

        return smoothedData;
    }
}
