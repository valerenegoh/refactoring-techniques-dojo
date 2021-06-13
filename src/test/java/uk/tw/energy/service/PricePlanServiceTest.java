package uk.tw.energy.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.tw.energy.domain.ElectricityReading;
import uk.tw.energy.domain.PricePlan;
import uk.tw.energy.domain.PricePlanType;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class PricePlanServiceTest {

    private static final String ENERGY_SUPPLIER_1 = "JOI";
    private static final String ENERGY_SUPPLIER_2 = "JOI_ECO";
    private static final String ENERGY_SUPPLIER_3 = "JOI_RIPOFF";
    private static final String SMART_METER_ID = "meterID";

    private PricePlanService pricePlanService;

    @Mock
    private MeterService meterService;

    @BeforeEach
    public void setUp() {
        PricePlan pricePlan1 = new PricePlan(ENERGY_SUPPLIER_1, ENERGY_SUPPLIER_1, BigDecimal.valueOf(100), PricePlanType.STANDARD_PRICE_PLAN);
        PricePlan pricePlan2 = new PricePlan(ENERGY_SUPPLIER_2, ENERGY_SUPPLIER_2, BigDecimal.valueOf(100), PricePlanType.ECO_PRICE_PLAN);
        PricePlan pricePlan3 = new PricePlan(ENERGY_SUPPLIER_3, ENERGY_SUPPLIER_3, BigDecimal.valueOf(100), PricePlanType.PREMIUM_PRICE_PLAN);
        pricePlanService = new PricePlanService(List.of(pricePlan1, pricePlan2, pricePlan3), meterService);
    }

    @Test
    public void shouldGetAllStandardPricePlanCostsGivenMeterReadingsForAMeterId() {

        when(meterService.getReadings(SMART_METER_ID)).thenReturn(Optional.of(List.of(
                new ElectricityReading(Instant.now().minus(Duration.ofHours(2)), BigDecimal.valueOf(15.0)),
                new ElectricityReading(Instant.now(), BigDecimal.valueOf(5.0)))));

        Map<String, BigDecimal> pricePlanCostsForReadings = pricePlanService.getAllPricePlanCostsForMeter(SMART_METER_ID).get();

        assertThat(pricePlanCostsForReadings.size(), is(3));
        assertThat(pricePlanCostsForReadings.get(ENERGY_SUPPLIER_1), comparesEqualTo(BigDecimal.valueOf(2000.0)));
        assertThat(pricePlanCostsForReadings.get(ENERGY_SUPPLIER_2), comparesEqualTo(BigDecimal.valueOf(1000.0)));
        assertThat(pricePlanCostsForReadings.get(ENERGY_SUPPLIER_3), comparesEqualTo(BigDecimal.valueOf(4000.0)));
    }

    @Test
    public void shouldReturnEmptyWhenNoReadingsForMeterId() {
        when(meterService.getReadings(SMART_METER_ID)).thenReturn(Optional.empty());
        assertThat(pricePlanService.getAllPricePlanCostsForMeter(SMART_METER_ID), is(Optional.empty()));
    }
}