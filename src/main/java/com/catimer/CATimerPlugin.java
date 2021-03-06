package com.catimer;

import java.lang.*;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.NpcChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.ui.overlay.infobox.Timer;


@Slf4j
@PluginDescriptor(
	name = "CA Timers"
)
public class CATimerPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private CATimerConfig config;

	@Inject
	private InfoBoxManager infoBoxManager;

	@Inject
	private ItemManager itemManager;

	@Provides
	CATimerConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(CATimerConfig.class);
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned npcSpawned)
	{
		NPC npc = npcSpawned.getNpc();
		if (npc.isDead()) {
			return;
		}

		int npcId = npc.getId();
		CABoss boss = CABoss.find(npcId);

		if (boss == null)
		{
			return;
		}
		long configTime;
        switch (npcId) {
            case NpcID.HESPORI:
                 configTime = config.hesporiTime().getTime();
                break;
            case NpcID.ZULRAH:
                configTime = config.zulrahTime().getTime();
                break;
			case NpcID.ALCHEMICAL_HYDRA:
				if (client.isInInstancedRegion()){
					configTime = config.hydraTime().getTime();
				} else {
					configTime = 0;
				}
				break;
            default:
                configTime = 0;
        }
		infoBoxManager.removeIf(t -> t instanceof SpeedrunTimer && ((SpeedrunTimer) t).getBoss() == boss);
        if (configTime != 0) {
			SpeedrunTimer timer = new SpeedrunTimer(boss, configTime, itemManager.getImage(boss.getItemSpriteId()), this);
			timer.setTooltip(npc.getName());
			infoBoxManager.addInfoBox(timer);
        }
	}

	// Vorkath case - will adjust as new bosses are added
	@Subscribe
	public void onNpcChanged(NpcChanged npcChanged) {
		NPC npc =  npcChanged.getNpc();
		NPCComposition npcComp = npc.getComposition();
		String firstAction = npcComp.getActions()[1];

		int npcId = npc.getId();
		CABoss boss = CABoss.find(npcId);

		if (boss == null || boss.getId() != NpcID.VORKATH_8061)
		{
			return;
		}
		long configTime = config.vorkathTime().getTime();
		infoBoxManager.removeIf(t -> t instanceof SpeedrunTimer && ((SpeedrunTimer) t).getBoss() == boss);
		if (Objects.equals(firstAction, "Attack") && configTime != 0) {
			SpeedrunTimer timer = new SpeedrunTimer(boss, configTime, itemManager.getImage(boss.getItemSpriteId()), this);
			timer.setTooltip(npc.getName());
			infoBoxManager.addInfoBox(timer);
		}
	}
}
