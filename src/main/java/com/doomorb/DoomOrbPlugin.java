package net.runelite.client.plugins.doomorb;

import com.google.inject.Provides;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemDespawned;
import net.runelite.api.events.ItemSpawned;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.NpcDespawned;

@PluginDescriptor(
        name = "Doom Orb Helper",
        description = "Highlights volatile earth tiles during the moving safety boulder phase",
        tags = {"pvm", "boss", "overlay", "orb", "doom"}
)
public class DoomOrbPlugin extends Plugin
{
    @Inject
    private Client client;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private DoomOrbTracker tracker;

    @Inject
    private DoomOrbSceneOverlay sceneOverlay;

    @Inject
    private DoomOrbInfoOverlay infoOverlay;

    @Inject
    private DoomOrbConfig config;

    @Provides
    DoomOrbConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(DoomOrbConfig.class);
    }

    @Override
    protected void startUp()
    {
        tracker.reset();
        overlayManager.add(sceneOverlay);
        overlayManager.add(infoOverlay);
    }

    @Override
    protected void shutDown()
    {
        overlayManager.remove(sceneOverlay);
        overlayManager.remove(infoOverlay);
        tracker.reset();
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {
        tracker.onTick();
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned event)
    {
        tracker.onNpcSpawned(event.getNpc());
    }

    @Subscribe
    public void onNpcDespawned(NpcDespawned event)
    {
        tracker.onNpcDespawned(event.getNpc());
    }

    public Client getClient()
    {
        return client;
    }

    public DoomOrbConfig getConfig()
    {
        return config;
    }

    public DoomOrbTracker getTracker()
    {
        return tracker;
    }
}