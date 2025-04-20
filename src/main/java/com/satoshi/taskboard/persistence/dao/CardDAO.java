package com.satoshi.taskboard.persistence.dao;

import java.sql.Connection;

import com.satoshi.taskboard.dto.CardDetailsDTO;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CardDAO {

    private Connection connection;

    public CardDetailsDTO findById(final Long id){
        return null;
    }
}