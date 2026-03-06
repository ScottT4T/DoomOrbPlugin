package net.runelite.client.plugins.doomorb;

import net.runelite.client.plugins.doomorb.*;

@Getter
public class OrbPair
{
    private final TrackedOrb first;
    private final TrackedOrb second;
    private final int score;
    private final String safeHint;

    public OrbPair(TrackedOrb first, TrackedOrb second, int score, String safeHint)
    {
        this.first = first;
        this.second = second;
        this.score = score;
        this.safeHint = safeHint;
    }

}
