package skinsrestorer.bungee.listeners;

import java.util.concurrent.TimeUnit;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import skinsrestorer.bungee.SkinsRestorer;
import skinsrestorer.shared.format.SkinProfile;
import skinsrestorer.shared.storage.ConfigStorage;
import skinsrestorer.shared.storage.LocaleStorage;
import skinsrestorer.shared.storage.SkinStorage;
import skinsrestorer.shared.utils.C;
import skinsrestorer.shared.utils.ReflectionUtil;
import skinsrestorer.shared.utils.SkinFetchUtils.SkinFetchFailedException;

public class LoginListener implements Listener {

	// load skin data on login
	@EventHandler(priority = EventPriority.LOW)
	public void onPreLogin(final PostLoginEvent event) {
		if (ConfigStorage.getInstance().USE_BOT_FEATURE == true) {
			return;
		}
		if (SkinsRestorer.getInstance().isAutoInEnabled()) {

			try {

				Object api = ReflectionUtil.invokeMethod(Class.forName("com.gmail.bartlomiejkmazur.autoin.api.APICore"),
						null, "getAPI");
				Enum<?> premiumStatus = (Enum<?>) ReflectionUtil.invokeMethod(api.getClass(), null, "getPremiumStatus",
						new Class<?>[] { String.class }, event.getPlayer().getName());
				Enum<?> premiumEnum = ReflectionUtil
						.getEnum(Class.forName("com.gmail.bartlomiejkmazur.autoin.api.PremiumStatus"), "PREMIUM");

				if (ConfigStorage.getInstance().USE_AUTOIN_SKINS == true && premiumStatus == premiumEnum) {
					return;
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		final String name = event.getPlayer().getName();
		final SkinProfile skinprofile = SkinStorage.getInstance().getOrCreateSkinForPlayer(name.toLowerCase());
		ProxyServer.getInstance().getScheduler().runAsync(SkinsRestorer.getInstance(), new Runnable() {

			@Override
			public void run() {
				try {
					skinprofile.attemptUpdate();
					SkinsRestorer.getInstance().getFactory().applySkin(event.getPlayer());
				} catch (SkinFetchFailedException e) {
					SkinsRestorer.getInstance().logInfo("Skin fetch failed for player " + name + ": " + e.getMessage());
				}
			}

		});
	}

	// Apply skin on join.
	@EventHandler(priority = EventPriority.LOW)
	public void onPreLogin(final ServerConnectedEvent event) {
		if (ConfigStorage.getInstance().USE_BOT_FEATURE == false) {
			return;
		}
		if (SkinsRestorer.getInstance().isAutoInEnabled()) {

			try {

				Object api = ReflectionUtil.invokeMethod(Class.forName("com.gmail.bartlomiejkmazur.autoin.api.APICore"),
						null, "getAPI");
				Enum<?> premiumStatus = (Enum<?>) ReflectionUtil.invokeMethod(api.getClass(), null, "getPremiumStatus",
						new Class<?>[] { String.class }, event.getPlayer().getName());
				Enum<?> premiumEnum = ReflectionUtil
						.getEnum(Class.forName("com.gmail.bartlomiejkmazur.autoin.api.PremiumStatus"), "PREMIUM");

				if (ConfigStorage.getInstance().USE_AUTOIN_SKINS == true && premiumStatus == premiumEnum) {
					return;
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		if (event.getPlayer().hasPermission("skinsrestorer.playercmds")) {
			final TextComponent message = new TextComponent("");
			message.addExtra(C.c(LocaleStorage.DO_YOU_WANT_SKIN));
			message.setClickEvent(
					new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/skin set " + event.getPlayer().getName()));
			ProxyServer.getInstance().getScheduler().schedule(SkinsRestorer.getInstance(), new Runnable() {
				@Override
				public void run() {
					event.getPlayer().sendMessage(message);
				}
			}, 5L, TimeUnit.MILLISECONDS);
		}
	}
}
