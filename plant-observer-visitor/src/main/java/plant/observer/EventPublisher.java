package plant.observer;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventPublisher<E extends Event> implements Subject<E> {

    private final Map<EventType, CopyOnWriteArrayList<Observer<E>>> observers = new EnumMap<>(EventType.class);

    public EventPublisher() {
        for (EventType type : EventType.values()) {
            observers.put(type, new CopyOnWriteArrayList<>());
        }
    }

    @Override
    public void subscribe(EventType type, Observer<E> observer) {
        Objects.requireNonNull(observer, "observer");
        observers.get(type).addIfAbsent(observer);
    }

    @Override
    public void unsubscribe(EventType type, Observer<E> observer) {
        observers.get(type).remove(observer);
    }

    public int subscriberCount(EventType type) {
        return observers.get(type).size();
    }

    protected final void publish(E event) {
        RuntimeException firstFailure = null;
        for (Observer<E> observer : observers.get(event.type())) {
            try {
                observer.onEvent(event);
            } catch (RuntimeException failure) {
                if (firstFailure == null) {
                    firstFailure = failure;
                } else {
                    firstFailure.addSuppressed(failure);
                }
            }
        }
        if (firstFailure != null) {
            throw firstFailure;
        }
    }
}
