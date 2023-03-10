package uk.tw.energy.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.tw.energy.controller.exceptions.NoConsumptionException;
import uk.tw.energy.service.AccountService;
import uk.tw.energy.service.PricePlanService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/price-plans")
public class PricePlanComparatorController {

    public final static String PRICE_PLAN_ID_KEY = "pricePlanId";
    public final static String PRICE_PLAN_COMPARISONS_KEY = "pricePlanComparisons";
    private final PricePlanService pricePlanService;
    private final AccountService accountService;

    public PricePlanComparatorController(PricePlanService pricePlanService, AccountService accountService) {
        this.pricePlanService = pricePlanService;
        this.accountService = accountService;
    }

    @GetMapping("/compare-all/{smartMeterId}")
    public ResponseEntity<Map<String, Object>> calculatedCostForEachPricePlan(@PathVariable String smartMeterId) throws NoConsumptionException {
        String pricePlanId = accountService.getPricePlanIdForSmartMeterId(smartMeterId);

        Map<String, BigDecimal> consumptionsForPricePlans = getConsumptionsForPricePlans(smartMeterId);

        Map<String, Object> pricePlanComparisons = new HashMap<>();
        pricePlanComparisons.put(PRICE_PLAN_ID_KEY, pricePlanId);
        pricePlanComparisons.put(PRICE_PLAN_COMPARISONS_KEY, consumptionsForPricePlans);

        return ResponseEntity.ok(pricePlanComparisons);
    }

    @GetMapping("/recommend/{smartMeterId}")
    public ResponseEntity<List<Map.Entry<String, BigDecimal>>> recommendCheapestPricePlans(@PathVariable String smartMeterId,
                                                                                           @RequestParam(value = "limit", required = false) Integer limit) throws NoConsumptionException {
        Map<String, BigDecimal> consumptionsForPricePlans = getConsumptionsForPricePlans(smartMeterId);

        List<Map.Entry<String, BigDecimal>> cheapestPricePlans = extractCheapestPricePlans(consumptionsForPricePlans, limit);

        return ResponseEntity.ok(cheapestPricePlans);
    }

    private Map<String, BigDecimal> getConsumptionsForPricePlans(String smartMeterId) {
        Optional<Map<String, BigDecimal>> consumptionsForPricePlans =
                pricePlanService.getAllPricePlanCostsForMeter(smartMeterId);

        if (!consumptionsForPricePlans.isPresent()) {
            throw new NoConsumptionException("Could not find any usage on the price plan");
        }
        return consumptionsForPricePlans.get();
    }

    private List<Map.Entry<String, BigDecimal>> extractCheapestPricePlans(Map<String, BigDecimal> consumptionsForPricePlans, Integer limit) {
        List<Map.Entry<String, BigDecimal>> pricePlans = new ArrayList<>(consumptionsForPricePlans.entrySet());
        pricePlans.sort(Comparator.comparing(Map.Entry::getValue));

        if (limit != null && limit < pricePlans.size()) {
            pricePlans = pricePlans.subList(0, limit);
        }
        return pricePlans;
    }
}