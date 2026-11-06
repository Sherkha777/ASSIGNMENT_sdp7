package plant.app;

import plant.listener.AlertService;
import plant.listener.EmergencyStopGuard;
import plant.listener.LineAggregator;
import plant.listener.OeeTracker;
import plant.listener.PredictiveMaintenance;
import plant.listener.ScadaDashboard;
import plant.model.Factory;
import plant.model.PlantIndex;
import plant.observer.EventType;
import plant.sim.ProducerPool;
import plant.source.ProductionCounterSource;
import plant.source.TelemetrySource;
import plant.state.StatusBoard;

public final class PlantSystem implements AutoCloseable {

    private final Factory factory;
    private final PlantIndex index;
    private final StatusBoard board;
    private final TelemetrySource telemetry;
    private final ProductionCounterSource counters;
    private final ScadaDashboard dashboard;
    private final PredictiveMaintenance predictive;
    private final EmergencyStopGuard guard;
    private final OeeTracker oee;
    private final LineAggregator aggregator;
    private final AlertService alerts;
    private final ProducerPool pool;

    public PlantSystem(Factory factory, PlantConfig config) {
        this.factory = factory;
        this.index = new PlantIndex(factory);
        this.board = new StatusBoard();
        this.telemetry = new TelemetrySource();
        this.counters = new ProductionCounterSource();
        this.dashboard = new ScadaDashboard();
        this.predictive = new PredictiveMaintenance(board, config.vibrationLimitMmS());
        this.guard = new EmergencyStopGuard(board, config.temperatureLimitC());
        this.oee = new OeeTracker(config.idealUnitsPerMinute());
        this.aggregator = new LineAggregator(index);
        this.alerts = new AlertService(index, board);
        this.pool = new ProducerPool(config.producerThreads());
        wire();
    }

    private void wire() {
        telemetry.subscribe(EventType.TELEMETRY, dashboard);
        telemetry.subscribe(EventType.TELEMETRY, predictive);
        telemetry.subscribe(EventType.TELEMETRY, guard);
        counters.subscribe(EventType.PRODUCTION, dashboard);
        counters.subscribe(EventType.PRODUCTION, oee);
        counters.subscribe(EventType.PRODUCTION, aggregator);
        aggregator.subscribe(EventType.LINE_SUMMARY, dashboard);
        predictive.subscribe(EventType.ALERT, dashboard);
        predictive.subscribe(EventType.ALERT, alerts);
        guard.subscribe(EventType.EMERGENCY_STOP, dashboard);
        alerts.subscribe(EventType.MAINTENANCE_TICKET, dashboard);
    }

    public Factory factory() {
        return factory;
    }

    public PlantIndex index() {
        return index;
    }

    public StatusBoard board() {
        return board;
    }

    public TelemetrySource telemetry() {
        return telemetry;
    }

    public ProductionCounterSource counters() {
        return counters;
    }

    public ScadaDashboard dashboard() {
        return dashboard;
    }

    public PredictiveMaintenance predictive() {
        return predictive;
    }

    public EmergencyStopGuard guard() {
        return guard;
    }

    public OeeTracker oee() {
        return oee;
    }

    public LineAggregator aggregator() {
        return aggregator;
    }

    public AlertService alerts() {
        return alerts;
    }

    public ProducerPool pool() {
        return pool;
    }

    @Override
    public void close() throws InterruptedException {
        pool.close();
    }
}
