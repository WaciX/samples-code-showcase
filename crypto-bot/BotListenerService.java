package org.cryptobot.app.bot.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.cryptobot.common.dto.bot.BotPromoteRequest;
import org.cryptobot.common.entity.Trade;
import org.cryptobot.messaging.dto.EventBusType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BotListenerService {

    private final BotPromotionService botService;
    private final BotExecuteService botExecuteService;

    @KafkaListener(id = "bot-promote-listener", topics = EventBusType.BOT_PROMOTE,
            containerFactory = "botPromoteRequestKafkaListenerContainerFactory")
    public void promoteBot(@Payload @Valid BotPromoteRequest request) {
        botService.startTrading(request.getAlgoBotId(), request.getBotRunningMode());
    }

    @KafkaListener(id = "bot-new-trade-listener", topics = EventBusType.AGGREGATED_TRADE,
            containerFactory = "tradeKafkaListenerContainerFactory")
    public void newTrade(@Payload @Valid Trade trade) {
        botExecuteService.newTrade(trade);
    }
}
