package plant.event;

final class Require {

    private Require() {
    }

    static String text(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }

    static double nonNegative(double value, String field) {
        if (value < 0 || Double.isNaN(value)) {
            throw new IllegalArgumentException(field + " must not be negative");
        }
        return value;
    }

    static long nonNegative(long value, String field) {
        if (value < 0) {
            throw new IllegalArgumentException(field + " must not be negative");
        }
        return value;
    }
}
