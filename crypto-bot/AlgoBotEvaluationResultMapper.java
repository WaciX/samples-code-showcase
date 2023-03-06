package org.cryptobot.bot.mapper;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.cryptobot.common.dto.AlgoStatPeriodType;
import org.cryptobot.common.dto.bot.AlgoBotEvaluationResult;
import org.cryptobot.common.dto.trade.TradeChunkProfitCalculated;
import org.cryptobot.common.dto.trade.TradePeriod;
import org.cryptobot.common.entity.AlgoBot;
import org.cryptobot.common.entity.AlgoStat;
import org.cryptobot.common.entity.TradeChunk;
import org.cryptobot.common.entity.TradeSettings;
import org.cryptobot.data.service.TradeChunkDataService;
import org.cryptobot.messaging.dto.EventBusType;
import org.cryptobot.messaging.service.ReplyingKafkaService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlgoBotEvaluationResultMapper {

    final TradeChunkDataService tradeChunkDataService;
    final ReplyingKafkaService replyingKafkaService;

    public AlgoBotEvaluationResult map(List<AlgoStat> dailyAlgoStats, AlgoBot algoBot,
                                       TradePeriod tradePeriod, TradeSettings tradeSettings,
                                       boolean ignoreValidationChecks) {

        if (dailyAlgoStats.stream().map(AlgoStat::getPeriodType).anyMatch(periodType -> periodType != AlgoStatPeriodType.DAY)) {
            throw new IllegalStateException(String.format("Algo stats evaluation result mapper contains invalid " +
                            "period type. " +
                            "Bot id %s. Trade period %s, settings %s. Stats %s",
                    algoBot.getId(), tradePeriod, tradeSettings.getId(), dailyAlgoStats));
        }

        if (dailyAlgoStats.isEmpty()) {
            return AlgoBotEvaluationResult.builder()
                    .profit(BigDecimal.ZERO)
                    .score(BigDecimal.ZERO)
                    .placedOrdersPerDay(BigDecimal.ZERO)
                    .algoBot(algoBot)
                    .build();
        }

        var profitPercentSum = dailyAlgoStats.stream()
                .map(AlgoStat::getProfitLossPercentage)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        var profitPercentAverage = profitPercentSum
                .divide(BigDecimal.valueOf(dailyAlgoStats.size()), 2, RoundingMode.HALF_EVEN);

        var placedOrdersPerDay = dailyAlgoStats.stream()
                .map(algoStat -> algoStat.getLossOrderCount().add(algoStat.getProfitOrderCount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(dailyAlgoStats.size()), 2, RoundingMode.HALF_EVEN);

        if (placedOrdersPerDay.compareTo(BigDecimal.ZERO) == 0) {
            return AlgoBotEvaluationResult.builder()
                    .profit(BigDecimal.ZERO)
                    .score(BigDecimal.ZERO)
                    .placedOrdersPerDay(BigDecimal.ZERO)
                    .algoBot(algoBot)
                    .build();
        }

        var scoreMultiplier = BigDecimal.ONE;

        if (placedOrdersPerDay.compareTo(BigDecimal.valueOf(10)) < 0) {
            scoreMultiplier = placedOrdersPerDay.divide(BigDecimal.valueOf(10), 2, RoundingMode.HALF_EVEN);
        }

        BigDecimal periodProfitPercentSum = getPeriodProfitPercentSum(tradePeriod, tradeSettings,
                ignoreValidationChecks);
        if (periodProfitPercentSum != null) {
            // 500 profit, periodProfitPercentSum 2000: 500/2000 = 0.25
            // -500 profit, periodProfitPercentSum 2000: -500/2000 = -0.25
            scoreMultiplier = scoreMultiplier.multiply(profitPercentSum.divide(periodProfitPercentSum, 4,
                    RoundingMode.HALF_EVEN));
        }

        var score = profitPercentAverage.compareTo(BigDecimal.ZERO) >= 0 ?
                // 10 profit, score Multiplier 0.7: 10*0.7 = 7 (reduced profit by 3)
                profitPercentAverage.multiply(scoreMultiplier).setScale(2, RoundingMode.HALF_EVEN) :
                // -10 profit, score Multiplier 0.7: -10*(2-0.7) = -13 (reduced profit by 3)
                profitPercentAverage.multiply(BigDecimal.valueOf(2).subtract(scoreMultiplier)).setScale(2,
                        RoundingMode.HALF_EVEN);

        return AlgoBotEvaluationResult.builder()
                .placedOrdersPerDay(placedOrdersPerDay)
                .profit(profitPercentAverage)
                .score(score)
                .algoBot(algoBot)
                .build();
    }

    @SneakyThrows
    private BigDecimal getPeriodProfitPercentSum(TradePeriod tradePeriod, TradeSettings tradeSettings,
                                                 boolean ignoreValidationChecks) {
        List<TradeChunk> tradeChunks = tradeChunkDataService.getTradeChunksForPeriod(tradeSettings, tradePeriod);

        var missingTradeChunks = tradeChunks.stream()
                .filter(tradeChunk -> tradeChunk.getAggregatedProfitSum() == null)
                .toList();

        if (!missingTradeChunks.isEmpty()) {
            if (ignoreValidationChecks) {
                return null;
            }

            var missingTradeChunksIds = missingTradeChunks.stream()
                    .map(TradeChunk::getId)
                    .collect(Collectors.toSet());

            log.warn("Inaccurate Bot scoring due to missing trade chunks for period {} and settings {}. " +
                            "Waiting for the chunks {} to complete.",
                    tradePeriod, tradeSettings, missingTradeChunksIds);

            List<Future<TradeChunkProfitCalculated>> futures = missingTradeChunks.stream()
                    .map(tradeChunk -> replyingKafkaService.<TradeChunkProfitCalculated>listen(
                            EventBusType.TRADE_CHUNK_PROFIT_CALCULATED, tradeChunk.getId()))
                    .toList();

            for (Future<TradeChunkProfitCalculated> future : futures) {
                var tradeChunkProfitCompleted = future.get(10, TimeUnit.MINUTES);

                missingTradeChunksIds.remove(tradeChunkProfitCompleted.getTradeChunkId());

                log.warn("Trade chunk loaded {}, remaining missing {}",
                        tradeChunkProfitCompleted.getTradeChunkId(), missingTradeChunksIds);
            }

            tradeChunks = tradeChunkDataService.getTradeChunksForPeriod(tradeSettings, tradePeriod);
        }

        BigDecimal result = tradeChunks.stream()
                .map(TradeChunk::getAggregatedProfitSum)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (result.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        return result;
    }
}
