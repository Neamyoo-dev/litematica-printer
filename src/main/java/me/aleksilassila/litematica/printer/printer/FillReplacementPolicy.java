package me.aleksilassila.litematica.printer.printer;

/** Decides whether a fill target can be placed directly or must be cleared first. */
public final class FillReplacementPolicy {
    private FillReplacementPolicy() {}

    public enum Operation {
        KEEP, PLACE, DESTROY
    }

    public static Operation operation(boolean destroyEnabled, boolean matchesFillBlock,
                                      boolean directlyReplaceable, boolean legacyFillTarget) {
        if (!destroyEnabled) {
            return legacyFillTarget ? Operation.PLACE : Operation.KEEP;
        }
        // Preserve completed targets even when they also match the replaceable list.
        if (matchesFillBlock) return Operation.KEEP;
        return directlyReplaceable ? Operation.PLACE : Operation.DESTROY;
    }
}
