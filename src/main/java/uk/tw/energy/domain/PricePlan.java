package uk.tw.energy.domain;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

public class PricePlan {

    private final String energySupplier;
    private final String planName;
    private final BigDecimal unitPricePerKWh;
    private final PricePlanType pricePlanType;

    public PricePlan(String planName, String energySupplier, BigDecimal unitRate, PricePlanType pricePlanType) {
        this.planName = planName;
        this.energySupplier = energySupplier;
        this.unitPricePerKWh = unitRate;
        this.pricePlanType = pricePlanType;
    }

    public String getEnergySupplier() {
        return energySupplier;
    }

    public String getPlanName() {
        return planName;
    }

    public BigDecimal getUnitPricePerKWh() {
        return unitPricePerKWh;
    }

    public PricePlanType getPricePlanType() {
        return pricePlanType;
    }
}
