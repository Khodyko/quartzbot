package org.khodyko.quartzbot.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Data
public class BotConfig {

    @Value("${bot.name:${BOT_NAME:java_holic_bot}}")
    String botName;
    
    @Value("${bot.token:${BOT_TOKEN:}}")
    String token;
}
