package net.thepir4te.fixmylaggg.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.thepir4te.fixmylaggg.config.ModConfig;
import net.thepir4te.fixmylaggg.manager.FakeFramerateManager;

public class FixLagConfigScreen extends Screen {

    private EditBox fpsInput;
    private Button sinkButton;
    private EditBox thresholdInput;

    private boolean sinkEnabled;

    private static final int COL1 = 25;
    private static final int COL2 = 105;
    private static final int INPUT_W = 100;
    private static final int BTN_W = 24;
    private static final int ROW = 22;
    private static final int GAP = 4;

    private static final int WHITE = 0xFFFFFFFF;
    private static final int GRAY = 0xFFAAAAAA;
    private static final int DIM = 0xFF777777;
    private static final int GOLD = 0xFFFFAA00;

    private int startY;
    private int fpsY;
    private int sinkY;
    private int threshY;
    private int saveY;
    private int statusY;

    protected FixLagConfigScreen() {
        super(Component.translatable("gui.fix-my-laggg.title"));
        this.sinkEnabled = FakeFramerateManager.getInstance().isRealisticSinkEnabled();
    }

    @Override
    protected void init() {
        startY = Math.max(30, height / 6);
        fpsY = startY;
        sinkY = fpsY + ROW + GAP + 10;
        threshY = sinkY + ROW + GAP + 10;
        saveY = threshY + ROW + GAP + 14;
        statusY = saveY + ROW + GAP + 14;

        addFpsRow();
        addSinkRow();
        addThresholdRow();
        addActionButtons();
    }

    private void addFpsRow() {
        fpsInput = new EditBox(font, COL2, fpsY, INPUT_W, ROW, Component.translatable("gui.fix-my-laggg.fps"));
        fpsInput.setValue(String.valueOf(FakeFramerateManager.getInstance().getTargetFramerate()));
        addRenderableWidget(fpsInput);

        addRenderableWidget(Button.builder(Component.literal("-"), b -> adjustFps(-50))
            .bounds(COL2 + INPUT_W + 2, fpsY, BTN_W, ROW).build());
        addRenderableWidget(Button.builder(Component.literal("+"), b -> adjustFps(50))
            .bounds(COL2 + INPUT_W + BTN_W + 4, fpsY, BTN_W, ROW).build());
        addRenderableWidget(Button.builder(Component.literal("--"), b -> adjustFps(-500))
            .bounds(COL2 + INPUT_W + BTN_W * 2 + 6, fpsY, BTN_W, ROW).build());
        addRenderableWidget(Button.builder(Component.literal("++"), b -> adjustFps(500))
            .bounds(COL2 + INPUT_W + BTN_W * 3 + 8, fpsY, BTN_W, ROW).build());
    }

    private void addSinkRow() {
        sinkButton = Button.builder(
            Component.translatable(sinkEnabled ? "gui.fix-my-laggg.on" : "gui.fix-my-laggg.off"),
            b -> {
                sinkEnabled = !sinkEnabled;
                b.setMessage(Component.translatable(sinkEnabled ? "gui.fix-my-laggg.on" : "gui.fix-my-laggg.off"));
            }
        ).bounds(COL2, sinkY, 80, ROW).build();
        addRenderableWidget(sinkButton);
    }

    private void addThresholdRow() {
        thresholdInput = new EditBox(font, COL2, threshY, INPUT_W, ROW, Component.translatable("gui.fix-my-laggg.threshold"));
        thresholdInput.setValue(String.valueOf(FakeFramerateManager.getInstance().getRealisticSinkThreshold()));
        addRenderableWidget(thresholdInput);

        addRenderableWidget(Button.builder(Component.literal("-"), b -> adjustThreshold(-0.05f))
            .bounds(COL2 + INPUT_W + 2, threshY, BTN_W, ROW).build());
        addRenderableWidget(Button.builder(Component.literal("+"), b -> adjustThreshold(0.05f))
            .bounds(COL2 + INPUT_W + BTN_W + 4, threshY, BTN_W, ROW).build());
    }

    private void addActionButtons() {
        int cx = width / 2;
        addRenderableWidget(Button.builder(
            Component.translatable("gui.fix-my-laggg.save"), b -> saveAndClose()
        ).bounds(cx - 130, saveY, 120, ROW).build());

        addRenderableWidget(Button.builder(
            Component.translatable("gui.fix-my-laggg.reset"), b -> resetToDefaults()
        ).bounds(cx + 10, saveY, 120, ROW).build());
    }

    private void adjustFps(int delta) {
        int val = parseInt(fpsInput.getValue(), FakeFramerateManager.getInstance().getTargetFramerate());
        val = Math.max(1, Math.min(12000, val + delta));
        fpsInput.setValue(String.valueOf(val));
    }

    private void adjustThreshold(float delta) {
        float val = parseFloat(thresholdInput.getValue(), FakeFramerateManager.getInstance().getRealisticSinkThreshold());
        val = Math.max(0.0f, Math.min(1.0f, val + delta));
        thresholdInput.setValue(String.format("%.2f", val));
    }

    private void resetToDefaults() {
        fpsInput.setValue("500");
        thresholdInput.setValue("0.85");
        sinkEnabled = true;
        sinkButton.setMessage(Component.translatable("gui.fix-my-laggg.on"));
    }

    private void saveAndClose() {
        FakeFramerateManager mgr = FakeFramerateManager.getInstance();
        ModConfig config = ModConfig.getInstance();

        int fps = parseInt(fpsInput.getValue(), 500);
        config.targetFakeFramerate = Math.max(1, Math.min(12000, fps));
        mgr.setTargetFramerate(config.targetFakeFramerate);

        config.realisticSinkEnabled = sinkEnabled;
        mgr.setRealisticSinkEnabled(sinkEnabled);

        float thresh = parseFloat(thresholdInput.getValue(), 0.85f);
        config.realisticSinkThreshold = Math.max(0.0f, Math.min(1.0f, thresh));
        mgr.setRealisticSinkThreshold(config.realisticSinkThreshold);

        config.save();
        Minecraft.getInstance().gui.setScreen(null);
    }

    private static int parseInt(String s, int def) {
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return def; }
    }

    private static float parseFloat(String s, float def) {
        try { return Float.parseFloat(s); } catch (NumberFormatException e) { return def; }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        super.extractRenderState(g, mx, my, delta);
        FakeFramerateManager mgr = FakeFramerateManager.getInstance();

        g.centeredText(font, title, width / 2, startY - 18, GOLD);

        g.text(font, Component.translatable("gui.fix-my-laggg.fps"), COL1, fpsY + 4, WHITE, false);
        g.text(font, Component.translatable("gui.fix-my-laggg.fps_hint"), COL1, fpsY + ROW + 2, DIM, false);

        g.text(font, Component.translatable("gui.fix-my-laggg.sink"), COL1, sinkY + 4, WHITE, false);
        g.text(font, Component.translatable("gui.fix-my-laggg.sink_hint"), COL1, sinkY + ROW + 2, DIM, false);

        g.text(font, Component.translatable("gui.fix-my-laggg.threshold"), COL1, threshY + 4, WHITE, false);
        g.text(font, Component.translatable("gui.fix-my-laggg.threshold_hint"), COL1, threshY + ROW + 2, DIM, false);

        g.centeredText(font, "§7── §8" + Component.translatable("gui.fix-my-laggg.status").getString() + " §7──", width / 2, statusY, WHITE);

        String status = String.format("§7%s: §e%d  §7|  §7%s: §e%d  §7|  §7%s: §e%.0f",
            Component.translatable("gui.fix-my-laggg.real").getString(), mgr.getRealFps(),
            Component.translatable("gui.fix-my-laggg.fake").getString(), mgr.getCurrentFakeFps(),
            Component.translatable("gui.fix-my-laggg.peak").getString(), mgr.getPeakRealFps());
        g.centeredText(font, status, width / 2, statusY + 12, WHITE);

        g.centeredText(font, Component.translatable("gui.fix-my-laggg.hint"), width / 2, statusY + 28, 0xFF666666);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
