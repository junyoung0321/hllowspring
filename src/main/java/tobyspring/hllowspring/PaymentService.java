package tobyspring.hllowspring;

import tools.jackson.databind.ObjectMapper;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.runtime.ObjectMethods;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.stream.Collectors;

public class PaymentService {
    public Payment prepare(Long orderId, String currency, BigDecimal foreignCurrencyAmount) throws IOException {
        //환율 가져오기
        URL url = new URL("https://open.er-api.com/v6/latest/" + currency);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String response = bufferedReader.lines().collect(Collectors.joining());
        bufferedReader.close();

        ObjectMapper mapper = new ObjectMapper();
        ExRateData data = mapper.readValue(response, ExRateData.class);
        BigDecimal exRate = data.rates().get("KRW");


        // https://open.er-api.com/v6/latest/USD
        //금액 계산
        BigDecimal convertedAmount = foreignCurrencyAmount.multiply(exRate);

        // 유효 시간 계산
        LocalDateTime validUntile = LocalDateTime.now().plusMinutes(30);

        return new Payment(orderId, currency, foreignCurrencyAmount, exRate, convertedAmount, validUntile);
    }

    public static void main(String[] args) throws IOException {
      PaymentService paymentService = new PaymentService();
      Payment payment = paymentService.prepare(100L, "USD", BigDecimal.valueOf(50.7));
      System.out.println(payment);
    }
}
