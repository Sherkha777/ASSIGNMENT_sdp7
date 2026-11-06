package plant.sim;

import org.junit.jupiter.api.Test;
import plant.app.PlantConfig;
import plant.app.PlantSystem;
import plant.app.SamplePlant;
import plant.event.TelemetryReading;
import plant.observer.EventType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConcurrencyTest {

    private static final int THREADS = 4;
    private static final int EVENTS = 1000;

    @Test
    void fourThreadsTimesOneThousandEventsLoseNothing() throws Exception {
        PlantSystem system = new PlantSystem(SamplePlant.build(), new PlantConfig(THREADS, 7.0, 120.0, 2.5));
        LoadGenerator generator = new LoadGenerator(system.index(), system.telemetry(), system.counters());

        system.pool().runAll(generator.calmProducers(THREADS, EVENTS));
        system.close();

        long expected = (long) THREADS * EVENTS;
        assertEquals(expected, system.dashboard().count(EventType.TELEMETRY));
        assertEquals(expected, system.dashboard().count(EventType.PRODUCTION));
        assertEquals(expected, system.dashboard().count(EventType.LINE_SUMMARY));
        assertEquals(0, system.dashboard().count(EventType.ALERT));
        for (String stationId : system.index().stationIds()) {
            assertEquals(9000, system.oee().goodUnits(stationId));
        }
        assertEquals(18000, system.dashboard().lineSummary("L1").orElseThrow().good());
        assertEquals(2000, system.dashboard().lineSummary("L2").orElseThrow().rejected());
        assertTrue(system.pool().isTerminated());
    }

    @Test
    void manyThreadsHammeringOneFaultyMachineRaiseExactlyOneAlertAndOneTicket() throws Exception {
        PlantSystem system = new PlantSystem(SamplePlant.build(), new PlantConfig(THREADS, 7.0, 120.0, 2.5));
        List<Runnable> producers = new ArrayList<>();
        for (int p = 0; p < THREADS; p++) {
            producers.add(() -> {
                for (int i = 0; i < EVENTS; i++) {
                    system.telemetry().emit(new TelemetryReading("S1", "R1", 70, 9.0));
                }
            });
        }

        system.pool().runAll(producers);
        system.close();

        assertEquals(THREADS * EVENTS, system.dashboard().count(EventType.TELEMETRY));
        assertEquals(1, system.dashboard().count(EventType.ALERT));
        assertEquals(1, system.dashboard().count(EventType.MAINTENANCE_TICKET));
    }

    @Test
    void manyThreadsOverheatingOneMachineRaiseExactlyOneEmergencyStop() throws Exception {
        PlantSystem system = new PlantSystem(SamplePlant.build(), new PlantConfig(THREADS, 7.0, 120.0, 2.5));
        List<Runnable> producers = new ArrayList<>();
        for (int p = 0; p < THREADS; p++) {
            producers.add(() -> {
                for (int i = 0; i < EVENTS; i++) {
                    system.telemetry().emit(new TelemetryReading("S2", "P1", 150, 1.0));
                }
            });
        }

        system.pool().runAll(producers);
        system.close();

        assertEquals(1, system.dashboard().count(EventType.EMERGENCY_STOP));
    }

    @Test
    void threadCountIsConfigurableAndProducersReallyRunInParallel() throws Exception {
        try (ProducerPool pool = new ProducerPool(3)) {
            CyclicBarrier meet = new CyclicBarrier(3);
            Set<String> names = ConcurrentHashMap.newKeySet();
            List<Runnable> tasks = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                tasks.add(() -> {
                    try {
                        names.add(Thread.currentThread().getName());
                        meet.await(5, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                });
            }

            pool.runAll(tasks);

            assertEquals(3, pool.threads());
            assertEquals(3, names.size());
        }
    }

    @Test
    void closingThePoolTerminatesItsThreads() throws Exception {
        ProducerPool pool = new ProducerPool(2);
        pool.runAll(List.of(() -> {
        }));

        pool.close();

        assertTrue(pool.isTerminated());
    }

    @Test
    void aFailingProducerIsReportedInsteadOfBeingSwallowed() throws Exception {
        try (ProducerPool pool = new ProducerPool(2)) {
            assertThrows(IllegalStateException.class, () -> pool.runAll(List.of(() -> {
                throw new IllegalArgumentException("bad producer");
            })));
        }
    }

    @Test
    void poolRejectsZeroThreads() {
        assertThrows(IllegalArgumentException.class, () -> new ProducerPool(0));
        assertThrows(IllegalArgumentException.class, () -> new PlantConfig(0, 7, 120, 2.5));
    }
}
