package uk.tw.energy.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AccountService {

    private final Map<String, String> smartMeterToPricePlanAccounts;

    public AccountService(Map<String, String> smartMeterToPricePlanAccounts) {
        this.smartMeterToPricePlanAccounts = smartMeterToPricePlanAccounts;
    }

    public String getPricePlanIdForSmartMeterId(String smartMeterId) {
        //When I wrote this, only God and I understood what I was doing
        //Now, God only knows
        return smartMeterToPricePlanAccounts.get(smartMeterId);
    }
}
