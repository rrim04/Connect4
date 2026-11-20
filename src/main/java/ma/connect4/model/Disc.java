
package ma.connect4.model;

/**
 * Represents the content of a Connect Four cell.
 */
public enum Disc {
    EMPTY,
    RED,
    YELLOW;

    public Disc opponent() {
        return switch (this) {
            case RED -> YELLOW;
            case YELLOW -> RED;
            case EMPTY -> EMPTY;
        };
    }
}
