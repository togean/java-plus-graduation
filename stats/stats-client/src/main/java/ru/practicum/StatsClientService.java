package ru.practicum;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StatsClientService{
    private final StatsClient statsClient;
    private final String appName;

    @Autowired
    public StatsClientService(StatsClient statsClient,
                              @Value("${spring.application.name}") String appName) {
        this.statsClient = statsClient;
        this.appName = appName;
    }

    public ResponseHitDto createHit(CreateHitDto createHitDto) {
        createHitDto.setApp(appName);
        return statsClient.createHit(createHitDto);
    }

    public List<ResponseStatsDto> getStats(String start, String end, List<String> uris, Boolean unique) {
        return statsClient.getStats(start, end, uris, unique);
    }
}
