package com.satoshi.taskboard.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.satoshi.taskboard.dto.BoardColumnInfoDTO;
import com.satoshi.taskboard.exception.CardBlockedException;
import com.satoshi.taskboard.exception.CardFinishedException;
import com.satoshi.taskboard.exception.EntityNotFoundException;
import com.satoshi.taskboard.persistence.dao.CardDAO;
import com.satoshi.taskboard.persistence.entity.CardEntity;

import lombok.AllArgsConstructor;

import static com.satoshi.taskboard.persistence.entity.BoardColumnKindEnum.FINAL;

@AllArgsConstructor
public class CardService {

    private final Connection connection;

    public CardEntity create(final CardEntity entity) throws SQLException {
        try {
            var dao = new CardDAO(connection);
            dao.insert(entity);
            connection.commit();
            return entity;
        } catch (SQLException ex){
            connection.rollback();
            throw ex;
        }
    }
    
    public void moveToNextColumn(final Long cardId, final List<BoardColumnInfoDTO> boardColumnsInfo) throws SQLException{
        try{
            var dao = new CardDAO(connection);
            var optional = dao.findById(cardId);
            var dto = optional.orElseThrow(
                    () -> new EntityNotFoundException("O card de id %s não foi encontrado".formatted(cardId))
            );
            if (dto.blocked()){
                var message = "O card %s está bloqueado, é necesário desbloquea-lo para mover".formatted(cardId);
                throw new CardBlockedException(message);
            }
            var currentColumn = boardColumnsInfo.stream()
                    .filter(bc -> bc.id().equals(dto.columnId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("O card informado pertence a outro board"));
            if (currentColumn.kind().equals(FINAL)){
                throw new CardFinishedException("O card já foi finalizado");
            }
            var nextColumn = boardColumnsInfo.stream()
                    .filter(bc -> bc.order() == currentColumn.order() + 1)
                    .findFirst().orElseThrow(() -> new IllegalStateException("O card está cancelado"));
            dao.moveToColumn(nextColumn.id(), cardId);
            connection.commit();
        }catch (SQLException ex){
            connection.rollback();
            throw ex;
        }
    }
    
	public void cancel(final Long cardId, final Long cancelColumnId, final List<BoardColumnInfoDTO> boardColumnsInfo)
			throws SQLException {
		try {
			var dao = new CardDAO(connection);
			var optional = dao.findById(cardId);
			var dto = optional.orElseThrow(
					() -> new EntityNotFoundException("O card de id %s não foi encontrado".formatted(cardId)));
			if (dto.blocked()) {
				var message = "O card %s está bloqueado, é necesário desbloquea-lo para mover".formatted(cardId);
				throw new CardBlockedException(message);
			}
			var currentColumn = boardColumnsInfo.stream().filter(bc -> bc.id().equals(dto.columnId())).findFirst()
					.orElseThrow(() -> new IllegalStateException("O card informado pertence a outro board"));
			if (currentColumn.kind().equals(FINAL)) {
				throw new CardFinishedException("O card já foi finalizado");
			}
			boardColumnsInfo.stream().filter(bc -> bc.order() == currentColumn.order() + 1).findFirst()
					.orElseThrow(() -> new IllegalStateException("O card está cancelado"));
			dao.moveToColumn(cancelColumnId, cardId);
			connection.commit();
		} catch (SQLException ex) {
			connection.rollback();
			throw ex;
		}
	}
}