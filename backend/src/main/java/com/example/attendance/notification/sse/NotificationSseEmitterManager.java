package com.example.attendance.notification.sse;

import com.example.attendance.notification.dto.NotificationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
public class NotificationSseEmitterManager {

    private final ConcurrentHashMap<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    private static final long SSE_TIMEOUT = 30 * 60 * 1000L;

    public SseEmitter createEmitter(Long recipientId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        List<SseEmitter> recipientEmitters = emitters.computeIfAbsent(
                recipientId, k -> new CopyOnWriteArrayList<>());
        recipientEmitters.add(emitter);

        emitter.onCompletion(() -> removeEmitter(recipientId, emitter));
        emitter.onTimeout(() -> removeEmitter(recipientId, emitter));
        emitter.onError(e -> removeEmitter(recipientId, emitter));

        return emitter;
    }

    public void send(Long recipientId, NotificationResponse notification) {
        List<SseEmitter> recipientEmitters = emitters.get(recipientId);
        if (recipientEmitters == null || recipientEmitters.isEmpty()) {
            return;
        }

        List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();
        for (SseEmitter emitter : recipientEmitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(notification));
            } catch (IOException e) {
                deadEmitters.add(emitter);
            }
        }
        recipientEmitters.removeAll(deadEmitters);
    }

    private void removeEmitter(Long recipientId, SseEmitter emitter) {
        List<SseEmitter> recipientEmitters = emitters.get(recipientId);
        if (recipientEmitters != null) {
            recipientEmitters.remove(emitter);
            if (recipientEmitters.isEmpty()) {
                emitters.remove(recipientId);
            }
        }
    }
}
