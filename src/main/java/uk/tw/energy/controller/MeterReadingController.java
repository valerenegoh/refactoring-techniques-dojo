package uk.tw.energy.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.tw.energy.domain.ElectricityReading;
import uk.tw.energy.domain.Meter;
import uk.tw.energy.service.MeterService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/readings")
public class MeterReadingController {

    private final MeterService meterService;

    public MeterReadingController(MeterService meterService) {
        this.meterService = meterService;
    }

    @PostMapping("/store")
    public ResponseEntity storeReadings(@RequestBody Meter meter) {
        String id = meter.getSmartMeterId();
        List<ElectricityReading> readings = meter.getElectricityReadings();
        if (!(id != null && !id.isEmpty()
                && readings != null && !readings.isEmpty())) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        meterService.storeReadings(meter.getSmartMeterId(), meter.getElectricityReadings());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/read/{smartMeterId}")
    public ResponseEntity readReadings(@PathVariable String smartMeterId) {
        Optional<List<ElectricityReading>> readings = meterService.getReadings(smartMeterId);

        return readings.isPresent()
                ? ResponseEntity.ok(readings.get())
                : ResponseEntity.notFound().build();
    }
}
