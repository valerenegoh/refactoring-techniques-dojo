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

    public Optional<Map<String, BigDecimal>> getAllPricePlanCostsFoMeter(String smartMeterId) {
        Optional<List<ElectricityReading>> electricityReadings = meterService.getReadings(smartMeterId);

        //Check if any readings
        if (!electricityReadings.isPresent()) {
            return Optional.empty();
        }

        //Find all price plans given a list of readings
        Optional<Map<String, BigDecimal>> allPricePlansForMeter = Optional.of(

            //look at each price plan
            pricePlans.stream().collect(
                Collectors.toMap(PricePlan::getPlanName, t -> {

                    //find the average kW unit reading for plan
                    List<ElectricityReading> er = electricityReadings.get();
                    BigDecimal summedReadings = er.stream()
                        .map(ElectricityReading::getReading)
                        .reduce(BigDecimal.ZERO, (reading, accumulator) -> reading.add(accumulator));
                    BigDecimal av = summedReadings.divide(BigDecimal.valueOf(er.size()), RoundingMode.HALF_UP);

                    //find first and last readings
                    ElectricityReading first = er.stream()
                        .min(Comparator.comparing(ElectricityReading::getTime))
                        .get();
                    ElectricityReading last = er.stream()
                        .max(Comparator.comparing(ElectricityReading::getTime))
                        .get();

                    //calculate kW average usage over usage time
                    BigDecimal elapsedTime = BigDecimal.valueOf(Duration.between(first.getTime(), last.getTime()).getSeconds() / 3600.0);
                    BigDecimal unitsUsedInPeriod = av.multiply(elapsedTime);

                    BigDecimal multiplier = null;
                    // switch on price plan name
                    switch (t.getPricePlanType()) {
                        case (STANDARD_PRICE_PLAN):
                            // No need to multiply
                            multiplier = BigDecimal.ONE;
                            break;
                        case (ECO_PRICE_PLAN):
                            // Half price
                            multiplier = BigDecimal.valueOf(0.5);
                            break;
                        case (PREMIUM_PRICE_PLAN):
                            // Multiply by 1.5
                            multiplier = BigDecimal.valueOf(2);
                            break;
                    }

                    // return total cost for price plan
                    return unitsUsedInPeriod.multiply(t.getUnitRate().multiply(multiplier));
                })));

        return allPricePlansForMeter;
    }

}
