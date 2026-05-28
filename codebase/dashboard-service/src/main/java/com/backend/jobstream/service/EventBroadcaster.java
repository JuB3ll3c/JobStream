package com.backend.jobstream.service;

import com.backend.jobstream.entity.Analysis;

/**
 * Interface for broadcasting events to connected clients via SSE.
 * Allows for different broadcasting implementations.
 */
public interface EventBroadcaster {

    /**
     * Broadcasts an analysis result to all connected clients
     *
     * @param analysis the analysis entity to broadcast
     */
    void broadcast(Analysis analysis);
}
