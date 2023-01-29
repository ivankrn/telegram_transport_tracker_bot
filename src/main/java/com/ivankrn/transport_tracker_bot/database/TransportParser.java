package com.ivankrn.transport_tracker_bot.database;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class TransportParser {

    private final WebClient webClient;
    private static final ObjectMapper mapper = new ObjectMapper();
    private final static String apiUrl = "https://your-bus.ru/api/v1";

    public TransportParser(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(apiUrl).build();
    }

    public List<StopPrediction> getStopPredictionsById(int stopId) {
        List<StopPrediction> predictions = new ArrayList<>();
        String response = webClient.get()
                .uri("/station/predictionSite/" + stopId)
                .retrieve()
                .bodyToMono(String.class).block();
        JsonNode json = getJson(response);
        JsonNode dataNode = json.get("data").get("predictions");
        if (dataNode.isArray()) {
            for (JsonNode predictionNode : dataNode) {
                int transportId = predictionNode.get("id").asInt();
                int distanceToStop = predictionNode.get("prediction").get("meters").asInt();
                int route = getRouteNumberById(transportId);
                int stopsCount = predictionNode.get("prediction").get("stationCount").asInt();
                StopPrediction prediction = new StopPrediction(stopId, transportId, route, distanceToStop, stopsCount);
                predictions.add(prediction);
            }
        }
        return predictions;
    }

    private int getRouteNumberById(int id) {
        String response = webClient.get()
                .uri("/route/getsummary/" + id)
                .retrieve()
                .bodyToMono(String.class).block();
        JsonNode json = getJson(response);
        return json.get("data").get("name").asInt();
    }

    private JsonNode getJson(String jsonString) {
        JsonNode json = null;
        try {
            json = mapper.readTree(jsonString);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
        return json;
    }

    public List<Stop> getAllStops() {
        String baseUrl = "https://your-bus.ru";
        List<Stop> stops = new ArrayList<>();
        try {
            int pageNumber = 1;
            stops.addAll(getStopsByPageId(pageNumber));
            Thread.sleep(200);
            Document doc = Jsoup.connect(baseUrl + "/ekaterinburg/stations/page/" + pageNumber).get();
            List<String> pages = doc.select(".pagination .page-item a.page-link").eachAttr("aria-label");
            while (pages.contains(">")) {
                pageNumber++;
                Thread.sleep(200);
                List<Stop> stopsPerPage = getStopsByPageId(pageNumber);
                stops.addAll(stopsPerPage);
                Thread.sleep(200);
                doc = Jsoup.connect(baseUrl + "/ekaterinburg/stations/page/" + pageNumber).get();
                pages = doc.select(".pagination .page-item a.page-link").eachAttr("aria-label");
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return stops;
    }

    public List<Stop> getStopsByPageId(int pageId) {
        String baseUrl = "https://your-bus.ru";
        List<Stop> stops = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(baseUrl + "/ekaterinburg/stations/page/" + pageId).get();
            doc.select(".container a.row").forEach(stop -> {
                String name = stop.select("h3").text();
                String to = stop.select("p").text();
                name = name + " (на " + to + ")";
                try {
                    String stopUrl = stop.attr("href");
                    long stopId = Long.parseLong(stopUrl.split("=")[1]);
                    Document stopDoc = Jsoup.connect(baseUrl + stopUrl).get();
                    Stop newStop = new Stop();
                    newStop.setId(stopId);
                    newStop.setName(name);
                    if (stopDoc.select(".yicon").hasClass("ybus-bus")
                            || stopDoc.select(".yicon").hasClass("ybus-troll")) {
                        newStop.setType(Stop.Type.BUS);
                        stops.add(newStop);
                    } else if (stopDoc.select(".yicon").hasClass("ybus-tram")) {
                        newStop.setType(Stop.Type.TRAM);
                        stops.add(newStop);
                    } else {
                        log.error("Couldn't parse stop type for stop id: {}", stopId);
                    }
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
            });
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return stops;
    }
}
