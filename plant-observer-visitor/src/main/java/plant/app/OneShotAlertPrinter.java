package plant.app;

import plant.event.AlertRaised;
import plant.event.PlantEvent;
import plant.observer.EventType;
import plant.observer.Observer;
import plant.observer.Subject;

import java.io.PrintStream;

public class OneShotAlertPrinter implements Observer<PlantEvent> {

    private final Subject<PlantEvent> subject;
    private final PrintStream out;

    public OneShotAlertPrinter(Subject<PlantEvent> subject, PrintStream out) {
        this.subject = subject;
        this.out = out;
    }

    public void attach() {
        subject.subscribe(EventType.ALERT, this);
    }

    @Override
    public void onEvent(PlantEvent event) {
        AlertRaised alert = (AlertRaised) event;
        out.println("[printer] alert on " + alert.machineId() + ": " + alert.reason() + " (printer leaves now)");
        subject.unsubscribe(EventType.ALERT, this);
    }
}
