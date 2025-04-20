package com.satoshi.taskboard.dto;

import com.satoshi.taskboard.persistence.entity.BoardColumnKindEnum;

public record BoardColumnInfoDTO(Long id, int order, BoardColumnKindEnum kind) {
}
