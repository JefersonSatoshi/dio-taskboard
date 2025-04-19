package com.satoshi.taskboard.dto;

import com.satoshi.taskboard.persistence.entity.BoardColumnKindEnum;

public record BoardColumnDTO(Long id,
        String name,
        BoardColumnKindEnum kind,
        int cardsAmount) {
	
}