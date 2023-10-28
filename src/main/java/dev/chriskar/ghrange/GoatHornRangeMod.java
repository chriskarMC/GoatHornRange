package dev.chriskar.ghrange;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GoatHornRangeMod implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("goat-horn-range");

    @Override
    public void onInitialize() {
        LOGGER.info("[GoatHornRange] is active!");
    }
}
