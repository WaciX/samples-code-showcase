package org.cryptobot.app.bot.service;

import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cryptobot.bot.BotContext;
import org.cryptobot.bot.BotFactory;
import org.cryptobot.bot.dto.CachedBarSeries;
import org.cryptobot.bot.mapper.AlgoBotEvaluationResultMapper;
import org.cryptobot.bot.service.BarSeriesCachingService;
import org.cryptobot.common.dto.bot.*;
import org.cryptobot.common.dto.trade.TradePeriod;
import org.cryptobot.common.entity.AlgoBot;
import org.cryptobot.common.entity.AlgoStat;
import org.cryptobot.common.entity.PlacedOrder;
import org.cryptobot.common.entity.TradeSettings;
import org.cryptobot.common.model.EvaluationMode;
import org.cryptobot.messaging.dto.EventBusType;
import org.cryptobot.statistics.service.AlgoBotStatCollectorService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class AdHocExecutorService {

    final BotFactory botFactory;

    final AlgoBotEvaluationResultMapper algoBotEvaluationResultMapper;

    final AlgoBotStatCollectorService algoBotStatCollectorService;

    final BarSeriesCachingService barSeriesCachingService;

    final KafkaTemplate<String, AdhocBotEvaluationResult> adhocBotEvaluationResultKafkaTemplate;


    @KafkaListener(id = "bot-adhoc-evaluation", topics = EventBusType.ADHOC_BOT_EVALUATION,
            containerFactory = "adhocBotEvaluationRequestKafkaListenerContainerFactory")
    public void executeAdhocEvaluationListener(@Payload @Valid AdhocBotEvaluationRequest request)
            throws ExecutionException, InterruptedException, TimeoutException {
        var result = executeAdhocEvaluation(request);

        adhocBotEvaluationResultKafkaTemplate.sendDefault(request.getAlgoBot().getName(), result)
                .get(30, TimeUnit.SECONDS);
    }

    @Timed
    public AdhocBotEvaluationResult executeAdhocEvaluation(AdhocBotEvaluationRequest request) {
        var algoBot = request.getAlgoBot();
        var runParameters = request.getRunParameters();

        BotContext botContext = botFactory.createBotContext(algoBot);

        TradePeriod tradePeriod = runParameters.getTradePeriod();

        log.debug("Bot {} loading bars on period {}", algoBot.getName(), tradePeriod);

        CachedBarSeries cachedBarSeries = barSeriesCachingService.loadBarSeriesTradesForHistoricPeriod(
                tradePeriod, runParameters.getTradeSettings(),
                !request.isIgnoreValidationChecks());

        BarSeries barSeries = cachedBarSeries.getBarSeries();

        log.debug("Bot {} loaded bar count {}", algoBot.getName(), barSeries.getBarCount());

        List<PlacedOrder> placedOrders = adhocBarSeries(botContext, barSeries, runParameters.getTradeSettings());
        if (runParameters.getEvaluationMode().equals(EvaluationMode.IGNORE_LOSSES)) {
            placedOrders = removeLossOrderPairs(placedOrders);
        }

        log.debug("Bot {} placed orders count {}", algoBot.getName(), placedOrders.size());

        List<AlgoStat> algoStats = algoBotStatCollectorService.collectAdhocForPeriod(
                algoBot.getId(), BotRunningMode.SIMULATION, runParameters, placedOrders);

        log.debug("Bot {} stats {}", algoBot.getName(), algoStats);

        var algoBotEvaluationResult = calculateEvaluationResult(algoBot, algoStats, tradePeriod,
                runParameters.getTradeSettings(), request.isIgnoreValidationChecks());

        return AdhocBotEvaluationResult.builder()
                .algoBotEvaluationResult(algoBotEvaluationResult)
                .build();
    }

    private AlgoBotEvaluationResult calculateEvaluationResult(
            AlgoBot algoBot, List<AlgoStat> algoStats,
            TradePeriod tradePeriod, TradeSettings tradeSettings,
            boolean ignoreValidationChecks) {

        AlgoBotEvaluationResult algoBotEvaluationResult = algoBotEvaluationResultMapper.map(
                algoStats, algoBot, tradePeriod, tradeSettings, ignoreValidationChecks);

        log.debug("Bot {} finished. Result: {}.", algoBot.getName(), algoBotEvaluationResult);

        return algoBotEvaluationResult;
    }

    @Timed
    List<PlacedOrder> adhocBarSeries(BotContext botContext, BarSeries barSeries, TradeSettings tradeSettings) {
        return botContext.adhocBarSeries(barSeries, tradeSettings);
    }

    // TODO move it somewhere else
    private List<PlacedOrder> removeLossOrderPairs(List<PlacedOrder> placedOrders) {
        AtomicReference<PlacedOrder> lastBuyOrder = new AtomicReference<>();

        return placedOrders.stream()
                .filter(placedOrder -> {
                    switch (placedOrder.getTransactionType()) {
                        case BUY -> {
                            lastBuyOrder.set(placedOrder);
                            return true;
                        }
                        case SELL -> {
                            if (lastBuyOrder.get() == null) {
                                break;
                            }
                            BigDecimal buyValue = lastBuyOrder.get().getValue();
                            BigDecimal sellValue = placedOrder.getValue();
                            BigDecimal subtract = sellValue.subtract(buyValue);
                            int diff = subtract.compareTo(BigDecimal.ZERO);
                            if (diff > 0) {
                                return true;
                            }
                        }
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }
}
