package project;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class MainRouteBuilder extends RouteBuilder {

    @Autowired
    private MainConfig config;

    @Override
    public void configure() throws Exception {
        restConfiguration()
                .enableCORS(false)
                // enable CORS in the api-doc as well so the swagger UI can view it
                .apiProperty("cors", "false");

        fromF("jetty:http://0.0.0.0:%d/holidays/?matchOnUriPrefix=true", config.getPort())
                .setHeader("Access-Control-Allow-Origin", constant("*"))
                .setHeader("Access-Control-Allow-Headers", constant("access-control-allow-methods,access-control-allow-origin,authorization,content-type"))
                .setHeader("Access-Control-Allow-Methods", constant("GET, DELETE, POST, OPTIONS, PUT"))
                .setHeader("country").header(Exchange.HTTP_PATH)
                .choice().when(exchange -> Paths.get(String.format("cache/%s.json", exchange.getIn().getHeader("country", String.class))).toFile().exists())
                .setBody(exchange -> Paths.get(String.format("cache/%s.json", exchange.getIn().getHeader("country", String.class))).toFile())
                .convertBodyTo(String.class)
                .otherwise()
                    .removeHeaders("*", "country")
                .setHeader(Exchange.HTTP_METHOD).constant("GET")
                .setHeader("Access-Control-Allow-Origin", constant("*"))
                .setHeader("Access-Control-Allow-Headers", constant("access-control-allow-methods,access-control-allow-origin,authorization,content-type"))
                .setHeader("Access-Control-Allow-Methods", constant("GET, DELETE, POST, OPTIONS, PUT"))
                .setBody().constant("")
                .setHeader(Exchange.HTTP_PATH, simple("/${header.country}"))
                .to("https://date.nager.at/api/v2/publicholidays/2022")
                .convertBodyTo(String.class)
                .unmarshal().json(JsonLibrary.Jackson)
                .setHeader("id", () -> UUID.randomUUID().toString())
                .setBody(exchange -> {
                    List<Object> items = exchange.getIn().getBody(List.class);
                    Map<String, Integer> holidayMonths = new HashMap<>();

                    for (int i = 0; i < items.size(); i++) {
                        Map<String, String> holiday = (Map<String, String>) items.get(i);
                        String month = holiday.get("date").substring(5,7);
                        if (holidayMonths.containsKey(month)) {
                            holidayMonths.put(month, holidayMonths.get(month) + 1);
                        } else {
                            holidayMonths.put(month, 1);
                        }
                    }
                    int max = 0;
                    String month = "";
                    for (Map.Entry<String,Integer> entry : holidayMonths.entrySet()) {
                        if (entry.getValue() > max) {
                            max = entry.getValue();
                            month = entry.getKey();
                        }
                    }

                    Result result = new Result(items.size(), month);
                    System.out.println(result);

                    return result;
                }).marshal().json(JsonLibrary.Jackson)
                    .process(exchange -> Files.write(Paths.get(String.format("cache/%s.json",exchange.getIn().getHeader("country",String.class))),
                            exchange.getIn().getBody(String.class).getBytes()))
                .end();
    }
}
