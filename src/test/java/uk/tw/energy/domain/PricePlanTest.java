package uk.tw.energy.domain;

import org.junit.jupiter.api.Test;
import uk.tw.energy.service.PricePlanService;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class PricePlanTest {

    @Test
    public void shouldReturnTheEnergySupplierGivenInTheConstructor() {
        String energySupplierName = "Energy Supplier Name";
        PricePlan pricePlan = new PricePlan(null, energySupplierName, null, PricePlanService.STANDARD_PRICE_PLAN);

        assertThat(pricePlan.getEnergySupplier(), is(energySupplierName));
    }
}
