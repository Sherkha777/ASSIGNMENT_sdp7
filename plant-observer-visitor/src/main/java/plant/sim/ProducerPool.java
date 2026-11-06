package plant.sim;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class ProducerPool implements AutoCloseable {

    private static final long SHUTDOWN_SECONDS = 10;

    private final int threads;
    private final ExecutorService executor;

    public ProducerPool(int threads) {
        if (threads < 1) {
            throw new IllegalArgumentException("threads must be at least 1");
        }
        this.threads = threads;
        AtomicInteger counter = new AtomicInteger();
        this.executor = Executors.newFixedThreadPool(threads, task -> {
            Thread thread = new Thread(task, "producer-" + counter.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        });
    }

    public int threads() {
        return threads;
    }

    public void runAll(List<Runnable> producers) throws InterruptedException {
        List<Future<?>> running = new ArrayList<>();
        for (Runnable producer : producers) {
            running.add(executor.submit(producer));
        }
        for (Future<?> future : running) {
            try {
                future.get();
            } catch (ExecutionException failure) {
                throw new IllegalStateException("producer failed", failure.getCause());
            }
        }
    }

    public boolean isTerminated() {
        return executor.isTerminated();
    }

    @Override
    public void close() throws InterruptedException {
        executor.shutdown();
        if (!executor.awaitTermination(SHUTDOWN_SECONDS, TimeUnit.SECONDS)) {
            executor.shutdownNow();
            throw new IllegalStateException("producers did not finish within " + SHUTDOWN_SECONDS + " seconds");
        }
    }
}
