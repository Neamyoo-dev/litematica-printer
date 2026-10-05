package me.aleksilassila.litematica.printer.utils;

import fi.dy.masa.litematica.util.EasyPlaceUtils;

import java.util.function.Supplier;

public final class EasyPlaceCompatibility {
    private EasyPlaceCompatibility() {}

    public static <T> T runPrinterPlacement(Supplier<T> placement) {
        boolean wasHandling = EasyPlaceUtils.isHandling();
        // Keep Litematica's manual Easy Place hook from redirecting the printer's target.
        EasyPlaceUtils.setHandling(true);
        try {
            return placement.get();
        } finally {
            EasyPlaceUtils.setHandling(wasHandling);
        }
    }
}
