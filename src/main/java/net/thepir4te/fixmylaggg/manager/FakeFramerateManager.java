package net.thepir4te.fixmylaggg.manager;

import java.util.concurrent.ThreadLocalRandom;

public class FakeFramerateManager {
    private static FakeFramerateManager INSTANCE;

    private int targetFakeFramerate = 500;
    private boolean realisticSinkEnabled = true;
    private float realisticSinkThreshold = 0.85f;

    private int lastRealFps = 60;
    private int lastFakeFps = 500;
    private float peakRealFps = 60f;

    public static synchronized FakeFramerateManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FakeFramerateManager();
        }
        return INSTANCE;
    }

    public int calculateFakeFps(int realFps) {
        this.lastRealFps = realFps;

        peakRealFps *= 0.999f;
        if (realFps > peakRealFps) {
            peakRealFps = realFps;
        }
        if (peakRealFps < 10f) peakRealFps = 60f;

        if (!realisticSinkEnabled) {
            this.lastFakeFps = targetFakeFramerate;
            return targetFakeFramerate;
        }

        float ratio = Math.min(realFps / peakRealFps, 1.0f);

        int fake;
        if (ratio > realisticSinkThreshold) {
            fake = targetFakeFramerate;
        } else {
            float exaggerated = (float) Math.pow(ratio, 1.5f);
            fake = (int) (targetFakeFramerate * exaggerated);
            if (fake < 1) fake = 1;
        }

        ThreadLocalRandom rng = ThreadLocalRandom.current();
        float jitter = 1.0f + (rng.nextFloat() - 0.5f) * 0.02f;
        fake = (int) (fake * jitter);
        if (fake < 1) fake = 1;
        if (fake > targetFakeFramerate) fake = targetFakeFramerate;

        this.lastFakeFps = fake;
        return fake;
    }

    public int getCurrentFakeFps() {
        return lastFakeFps;
    }

    public int getLastRealFps() {
        return lastRealFps;
    }

    public int getRealFps() {
        return lastRealFps;
    }

    public float getPeakRealFps() {
        return peakRealFps;
    }

    public int getTargetFramerate() {
        return targetFakeFramerate;
    }

    public void setTargetFramerate(int fps) {
        if (fps >= 1 && fps <= 12000) {
            this.targetFakeFramerate = fps;
        }
    }

    public boolean isRealisticSinkEnabled() {
        return realisticSinkEnabled;
    }

    public void setRealisticSinkEnabled(boolean enabled) {
        this.realisticSinkEnabled = enabled;
    }

    public float getRealisticSinkThreshold() {
        return realisticSinkThreshold;
    }

    public void setRealisticSinkThreshold(float threshold) {
        if (threshold >= 0.0f && threshold <= 1.0f) {
            this.realisticSinkThreshold = threshold;
        }
    }
}
