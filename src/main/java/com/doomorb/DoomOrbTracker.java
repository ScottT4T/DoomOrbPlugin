package com.doomorb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;

@Singleton
public class DoomOrbTracker
{
    private static final int VOLATILE_EARTH_NPC_ID = 14714;
    private static final int SAFE_ORB_NPC_ID = 14715;

    private final Client client;
    private final DoomOrbConfig config;

    private long tick;
    private final List<TrackedOrb> trackedOrbs = new ArrayList<>();
    private OrbPair selectedPair;
    private PhaseState phaseState = PhaseState.IDLE;

    private WorldPoint safeOrbLocation;

    // Used to stop recalculating the pair continuously while the player moves.
    private long lastVolatileEarthSpawnTick = -1L;
    private boolean selectionLocked = false;

    @Inject
    public DoomOrbTracker(Client client, DoomOrbConfig config)
    {
        this.client = client;
        this.config = config;
    }

    public WorldPoint getSafeOrbLocation()
    {
        return safeOrbLocation;
    }

    public void reset()
    {
        tick = 0L;
        trackedOrbs.clear();
        selectedPair = null;
        phaseState = PhaseState.IDLE;
        safeOrbLocation = null;
        lastVolatileEarthSpawnTick = -1L;
        selectionLocked = false;
    }

    private NPC findSafeOrbNpc()
    {
        for (NPC npc : client.getNpcs())
        {
            if (npc != null && npc.getId() == SAFE_ORB_NPC_ID)
            {
                return npc;
            }
        }

        return null;
    }

    public void onTick()
    {
        tick++;

        NPC safeOrbNpc = findSafeOrbNpc();
        safeOrbLocation = safeOrbNpc != null ? safeOrbNpc.getWorldLocation() : null;

        // Lock the pair a short moment after the last volatile earth spawn,
        // so movement does not keep changing the recommendation.
        if (!selectionLocked && lastVolatileEarthSpawnTick != -1L && tick - lastVolatileEarthSpawnTick >= 2)
        {
            selectionLocked = true;
        }
    }

    public void onNpcSpawned(NPC npc)
    {
        if (npc == null)
        {
            return;
        }

        if (npc.getId() == VOLATILE_EARTH_NPC_ID)
        {
            WorldPoint location = npc.getWorldLocation();

            trackedOrbs.add(new TrackedOrb(npc.getId(), location, tick));

            // Recompute while the spawn wave is still forming.
            if (!selectionLocked)
            {
                recomputeStateAndSelection();
            }

            lastVolatileEarthSpawnTick = tick;
            return;
        }

        if (npc.getId() == SAFE_ORB_NPC_ID)
        {
            safeOrbLocation = npc.getWorldLocation();
        }
    }

    public void onNpcDespawned(NPC npc)
    {
        if (npc == null)
        {
            return;
        }

        if (npc.getId() == VOLATILE_EARTH_NPC_ID)
        {
            WorldPoint location = npc.getWorldLocation();

            Iterator<TrackedOrb> iterator = trackedOrbs.iterator();
            while (iterator.hasNext())
            {
                TrackedOrb orb = iterator.next();
                if (orb.getId() == VOLATILE_EARTH_NPC_ID && orb.getLocation().equals(location))
                {
                    iterator.remove();
                    break;
                }
            }

            // When the wave ends, fully reset selection state.
            if (trackedOrbs.isEmpty())
            {
                selectedPair = null;
                phaseState = PhaseState.IDLE;
                lastVolatileEarthSpawnTick = -1L;
                selectionLocked = false;
            }
            else if (!selectionLocked)
            {
                // Only recompute during the initial spawn window.
                recomputeStateAndSelection();
            }

            return;
        }

        if (npc.getId() == SAFE_ORB_NPC_ID)
        {
            safeOrbLocation = null;
        }
    }

    public long getTick()
    {
        return tick;
    }

    public PhaseState getPhaseState()
    {
        return phaseState;
    }

    public List<TrackedOrb> getTrackedOrbs()
    {
        return Collections.unmodifiableList(trackedOrbs);
    }

    public List<TrackedOrb> getViableOrbs(int maxDistance, int staleAfterTicks)
    {
        Player player = client.getLocalPlayer();
        if (player == null)
        {
            return Collections.emptyList();
        }

        WorldPoint playerLocation = player.getWorldLocation();

        return trackedOrbs.stream()
                .filter(o -> o.getLocation() != null)
                .filter(o -> o.getLocation().getPlane() == playerLocation.getPlane())
                .filter(o -> o.getLocation().distanceTo(playerLocation) <= maxDistance)
                .filter(o -> o.ageOn(tick) <= staleAfterTicks)
                .sorted(new Comparator<TrackedOrb>()
                {
                    @Override
                    public int compare(TrackedOrb a, TrackedOrb b)
                    {
                        int sa = OrbScorer.scoreSingle(playerLocation, a, tick);
                        int sb = OrbScorer.scoreSingle(playerLocation, b, tick);
                        return Integer.compare(sb, sa);
                    }
                })
                .collect(Collectors.toList());
    }

    public OrbPair getSelectedPair()
    {
        return selectedPair;
    }

    public NPC getBossNpc()
    {
        for (NPC npc : client.getNpcs())
        {
            if (npc != null && npc.getName() != null && npc.getName().equalsIgnoreCase("Doom of Mokhaiotl"))
            {
                return npc;
            }
        }

        return null;
    }

    private void recomputeStateAndSelection()
    {
        Player player = client.getLocalPlayer();

        if (player == null || trackedOrbs.isEmpty())
        {
            phaseState = PhaseState.IDLE;
            selectedPair = null;
            return;
        }

        WorldPoint playerLocation = player.getWorldLocation();

        List<TrackedOrb> candidates = trackedOrbs.stream()
                .filter(o -> o.getLocation() != null)
                .filter(o -> o.getLocation().getPlane() == playerLocation.getPlane())
                .filter(o -> o.ageOn(tick) <= 100)
                .collect(Collectors.toList());

        if (candidates.isEmpty())
        {
            phaseState = PhaseState.ACQUIRING;
            selectedPair = null;
            return;
        }

        phaseState = candidates.size() >= 2 ? PhaseState.ACTIVE : PhaseState.ACQUIRING;
        selectedPair = candidates.size() >= 2
                ? OrbScorer.bestPair(playerLocation, candidates, tick, getBossNpc(), config.maxDistance())
                : null;
    }
}