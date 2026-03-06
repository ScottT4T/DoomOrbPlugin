package net.runelite.client.plugins.doomorb;

import java.util.List;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;

public final class OrbScorer
{
    private OrbScorer()
    {
    }

    public static OrbPair bestPair(
            WorldPoint player,
            List<TrackedOrb> candidates,
            long currentTick,
            NPC boss,
            int maxDistance
    )
    {
        OrbPair bestWithinLimit = null;
        int bestWithinScore = Integer.MIN_VALUE;

        OrbPair bestFallback = null;
        int bestFallbackOverflow = Integer.MAX_VALUE;
        int bestFallbackScore = Integer.MIN_VALUE;

        for (TrackedOrb first : candidates)
        {
            for (TrackedOrb second : candidates)
            {
                if (first == second)
                {
                    continue;
                }

                int score = scorePair(player, first, second, currentTick, boss);
                if (score == Integer.MIN_VALUE)
                {
                    continue;
                }

                int pairDistance = first.getLocation().distanceTo(second.getLocation());
                OrbPair pair = new OrbPair(first, second, score, inferSafeHint(first, second));

                if (pairDistance <= maxDistance)
                {
                    if (score > bestWithinScore)
                    {
                        bestWithinScore = score;
                        bestWithinLimit = pair;
                    }
                }
                else
                {
                    int overflow = pairDistance - maxDistance;

                    if (overflow < bestFallbackOverflow
                            || (overflow == bestFallbackOverflow && score > bestFallbackScore))
                    {
                        bestFallbackOverflow = overflow;
                        bestFallbackScore = score;
                        bestFallback = pair;
                    }
                }
            }
        }

        return bestWithinLimit != null ? bestWithinLimit : bestFallback;
    }

    public static int scoreSingle(WorldPoint player, TrackedOrb orb, long currentTick)
    {
        int distanceToPlayer = player.distanceTo(orb.getLocation());
        int age = orb.ageOn(currentTick);

        int score = 0;
        score += Math.max(0, 40 - distanceToPlayer * 4);
        score += Math.max(0, 8 - age);

        return score;
    }

    private static int scorePair(WorldPoint player, TrackedOrb first, TrackedOrb second, long currentTick, NPC boss)
    {
        WorldPoint a = first.getLocation();
        WorldPoint b = second.getLocation();

        if (boss != null && crossesBoss(a, b, boss))
        {
            return Integer.MIN_VALUE;
        }

        int firstToSecond = a.distanceTo(b);
        int secondToPlayer = player.distanceTo(b);
        int firstToPlayer = player.distanceTo(a);

        int dx = Math.abs(a.getX() - b.getX());
        int dy = Math.abs(a.getY() - b.getY());

        int score = 0;

        // SECOND should be near the player.
        score += Math.max(0, 150 - secondToPlayer * 20);

        // FIRST should be far from SECOND.
        score += firstToSecond * 22;

        // Prefer clean horizontal / vertical / diagonal relationships.
        if (dx == 0 || dy == 0)
        {
            score += 30;
        }
        else if (dx == dy)
        {
            score += 22;
        }
        else
        {
            score -= 40;
        }

        // Slight preference against absurdly far first targets.
        score += Math.max(0, 30 - firstToPlayer * 2);

        // Slight age bonus.
        score += Math.max(0, 6 - first.ageOn(currentTick));
        score += Math.max(0, 6 - second.ageOn(currentTick));

        return score;
    }

    private static boolean crossesBoss(WorldPoint a, WorldPoint b, NPC boss)
    {
        if (boss == null)
        {
            return false;
        }

        WorldPoint base = boss.getWorldLocation();
        int size = boss.getComposition() != null ? boss.getComposition().getSize() : 1;

        int minX = base.getX();
        int maxX = base.getX() + size - 1;
        int minY = base.getY();
        int maxY = base.getY() + size - 1;

        int steps = Math.max(Math.abs(b.getX() - a.getX()), Math.abs(b.getY() - a.getY()));
        if (steps == 0)
        {
            return insideBox(a.getX(), a.getY(), minX, maxX, minY, maxY);
        }

        for (int i = 0; i <= steps; i++)
        {
            double t = (double) i / (double) steps;
            int x = (int) Math.round(a.getX() + (b.getX() - a.getX()) * t);
            int y = (int) Math.round(a.getY() + (b.getY() - a.getY()) * t);

            if (insideBox(x, y, minX, maxX, minY, maxY))
            {
                return true;
            }
        }

        return false;
    }

    private static boolean insideBox(int x, int y, int minX, int maxX, int minY, int maxY)
    {
        return x >= minX && x <= maxX && y >= minY && y <= maxY;
    }

    private static String inferSafeHint(TrackedOrb first, TrackedOrb second)
    {
        WorldPoint a = first.getLocation();
        WorldPoint b = second.getLocation();

        int dx = b.getX() - a.getX();
        int dy = b.getY() - a.getY();

        if (Math.abs(dx) >= Math.abs(dy))
        {
            return dx >= 0 ? "SECOND RIGHT" : "SECOND LEFT";
        }

        return dy >= 0 ? "SECOND UP" : "SECOND DOWN";
    }
}