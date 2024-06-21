package examples.um_futures.websocket;

import com.binance.connector.futures.client.impl.UMWebsocketClientImpl;

public final class KlineStream {
    private KlineStream() {
    }

    public static void main(String[] args) {
        UMWebsocketClientImpl client = new UMWebsocketClientImpl();
        client.klineStream("btcusdt", "1m", ((event) -> {
            System.out.println(event);
            //client.closeAllConnections();
        }));
    }
}
