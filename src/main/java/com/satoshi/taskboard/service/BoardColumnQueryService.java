package com.satoshi.taskboard.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import com.satoshi.taskboard.persistence.dao.BoardColumnDAO;
import com.satoshi.taskboard.persistence.entity.BoardColumnEntity;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class BoardColumnQueryService {

    private final Connection connection;

    public Optional<BoardColumnEntity> findById(final Long id) throws SQLException {
        var dao = new BoardColumnDAO(connection);
        return dao.findById(id);
    }
}