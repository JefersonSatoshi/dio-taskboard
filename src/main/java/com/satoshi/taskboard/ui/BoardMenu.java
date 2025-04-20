package com.satoshi.taskboard.ui;

import static com.satoshi.taskboard.persistence.config.ConnectionConfig.getConnection;

import java.sql.SQLException;
import java.util.Scanner;

import com.satoshi.taskboard.dto.BoardColumnInfoDTO;
import com.satoshi.taskboard.persistence.config.ConnectionConfig;
import com.satoshi.taskboard.persistence.entity.BoardColumnEntity;
import com.satoshi.taskboard.persistence.entity.BoardEntity;
import com.satoshi.taskboard.persistence.entity.CardEntity;
import com.satoshi.taskboard.service.BoardColumnQueryService;
import com.satoshi.taskboard.service.BoardQueryService;
import com.satoshi.taskboard.service.CardQueryService;
import com.satoshi.taskboard.service.CardService;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class BoardMenu {

    private final Scanner scanner = new Scanner(System.in).useDelimiter("\n");

    private final BoardEntity entity;

    public void execute() {

        try {
            System.out.printf("Bem vindo ao board %s, selecione a operação desejada\n", entity.getId());
            var option = -1;
            while (option != 9) {
                System.out.println("1 - Criar um card");
                System.out.println("2 - Mover um card");
                System.out.println("3 - Bloquear um card");
                System.out.println("4 - Desbloquear um card");
                System.out.println("5 - Cancelar um card");
                System.out.println("6 - Ver board");
                System.out.println("7 - Ver coluna com cards");
                System.out.println("8 - Ver card");
                System.out.println("9 - Voltar para o menu anterior");
                System.out.println("10 - Sair");

                System.out.print("Escolha: ");
                String input = scanner.nextLine();
                try {
                    option = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    System.out.println("Entrada inválida, digite um número.");
                    continue;
                }
                
                switch (option) {
                    case 1 -> createCard();
                    case 2 -> moveCardToNextColumn();
                    case 3 -> blockCard();
                    case 4 -> unblockCard();
                    case 5 -> cancelCard();
                    case 6 -> showBoard();
                    case 7 -> showColumn();
                    case 8 -> showCard();
                    case 9 -> System.out.println("Voltando para o menu anterior");
                    case 10 -> System.exit(0);
                    default -> System.out.println("Opção inválida, informe uma opção do menu");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);
        }
    }

    private void createCard() throws SQLException {
        var card = new CardEntity();
        System.out.println("Informe o título do card");
        card.setTitle(scanner.next());
        System.out.println("Informe a descrição do card");
        card.setDescription(scanner.next());
        card.setBoardColumn(entity.getInitialColumn());
        try (var connection = getConnection()) {
            new CardService(connection).create(card);
        }
    }

    private void moveCardToNextColumn() throws SQLException {
        long cardId = getLongInput("Informe o id do card que deseja mover para a próxima coluna:");
        var boardColumnsInfo = entity.getBoardColumns().stream()
                .map(bc -> new BoardColumnInfoDTO(bc.getId(), bc.getOrder(), bc.getKind()))
                .toList();
        try (var connection = getConnection()) {
            new CardService(connection).moveToNextColumn(cardId, boardColumnsInfo);
        } catch (RuntimeException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void blockCard() throws SQLException {
        long cardId = getLongInput("Informe o id do card que será bloqueado");
        System.out.println("Informe o motivo do bloqueio do card");
        var reason = scanner.nextLine();  // Captura o motivo usando nextLine()
        var boardColumnsInfo = entity.getBoardColumns().stream()
                .map(bc -> new BoardColumnInfoDTO(bc.getId(), bc.getOrder(), bc.getKind()))
                .toList();

        try (var connection = getConnection()) {
            new CardService(connection).block(cardId, reason, boardColumnsInfo);
        } catch (RuntimeException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void unblockCard() throws SQLException {
        long cardId = getLongInput("Informe o id do card que será desbloqueado");
        System.out.println("Informe o motivo do desbloqueio do card");
        var reason = scanner.nextLine();  // Captura o motivo usando nextLine()
        try (var connection = getConnection()) {
            new CardService(connection).unblock(cardId, reason);
        } catch (RuntimeException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void cancelCard() throws SQLException {
        long cardId = getLongInput("Informe o id do card que deseja mover para a coluna de cancelamento");
        var cancelColumn = entity.getCancelColumn();
        var boardColumnsInfo = entity.getBoardColumns().stream()
                .map(bc -> new BoardColumnInfoDTO(bc.getId(), bc.getOrder(), bc.getKind()))
                .toList();
        try (var connection = getConnection()) {
            new CardService(connection).cancel(cardId, cancelColumn.getId(), boardColumnsInfo);
        } catch (RuntimeException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void showBoard() {
        try (var connection = ConnectionConfig.getConnection()) {
            var service = new BoardQueryService(connection);
            var optional = service.showBoardDetails(entity.getId());

            optional.ifPresent(board -> {
                System.out.printf("Board [%d, %s]%n", board.id(), board.name());
                board.columns().forEach(column -> 
                        System.out.printf(" → Coluna [%s] | Tipo: [%s] | %d cards%n",
                                column.name(), column.kind(), column.cardsAmount()));
            });

        } catch (SQLException e) {
            System.err.println("Erro ao mostrar o board: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showColumn() throws SQLException {
        var columnsIds = entity.getBoardColumns().stream().map(BoardColumnEntity::getId).toList();
        long selectedColumnId = getLongInput("Escolha uma coluna do board " + entity.getName() + " pelo id");

        while (!columnsIds.contains(selectedColumnId)) {
            System.out.println("Coluna inválida. Por favor, escolha um id válido.");
            selectedColumnId = getLongInput("Escolha uma coluna do board " + entity.getName() + " pelo id");
        }

        try (var connection = ConnectionConfig.getConnection()) {
            var column = new BoardColumnQueryService(connection).findById(selectedColumnId);
            column.ifPresent(co -> {
                System.out.printf("Coluna %s tipo %s\n", co.getName(), co.getKind());
                co.getCards().forEach(ca -> 
                        System.out.printf("Card %s - %s\nDescrição: %s\n", 
                                ca.getId(), ca.getTitle(), ca.getDescription()));
            });
        }
    }

    private void showCard() throws SQLException {
        long selectedCardId = getLongInput("Informe o id do card que deseja visualizar");

        try (var connection = ConnectionConfig.getConnection()) {
            new CardQueryService(connection).findById(selectedCardId)
                    .ifPresentOrElse(
                            c -> {
                                System.out.printf("Card %s - %s.\n", c.id(), c.title());
                                System.out.printf("Descrição: %s\n", c.description());
                                System.out.println(c.blocked()
                                        ? "Está bloqueado. Motivo: " + c.blockReason()
                                        : "Não está bloqueado");
                                System.out.printf("Já foi bloqueado %s vezes\n", c.blocksAmount());
                                System.out.printf("Está no momento na coluna %s - %s\n",
                                        c.columnId(), c.columnName());
                            },
                            () -> System.out.printf("Não existe um card com o id %s\n", selectedCardId));
        }
    }
    
    private long getLongInput(String prompt) {
        long value = -1;
        while (value == -1) {
            System.out.println(prompt);
            String input = scanner.nextLine();
            try {
                value = Long.parseLong(input);
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Por favor, digite um número válido.");
            }
        }
        return value;
    }
}
