package uk.tw.energy.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.tw.energy.controller.exceptions.NoConsumptionException;
import uk.tw.energy.service.AccountService;
import uk.tw.energy.service.PricePlanService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class PricePlanComparatorControllerTest {

    private static final String PRICE_PLAN_1_ID = "test-supplier";
    private static final String PRICE_PLAN_2_ID = "best-supplier";
    private static final String PRICE_PLAN_3_ID = "second-best-supplier";
    private static final String SMART_METER_ID = "smart-meter-id";

    @InjectMocks
    private PricePlanComparatorController controller;

    @Mock
    private PricePlanService pricePlanService;

    @Mock
    private AccountService accountService;

    @Test
    public void shouldCalculateCostForMeterReadingsForEveryPricePlan() {

        Map<String, BigDecimal> pricePlanComparisons = createPricePlanCostComparisons();
        when(accountService.getPricePlanIdForSmartMeterId(SMART_METER_ID)).thenReturn(PRICE_PLAN_1_ID);
        when(pricePlanService.getAllPricePlanCostsFoMeter(SMART_METER_ID)).thenReturn(Optional.of(pricePlanComparisons));

        Map<String, Object> responseBody = controller.calculatedCostForEachPricePlan(SMART_METER_ID).getBody();
        assertThat(responseBody.get(PricePlanComparatorController.PRICE_PLAN_ID_KEY), is(PRICE_PLAN_1_ID));
        assertThat(responseBody.get(PricePlanComparatorController.PRICE_PLAN_COMPARISONS_KEY), is(pricePlanComparisons));
    }

    @Test
    public void shouldRecommendCheapestPricePlansInOrderNoLimitForMeterUsage() {

        Map<String, BigDecimal> pricePlanComparisons = createPricePlanCostComparisons();
        when(pricePlanService.getAllPricePlanCostsFoMeter(SMART_METER_ID)).thenReturn(Optional.of(pricePlanComparisons));

        List<Map.Entry<String, BigDecimal>> responseBody = controller.recommendCheapestPricePlans(SMART_METER_ID, null).getBody();

        assertThat(responseBody.size(), is(3));
        assertThat(responseBody.get(0).getKey(), is(PRICE_PLAN_2_ID));
        assertThat(responseBody.get(0).getValue(), is(BigDecimal.valueOf(10.0)));
        assertThat(responseBody.get(1).getKey(), is(PRICE_PLAN_3_ID));
        assertThat(responseBody.get(1).getValue(), is(BigDecimal.valueOf(20.0)));
        assertThat(responseBody.get(2).getKey(), is(PRICE_PLAN_1_ID));
        assertThat(responseBody.get(2).getValue(), is(BigDecimal.valueOf(100.0)));
    }

    @Test
    public void shouldRecommendLimitedCheapestPricePlansInOrderForMeterUsage() {

        Map<String, BigDecimal> pricePlanComparisons = createPricePlanCostComparisons();
        when(pricePlanService.getAllPricePlanCostsFoMeter(SMART_METER_ID)).thenReturn(Optional.of(pricePlanComparisons));

        List<Map.Entry<String, BigDecimal>> responseBody = controller.recommendCheapestPricePlans(SMART_METER_ID, 2).getBody();

        assertThat(responseBody.size(), is(2));
        assertThat(responseBody.get(0).getKey(), is(PRICE_PLAN_2_ID));
        assertThat(responseBody.get(0).getValue(), is(BigDecimal.valueOf(10.0)));
        assertThat(responseBody.get(1).getKey(), is(PRICE_PLAN_3_ID));
        assertThat(responseBody.get(1).getValue(), is(BigDecimal.valueOf(20.0)));
    }

    @Test
    public void shouldRecommendAllPricePlansInOrderWhenFewerPricePlansThanLimit() {

        Map<String, BigDecimal> pricePlanCostComparisons = createPricePlanCostComparisons();
        when(pricePlanService.getAllPricePlanCostsFoMeter(SMART_METER_ID)).thenReturn(Optional.of(pricePlanCostComparisons));

        List<Map.Entry<String, BigDecimal>> responseBody = controller.recommendCheapestPricePlans(SMART_METER_ID, 5).getBody();

        assertThat(responseBody.size(), is(3));
        assertThat(responseBody.get(0).getKey(), is(PRICE_PLAN_2_ID));
        assertThat(responseBody.get(0).getValue(), is(BigDecimal.valueOf(10.0)));
        assertThat(responseBody.get(1).getKey(), is(PRICE_PLAN_3_ID));
        assertThat(responseBody.get(1).getValue(), is(BigDecimal.valueOf(20.0)));
        assertThat(responseBody.get(2).getKey(), is(PRICE_PLAN_1_ID));
        assertThat(responseBody.get(2).getValue(), is(BigDecimal.valueOf(100.0)));
    }

    @Test
    public void givenNoMatchingMeterIdShouldReturnNotFound() {
        when(pricePlanService.getAllPricePlanCostsFoMeter(SMART_METER_ID)).thenReturn(Optional.empty());
        assertThrows(NoConsumptionException.class, () -> controller.calculatedCostForEachPricePlan("not-found"));
    }

    private Map<String, BigDecimal> createPricePlanCostComparisons() {
        return Map.ofEntries(
                entry(PRICE_PLAN_1_ID, BigDecimal.valueOf(100.0)),
                entry(PRICE_PLAN_2_ID, BigDecimal.valueOf(10.0)),
                entry(PRICE_PLAN_3_ID, BigDecimal.valueOf(20.0)));
    }
}
