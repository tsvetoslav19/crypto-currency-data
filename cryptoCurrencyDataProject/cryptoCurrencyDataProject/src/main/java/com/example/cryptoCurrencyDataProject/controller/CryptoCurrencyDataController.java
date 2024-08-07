package com.example.cryptoCurrencyDataProject.controller;

import com.example.cryptoCurrencyDataProject.entity.CryptoCurrencyData;
import com.example.cryptoCurrencyDataProject.service.CryptoCurrencyDataService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Controller
public class CryptoCurrencyDataController {
    private final CryptoCurrencyDataService cryptoCurrencyDataService;

    public CryptoCurrencyDataController(CryptoCurrencyDataService cryptoCurrencyDataService) {
        this.cryptoCurrencyDataService = cryptoCurrencyDataService;
    }

    @GetMapping("/")
    public String index(@RequestParam(name = "view", required = false) String view, Model model) {
        model.addAttribute("view", view);
        if ("data".equals(view)) {
            model.addAttribute("cryptoCurrencyData", cryptoCurrencyDataService.fetchAndSaveCryptoCurrencyData());
        }
        return "index";
    }
}

@RestController
@RequestMapping("/api")
class CryptoCurrencyDataApiController {
    private final CryptoCurrencyDataService cryptoCurrencyDataService;

    public CryptoCurrencyDataApiController(CryptoCurrencyDataService cryptoCurrencyDataService) {
        this.cryptoCurrencyDataService = cryptoCurrencyDataService;
    }

    @GetMapping("/data")
    public List<CryptoCurrencyData> getCryptoCurrencyData() {
        return cryptoCurrencyDataService.fetchAndSaveCryptoCurrencyData();
    }

    @GetMapping("/history")
    public List<CryptoCurrencyData> getCryptoCurrencyHistoryData(@RequestParam String symbol, @RequestParam String interval) {
        return cryptoCurrencyDataService.fetchHistoricalData(symbol, interval);
    }

    @GetMapping("/smoothData")
    public List<CryptoCurrencyData> getSmoothedCryptoData() {
        return cryptoCurrencyDataService.getSmoothedCryptoData();
    }
}
