package com.namelessmc.bot.commands;

import com.namelessmc.bot.Language;
import com.namelessmc.bot.Language.Term;
import com.namelessmc.bot.Main;
import com.namelessmc.bot.connections.BackendStorageException;
import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.NamelessException;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.Optional;

public class PingCommand extends Command {

	public PingCommand() {
		super("ping", Collections.emptyList(), CommandContext.PRIVATE_MESSAGE);
	}

	@Override
	public void execute(final User user, final String[] args, final Message message) {
		Language language = Language.getDefaultLanguage();

		if (args.length != 1) {
			message.reply(language.get(Term.PING_USAGE, "command", Main.getCommandPrefix() + "ping")).queue();
			return;
		}

		final long guildId;
		try {
			guildId = Long.parseLong(args[0]);
		} catch (final NumberFormatException e) {
			message.reply(language.get(Term.ERROR_GUILD_ID_INVALID)).queue();
			return;
		}

		final Guild guild = Main.getJda().getGuildById(guildId);

		if (guild == null) {
			message.reply(language.get(Term.ERROR_GUILD_ID_INVALID)).queue();
			return;
		}

		if (!Main.canModifySettings(user, guild)) {
			message.reply(language.get(Term.ERROR_NO_PERMISSION)).queue();
			return;
		}

		// Check if API URL works
		Optional<NamelessAPI> optApi;
		try {
			optApi = Main.getConnectionManager().getApi(guildId);
		} catch (final BackendStorageException e) {
			message.reply(language.get(Term.ERROR_GENERIC)).queue();
			e.printStackTrace();
			return;
		}

		if (optApi.isEmpty()) {
			message.reply(language.get(Term.ERROR_NOT_SET_UP)).queue();
			return;
		}

		final NamelessAPI api = optApi.get();

		try {
			final long start = System.currentTimeMillis();
			api.checkWebAPIConnection();
			final long time = System.currentTimeMillis() - start;
			language = Language.getDiscordUserLanguage(api, user);
			message.getChannel().sendMessage(language.get(Term.PING_WORKING, "time", time)).queue();
		} catch (final NamelessException e) {
			message.getChannel().sendMessage(new MessageBuilder().appendCodeBlock(StringUtils.truncate(e.getMessage(), 1500), "txt").build()).queue();
			message.reply(language.get(Term.APIURL_FAILED_CONNECTION)).queue();
			if (api.getApiUrl().toString().startsWith("http://")) {
				message.getChannel().sendMessage(language.get(Term.APIURL_TRY_HTTPS)).queue();
			}
		}
	}

}
