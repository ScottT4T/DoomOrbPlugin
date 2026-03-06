package com.doomorb;

import javax.inject.Inject;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.OverlayPanel;

public class DoomOrbInfoOverlay extends OverlayPanel
{
    private final DoomOrbPlugin plugin;
    private final PanelComponent panel = getPanelComponent();

    @Inject
    public DoomOrbInfoOverlay(DoomOrbPlugin plugin)
    {
        this.plugin = plugin;
        setPosition(OverlayPosition.TOP_LEFT);
    }

    @Override
    public java.awt.Dimension render(java.awt.Graphics2D graphics)
    {
        if (!plugin.getConfig().showInfoPanel())
        {
            return null;
        }

        panel.getChildren().clear();

        DoomOrbTracker tracker = plugin.getTracker();
        OrbPair pair = tracker.getSelectedPair();

        panel.getChildren().add(LineComponent.builder()
            .left("Doom Orb")
            .right(tracker.getPhaseState().name())
            .build());

        panel.getChildren().add(LineComponent.builder()
            .left("Tracked")
            .right(String.valueOf(tracker.getTrackedOrbs().size()))
            .build());

        panel.getChildren().add(LineComponent.builder()
            .left("Tick")
            .right(String.valueOf(tracker.getTick()))
            .build());

        if (pair != null)
        {
            panel.getChildren().add(LineComponent.builder()
                .left("First")
                .right(formatPoint(pair.getFirst()))
                .build());

            if (plugin.getConfig().showSecondOrb())
            {
                panel.getChildren().add(LineComponent.builder()
                    .left("Second")
                    .right(formatPoint(pair.getSecond()))
                    .build());
            }

            panel.getChildren().add(LineComponent.builder()
                .left("Hint")
                .right(pair.getSafeHint())
                .build());

            panel.getChildren().add(LineComponent.builder()
                .left("Pair score")
                .right(String.valueOf(pair.getScore()))
                .build());
        }
        else
        {
            panel.getChildren().add(LineComponent.builder()
                .left("Target")
                .right("Acquiring")
                .build());
        }

        return super.render(graphics);
    }

    private String formatPoint(TrackedOrb orb)
    {
        return orb.getLocation().getX() + "," + orb.getLocation().getY();
    }
}
