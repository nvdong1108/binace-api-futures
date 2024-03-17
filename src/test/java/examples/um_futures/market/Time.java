package examples.um_futures.market;

import com.binance.connector.futures.client.exceptions.BinanceClientException;
import com.binance.connector.futures.client.exceptions.BinanceConnectorException;
import com.binance.connector.futures.client.impl.UMFuturesClientImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Time {
    private Time() {
    }

    private static final Logger logger = LoggerFactory.getLogger(Time.class);
    public static void main(String[] args) {

        UMFuturesClientImpl client = new UMFuturesClientImpl();

        try {
            String result = client.market().time();
            logger.info(result);
        } catch (BinanceConnectorException e) {
            logger.error("fullErrMessage: {}", e.getMessage(), e);
        } catch (BinanceClientException e) {
            logger.error("fullErrMessage: {} \nerrMessage: {} \nerrCode: {} \nHTTPStatusCode: {}",
                    e.getMessage(), e.getErrMsg(), e.getErrorCode(), e.getHttpStatusCode(), e);
        }
    }
}