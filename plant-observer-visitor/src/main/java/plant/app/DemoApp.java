package plant.app;

import plant.event.TelemetryReading;
import plant.model.PlantElement;
import plant.observer.EventType;
import plant.sim.LoadGenerator;
import plant.sim.ProducerPool;
import plant.visitor.DowntimeCostVisitor;
import plant.visitor.EnergyVisitor;
import plant.visitor.FaultyElementsFilter;
import plant.visitor.InventoryVisitor;
import plant.visitor.JsonExportVisitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;

public class DemoApp {

    private final PlantSystem system;
    private final BufferedReader in;
    private final PrintStream out;

    public DemoApp(PlantSystem system, BufferedReader in, PrintStream out) {
        this.system = system;
        this.in = in;
        this.out = out;
    }

    public void run() throws IOException, InterruptedException {
        boolean running = true;
        while (running) {
            printMenu();
            String choice = prompt("> ");
            if (choice == null) {
                break;
            }
            switch (choice.trim()) {
                case "1" -> simulate();
                case "2" -> showDashboard();
                case "3" -> injectVibration();
                case "4" -> injectHeat();
                case "5" -> runVisitors();
                case "6" -> exportJson();
                case "7" -> attachPrinter();
                case "0" -> running = false;
                default -> out.println("unknown option");
            }
        }
        out.println("console closed");
    }

    private void printMenu() {
        out.println();
        out.println("=== Plant monitor ===");
        out.println("1  run simulation (producer threads x events each)");
        out.println("2  show dashboard");
        out.println("3  inject vibration reading (alert -> inspection visitor -> ticket)");
        out.println("4  inject overheating reading (emergency stop)");
        out.println("5  run visitors on the factory or one station");
        out.println("6  export factory or one station as JSON");
        out.println("7  attach a one-shot alert printer (leaves from inside its handler)");
        out.println("0  exit");
    }

    private void simulate() throws IOException, InterruptedException {
        int threads = Integer.parseInt(prompt("producer threads: ").trim());
        int events = Integer.parseInt(prompt("events per thread: ").trim());
        LoadGenerator generator = new LoadGenerator(system.index(), system.telemetry(), system.counters());
        long started = System.nanoTime();
        try (ProducerPool pool = new ProducerPool(threads)) {
            pool.runAll(generator.calmProducers(threads, events));
        }
        long millis = (System.nanoTime() - started) / 1_000_000;
        out.println("sent " + (long) threads * events + " telemetry and " + (long) threads * events
                + " production events with " + threads + " threads in " + millis + " ms");
        showDashboard();
    }

    private void showDashboard() {
        for (EventType type : EventType.values()) {
            out.println(type + ": " + system.dashboard().count(type));
        }
        system.dashboard().tickets().forEach(t -> out.println("ticket: station " + t.stationId()
                + " faulty " + t.faultyElementIds() + " downtime cost/h " + t.downtimeCostPerHour()));
        for (String lineId : new String[]{"L1", "L2"}) {
            system.dashboard().lineSummary(lineId).ifPresent(s -> out.println("line " + lineId
                    + ": good " + s.good() + ", rejected " + s.rejected()));
        }
    }

    private void injectVibration() throws IOException {
        String stationId = prompt("station id (S1-S4): ").trim();
        String machineId = prompt("machine id (e.g. R1): ").trim();
        double vibration = Double.parseDouble(prompt("vibration mm/s: ").trim());
        system.telemetry().emit(new TelemetryReading(stationId, machineId, 70.0, vibration));
        showDashboard();
    }

    private void injectHeat() throws IOException {
        String stationId = prompt("station id (S1-S4): ").trim();
        String machineId = prompt("machine id (e.g. P1): ").trim();
        double temperature = Double.parseDouble(prompt("temperature C: ").trim());
        system.telemetry().emit(new TelemetryReading(stationId, machineId, temperature, 1.0));
        showDashboard();
    }

    private void runVisitors() throws IOException {
        PlantElement element = pick();
        out.println("energy kW:          " + element.accept(new EnergyVisitor()));
        out.println("downtime cost/h:    " + element.accept(new DowntimeCostVisitor()));
        out.println("inventory:          " + element.accept(new InventoryVisitor()));
        out.println("faulty elements:    " + element.accept(new FaultyElementsFilter(system.board())).stream()
                .map(PlantElement::id).toList());
    }

    private void exportJson() throws IOException {
        out.println(pick().accept(new JsonExportVisitor()));
    }

    private void attachPrinter() {
        new OneShotAlertPrinter(system.predictive(), out).attach();
        out.println("printer attached; it prints the next alert and unsubscribes itself");
    }

    private PlantElement pick() throws IOException {
        String id = prompt("element id (" + system.factory().id() + " or S1-S4): ").trim();
        return id.equals(system.factory().id()) ? system.factory() : system.index().station(id);
    }

    private String prompt(String label) throws IOException {
        out.print(label);
        out.flush();
        return in.readLine();
    }
}
