package com.sleet.api.service;

import com.sleet.api.model.Option;
import com.sleet.api.model.OptionChain;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Test class for {@link OptionService}
 *
 * @author mautomic
 */
public class OptionServiceTest {

    @Test
    public void testOptionChainRequest() throws Exception {
        final OptionService optionService = new OptionService(TestConstants.API_KEY);

        long time = System.currentTimeMillis();
        final OptionChain optionChain = optionService.getOptionChain("SPY", "50");
        System.out.println("Retrieval for SPY options took " + (System.currentTimeMillis() - time) + " ms");

        long time2 = System.currentTimeMillis();
        final OptionChain optionChain2 = optionService.getOptionChain("QQQ", "50");
        System.out.println("Retrieval for QQQ options took " + (System.currentTimeMillis() - time2) + " ms");

        Assert.assertNotNull(optionChain);
        Assert.assertNotNull(optionChain.getCallExpDateMap());
        Assert.assertNotNull(optionChain.getPutExpDateMap());

        Assert.assertEquals("SPY", optionChain.getSymbol());

        Assert.assertFalse(optionChain.getCallExpDateMap().isEmpty());
        Assert.assertFalse(optionChain.getPutExpDateMap().isEmpty());
    }

    @Test
    public void testOptionChainRequestForStrikeAndDate() throws Exception {

        final OptionService optionService = new OptionService(TestConstants.API_KEY);
        final OptionChain optionChain = optionService.getOptionChainForStrikeAndDate("TLT", "155.5", "2020-12-18");

        Assert.assertNotNull(optionChain);
        Assert.assertNotNull(optionChain.getCallExpDateMap());
        Assert.assertNotNull(optionChain.getPutExpDateMap());

        Map<String, Map<String, List<Option>>> map = optionChain.getCallExpDateMap();
        Option option = map.get("2020-12-18:14").get("155.5").get(0);
        Assert.assertEquals("TLT_121820C155.5", option.getSymbol());
    }

    @Test
    public void testContinuousOptionScanningPerformance() throws Exception {
        final OptionService optionService = new OptionService(TestConstants.API_KEY);

        final String[] tickers = {"QQQ", "SPY", "IWM", "$VIX.X", "$SPX.X", "MSFT", "AAPL", "NFLX", "FB", "TSLA",
                "NVDA", "BYND", "TLT", "SPCE", "XLF"};

        final long startTime = System.currentTimeMillis();
        for (int j = 0; j < 3; j++) {

            List<CompletableFuture<OptionChain>> futures = new ArrayList<>();
            long time = System.currentTimeMillis();
            for (int i = 0; i < tickers.length; i++) {
                futures.add(optionService.getOptionChainAsync(tickers[i], "100"));
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(5, TimeUnit.SECONDS);
            System.out.println("Retrieval for " + Arrays.toString(tickers) + "  took " + (System.currentTimeMillis() - time) + " ms");
        }
        System.out.println("Took total of " + (System.currentTimeMillis() - startTime) + " ms");
    }
}
