package com.backend.jobstream.service;

import com.backend.jobstream.entity.Analysis;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class SseService implements EventBroadcaster {

    private static final long SSE_TIMEOUT = 30 * 60 * 1000L; // 30 minutes
    private static final int MAX_EMITTERS = 100;

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {
        if (emitters.size() >= MAX_EMITTERS) {
            throw new IllegalStateException("Maximum number of SSE connections reached");
        }
        
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));

        return emitter;
    }

    @Override
    public void broadcast(Analysis analysis) {
        // Convert Analysis entity to proper JSON format for frontend
        Map<String, Object> data = new HashMap<>();
        data.put("id", analysis.getId().toString());

        // Get savedJobId from the related SavedJob
        if (analysis.getSavedJob() != null) {
            data.put("savedJobId", analysis.getSavedJob().getId().toString());
            data.put("jobId", analysis.getSavedJob().getExternalId());
        }

        data.put("score", analysis.getScore());
        data.put("summary", analysis.getSummary());
        data.put("strengths", analysis.getStrengths());
        data.put("weaknesses", analysis.getWeaknesses());
        data.put("recommendations", analysis.getRecommendations());
        data.put("tags", analysis.getTags());
        data.put("errorMessage", analysis.getErrorMessage());

        if (analysis.getCreatedAt() != null) {
            data.put("createdAt", analysis.getCreatedAt().atOffset(ZoneOffset.UTC).toString());
        }
        if (analysis.getCompletedAt() != null) {
            data.put("completedAt", analysis.getCompletedAt().atOffset(ZoneOffset.UTC).toString());
        }

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("analysis-completed")
                        .data(data));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }
}