package plant.observer;

import org.junit.jupiter.api.Test;
import plant.event.AlertRaised;
import plant.event.PlantEvent;
import plant.event.TelemetryReading;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventPublisherTest {

    private static class OpenSubject extends EventPublisher<PlantEvent> {
        void fire(PlantEvent event) {
            publish(event);
        }
    }

    private final OpenSubject subject = new OpenSubject();
    private final TelemetryReading reading = new TelemetryReading("S1", "R1", 60.0, 1.0);
    private final AlertRaised alert = new AlertRaised("S1", "R1", "test");

    @Test
    void listenerReceivesOnlyTheEventTypeItSubscribedTo() {
        List<PlantEvent> received = new ArrayList<>();
        subject.subscribe(EventType.TELEMETRY, received::add);

        subject.fire(reading);
        subject.fire(alert);

        assertEquals(List.of(reading), received);
    }

    @Test
    void unsubscribedListenerGetsNothingMore() {
        AtomicInteger count = new AtomicInteger();
        Observer<PlantEvent> counter = e -> count.incrementAndGet();
        subject.subscribe(EventType.TELEMETRY, counter);
        subject.fire(reading);

        subject.unsubscribe(EventType.TELEMETRY, counter);
        subject.fire(reading);

        assertEquals(1, count.get());
    }

    @Test
    void sameListenerSubscribedTwiceIsNotCalledTwice() {
        AtomicInteger count = new AtomicInteger();
        Observer<PlantEvent> counter = e -> count.incrementAndGet();
        subject.subscribe(EventType.TELEMETRY, counter);
        subject.subscribe(EventType.TELEMETRY, counter);

        subject.fire(reading);

        assertEquals(1, count.get());
        assertEquals(1, subject.subscriberCount(EventType.TELEMETRY));
    }

    @Test
    void listenerCanUnsubscribeItselfInsideItsOwnHandlerWithoutHurtingOthers() {
        AtomicInteger selfRemoving = new AtomicInteger();
        AtomicInteger neighbour = new AtomicInteger();
        subject.subscribe(EventType.TELEMETRY, new Observer<>() {
            @Override
            public void onEvent(PlantEvent event) {
                selfRemoving.incrementAndGet();
                subject.unsubscribe(EventType.TELEMETRY, this);
            }
        });
        subject.subscribe(EventType.TELEMETRY, e -> neighbour.incrementAndGet());

        subject.fire(reading);
        subject.fire(reading);

        assertEquals(1, selfRemoving.get());
        assertEquals(2, neighbour.get());
    }

    @Test
    void listenerSubscribedInsideAHandlerStartsWithTheNextEvent() {
        AtomicInteger late = new AtomicInteger();
        subject.subscribe(EventType.TELEMETRY, e -> subject.subscribe(EventType.TELEMETRY, x -> late.incrementAndGet()));

        subject.fire(reading);
        assertEquals(0, late.get());
        subject.fire(reading);
        assertEquals(1, late.get());
    }

    @Test
    void failingListenerDoesNotStarveTheOthersAndTheFailureIsRethrown() {
        AtomicInteger healthy = new AtomicInteger();
        subject.subscribe(EventType.TELEMETRY, e -> {
            throw new IllegalStateException("boom");
        });
        subject.subscribe(EventType.TELEMETRY, e -> healthy.incrementAndGet());

        assertThrows(IllegalStateException.class, () -> subject.fire(reading));
        assertEquals(1, healthy.get());
    }

    @Test
    void unsubscribingWhileSeveralThreadsPublishLosesNoEventForTheStableListener() throws Exception {
        int threads = 4;
        int perThread = 1000;
        AtomicInteger stable = new AtomicInteger();
        subject.subscribe(EventType.TELEMETRY, e -> stable.incrementAndGet());
        List<Observer<PlantEvent>> leavers = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            Observer<PlantEvent> leaver = e -> {
            };
            leavers.add(leaver);
            subject.subscribe(EventType.TELEMETRY, leaver);
        }

        ExecutorService pool = Executors.newFixedThreadPool(threads + 1);
        CountDownLatch start = new CountDownLatch(1);
        for (int t = 0; t < threads; t++) {
            pool.submit(() -> {
                start.await();
                for (int i = 0; i < perThread; i++) {
                    subject.fire(reading);
                }
                return null;
            });
        }
        pool.submit(() -> {
            start.await();
            leavers.forEach(l -> subject.unsubscribe(EventType.TELEMETRY, l));
            return null;
        });
        start.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));

        assertEquals(threads * perThread, stable.get());
        assertEquals(1, subject.subscriberCount(EventType.TELEMETRY));
    }
}
