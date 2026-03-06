package net.runelite.client.plugins.doomorb;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("doomorb")
public interface DoomOrbConfig extends Config
{
    @ConfigItem(
        keyName = "bestColor",
        name = "Best orb color",
        description = "Highlight color for the best orb"
    )
    default Color bestColor()
    {
        return new Color(0,255,120);
    }

    @ConfigItem(
        keyName = "secondColor",
        name = "Second orb color",
        description = "Highlight color for the second orb"
    )
    default Color secondColor()
    {
        return new Color(255,150,0);
    }

    @ConfigItem(
        keyName = "pairLineColor",
        name = "Pair line color",
        description = "Color of the predicted line between the chosen orbs"
    )
    default Color pairLineColor()
    {
        return Color.CYAN;
    }

    @Alpha
    @ConfigItem(
        keyName = "fillAlpha",
        name = "Tile fill alpha",
        description = "Transparency of the tile fills"
    )
    default int fillAlpha()
    {
        return 40;
    }

    @Range(min = 8, max = 20)
    @ConfigItem(
        keyName = "maxDistance",
        name = "Max distance",
        description = "Ignore orbs farther than this from the player"
    )
    default int maxDistance()
    {
        return 10;
    }

    @ConfigItem(
        keyName = "showSecondOrb",
        name = "Show second orb",
        description = "Highlight the second orb in the chosen pair"
    )
    default boolean showSecondOrb()
    {
        return true;
    }

    @ConfigItem(
        keyName = "showPairLine",
        name = "Show pair line",
        description = "Draw a line between the chosen first and second orb"
    )
    default boolean showPairLine()
    {
        return true;
    }

    @ConfigItem(
        keyName = "showInfoPanel",
        name = "Show info panel",
        description = "Display state, chosen targets, and timing"
    )
    default boolean showInfoPanel()
    {
        return true;
    }
}
