package uk.tw.energy.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.tw.energy.builders.MeterReadingsBuilder;
import uk.tw.energy.domain.Meter;
import uk.tw.energy.service.MeterService;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class MeterReadingControllerTest {

    private static final String SMART_METER_ID = "10101010";

    @Mock
    private MeterService meterService;

    @InjectMocks
    private MeterReadingController meterReadingController;

    @Test
    public void givenNoMeterIdIsSuppliedWhenStoringShouldReturnErrorResponse() {
        Meter meter = new Meter(null, Collections.emptyList());
        assertThat(meterReadingController.storeReadings(meter).getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Test
    public void givenEmptyMeterReadingShouldReturnErrorResponse() {
        Meter meter = new Meter(SMART_METER_ID, Collections.emptyList());
        assertThat(meterReadingController.storeReadings(meter).getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Test
    public void givenNullReadingsAreSuppliedWhenStoringShouldReturnErrorResponse() {
        Meter meter = new Meter(SMART_METER_ID, null);
        assertThat(meterReadingController.storeReadings(meter).getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Test
    public void givenValidMeterReadingsShouldStore() {
        Meter meter = new MeterReadingsBuilder().setSmartMeterId(SMART_METER_ID)
                .generateElectricityReadings()
                .build();

        meterReadingController.storeReadings(meter);

        verify(meterService).storeReadings(SMART_METER_ID, meter.getElectricityReadings());
    }

    @Test
    public void shouldReturnMeterReadingsForSmartMeterWithReadings() {
        Meter meter = new MeterReadingsBuilder().setSmartMeterId(SMART_METER_ID)
                .generateElectricityReadings()
                .build();

        when(meterService.getReadings(SMART_METER_ID)).thenReturn(Optional.of(meter.getElectricityReadings()));

        assertThat(meterReadingController.readReadings(SMART_METER_ID).getStatusCode(), is(HttpStatus.OK));
        assertThat(meterReadingController.readReadings(SMART_METER_ID).getBody(), is(meter.getElectricityReadings()));
    }

    @Test
    public void givenMeterIdThatIsNotRecognisedShouldReturnNotFound() {
        String unknownSmartMeterId = "UNKNOWN";
        when(meterService.getReadings(unknownSmartMeterId)).thenReturn(Optional.empty());
        assertThat(meterReadingController.readReadings(SMART_METER_ID).getStatusCode(), is(HttpStatus.NOT_FOUND));
    }
}
