package net.runelite.client.plugins.doomorb;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.List;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public class DoomOrbSceneOverlay extends Overlay
{
    private final DoomOrbPlugin plugin;

    @Inject
    public DoomOrbSceneOverlay(DoomOrbPlugin plugin)
    {
        this.plugin = plugin;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        Client client = plugin.getClient();
        DoomOrbConfig config = plugin.getConfig();
        DoomOrbTracker tracker = plugin.getTracker();

        List<TrackedOrb> visible = tracker.getTrackedOrbs();
        OrbPair pair = tracker.getSelectedPair();

        for (TrackedOrb orb : visible)
        {
            boolean isBest = pair != null && pair.getFirst() == orb;
            boolean isSecond = pair != null && pair.getSecond() == orb;

            if (!isBest && !isSecond)
            {
                continue;
            }

            if (isSecond && !config.showSecondOrb())
            {
                continue;
            }

            Color base = isBest
                    ? config.bestColor()
                    : config.secondColor();

            drawOrbTile(graphics, client, orb, base, config.fillAlpha());

            String label = "";
            if (isBest)
            {
                label = "1";
            }
            else if (isSecond)
            {
                label = "2";
            }

            if (!label.isEmpty())
            {
                drawLabel(graphics, client, orb.getLocation(), label, base);
            }
        }

        if (pair != null && config.showPairLine())
        {
            drawPairLine(graphics, client, pair, config.pairLineColor());
        }

        WorldPoint safeOrbLocation = tracker.getSafeOrbLocation();
        if (safeOrbLocation != null)
        {
            drawSafeOrb(graphics, client, safeOrbLocation);
        }

        return null;
    }

    private void drawOrbTile(Graphics2D graphics, Client client, TrackedOrb orb, Color color, int alpha)
    {
        LocalPoint localPoint = toLocalPoint(client, orb.getLocation());
        if (localPoint == null)
        {
            return;
        }

        Polygon polygon = Perspective.getCanvasTilePoly(client, localPoint);
        if (polygon == null)
        {
            return;
        }

        graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
        graphics.fill(polygon);

        graphics.setColor(color);
        graphics.setStroke(new BasicStroke(2));
        graphics.draw(polygon);
    }

    private void drawLabel(Graphics2D graphics, Client client, WorldPoint worldPoint, String label, Color color)
    {
        LocalPoint localPoint = toLocalPoint(client, worldPoint);
        if (localPoint == null)
        {
            return;
        }

        Point textLocation = Perspective.getCanvasTextLocation(
                client,
                graphics,
                localPoint,
                label,
                0
        );

        if (textLocation == null)
        {
            return;
        }

        graphics.setColor(Color.BLACK);
        graphics.drawString(label, textLocation.getX() + 1, textLocation.getY() + 1);
        graphics.setColor(color);
        graphics.drawString(label, textLocation.getX(), textLocation.getY());
    }

    private void drawPairLine(Graphics2D graphics, Client client, OrbPair pair, Color color)
    {
        LocalPoint a = toLocalPoint(client, pair.getFirst().getLocation());
        LocalPoint b = toLocalPoint(client, pair.getSecond().getLocation());

        if (a == null || b == null)
        {
            return;
        }

        Point pa = Perspective.localToCanvas(client, a, client.getPlane());
        Point pb = Perspective.localToCanvas(client, b, client.getPlane());

        if (pa == null || pb == null)
        {
            return;
        }

        graphics.setColor(color);
        graphics.setStroke(new BasicStroke(2));
        graphics.drawLine(pa.getX(), pa.getY(), pb.getX(), pb.getY());
    }

    private void drawSafeOrb(Graphics2D graphics, Client client, WorldPoint worldPoint)
    {
        LocalPoint localPoint = toLocalPoint(client, worldPoint);
        if (localPoint == null)
        {
            return;
        }

        Polygon polygon = Perspective.getCanvasTilePoly(client, localPoint);
        if (polygon == null)
        {
            return;
        }

        Color safeColor = Color.CYAN;

        graphics.setColor(new Color(safeColor.getRed(), safeColor.getGreen(), safeColor.getBlue(), 70));
        graphics.fill(polygon);

        graphics.setColor(safeColor);
        graphics.setStroke(new BasicStroke(3));
        graphics.draw(polygon);

        Point textLocation = Perspective.getCanvasTextLocation(client, graphics, localPoint, "SAFE", 0);
        if (textLocation == null)
        {
            return;
        }

        graphics.setColor(Color.BLACK);
        graphics.drawString("SAFE", textLocation.getX() + 1, textLocation.getY() + 1);
        graphics.setColor(safeColor);
        graphics.drawString("SAFE", textLocation.getX(), textLocation.getY());
    }

    private LocalPoint toLocalPoint(Client client, WorldPoint worldPoint)
    {
        if (worldPoint == null)
        {
            return null;
        }

        return LocalPoint.fromWorld(client, worldPoint);
    }
}