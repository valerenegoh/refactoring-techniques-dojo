package uk.tw.energy.service;

import org.springframework.stereotype.Service;
import uk.tw.energy.domain.ElectricityReading;
import uk.tw.energy.domain.PricePlan;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PricePlanService {

    private final List<PricePlan> pricePlans;
    private final MeterService meterService;

    public static final String STANDARD_PRICE_PLAN = "Standard";
    public static final String PREMIUM_PRICE_PLAN = "Premium";
    public static final String ECO_PRICE_PLAN = "Eco";

    public PricePlanService(List<PricePlan> pricePlans, MeterService meterService) {
        this.pricePlans = pricePlans;
        this.meterService = meterService;
    }

    public Optional<Map<String, BigDecimal>> getAllPricePlanCostsForMeter(String smartMeterId) {
        Optional<List<ElectricityReading>> electricityReadings = meterService.getReadings(smartMeterId);

        if (!electricityReadings.isPresent()) {
            return Optional.empty();
        }
        List<ElectricityReading> er = electricityReadings.get();

        Optional<Map<String, BigDecimal>> allPricePlansForMeter = Optional.of(

            pricePlans.stream().collect(
                Collectors.toMap(PricePlan::getPlanName, pricePlan -> {

                    BigDecimal av = getAveReadingForPlanInKW(er);
                    BigDecimal unitsUsedInPeriod = getAveUsageOverTimeInKW(er, av);
                    BigDecimal unitPrice = pricePlan.getUnitPricePerKWh();
                    BigDecimal multiplier = getMultiplier(pricePlan.getPricePlanType());

                    return totalCostForPricePlan(unitsUsedInPeriod, unitPrice, multiplier);
                })));

        return allPricePlansForMeter;
    }

    private BigDecimal getAveReadingForPlanInKW(List<ElectricityReading> er) {
        BigDecimal summedReadings = er.stream()
                .map(ElectricityReading::getReadingInKW)
                .reduce(BigDecimal.ZERO, (reading, accumulator) -> reading.add(accumulator));
        return summedReadings.divide(BigDecimal.valueOf(er.size()), RoundingMode.HALF_UP);
    }

    private BigDecimal getAveUsageOverTimeInKW(List<ElectricityReading> er, BigDecimal av) {
        ElectricityReading first = er.stream()
                .min(Comparator.comparing(ElectricityReading::getTime))
                .get();
        ElectricityReading last = er.stream()
                .max(Comparator.comparing(ElectricityReading::getTime))
                .get();

        BigDecimal elapsedTime = BigDecimal.valueOf(Duration.between(first.getTime(), last.getTime()).getSeconds() / 3600.0);
        return av.multiply(elapsedTime);
    }

    private BigDecimal getMultiplier(String pricePlanType) {
        switch (pricePlanType) {
            case STANDARD_PRICE_PLAN: return BigDecimal.ONE;
            case ECO_PRICE_PLAN: return BigDecimal.valueOf(0.5);
            case PREMIUM_PRICE_PLAN: return BigDecimal.valueOf(2);
        }
        return null;
    }

    private BigDecimal totalCostForPricePlan(BigDecimal unitsUsedInPeriod, BigDecimal unitPrice, BigDecimal multiplier) {
        return unitsUsedInPeriod.multiply(unitPrice.multiply(multiplier));
    }
}
