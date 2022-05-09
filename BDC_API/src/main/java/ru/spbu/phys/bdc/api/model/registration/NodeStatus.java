package ru.spbu.phys.bdc.api.model.registration;

import java.time.Instant;


public record NodeStatus(String nodeName, Status status, Instant timestamp) {
}
