package net.thepir4te.fixmylaggg;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FIXMYLAGGG implements ModInitializer {
	public static final String MOD_ID = "fix-my-laggg";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Fix My Laggg initialized! Your FPS are about to look AMAZING. (not really)");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
