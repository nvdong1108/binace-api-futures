package examples.um_futures.portfoliomargin;

import com.binance.connector.futures.client.impl.UMFuturesClientImpl;
import examples.PrivateConfig;
import java.util.LinkedHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PortfolioMarginAccountInfo {
    private PortfolioMarginAccountInfo() {
    }

    private static final Logger logger = LoggerFactory.getLogger(PortfolioMarginAccountInfo.class);
    public static void main(String[] args) {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();

        UMFuturesClientImpl client = new UMFuturesClientImpl(PrivateConfig.TESTNET_API_KEY, PrivateConfig.TESTNET_SECRET_KEY, PrivateConfig.TESTNET_BASE_URL);

        parameters.put("asset", "USDT");
        String result = client.portfolioMargin().portfolioMarginAccountInfo(parameters);
        logger.info(result);
    }
}