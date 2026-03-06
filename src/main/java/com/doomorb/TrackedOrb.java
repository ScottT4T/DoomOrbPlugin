package com.doomorb;

import net.runelite.api.coords.WorldPoint;

public class TrackedOrb
{
    private final int id;
    private final WorldPoint location;
    private final long spawnTick;

    public TrackedOrb(int id, WorldPoint location, long spawnTick)
    {
        this.id = id;
        this.location = location;
        this.spawnTick = spawnTick;
    }

    public int getId()
    {
        return id;
    }

    public WorldPoint getLocation()
    {
        return location;
    }

    public long getSpawnTick()
    {
        return spawnTick;
    }

    public int ageOn(long currentTick)
    {
        return (int) Math.max(0, currentTick - spawnTick);
    }
}