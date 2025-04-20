package com.satoshi.taskboard.ui;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.satoshi.taskboard.persistence.entity.BoardColumnEntity;
import com.satoshi.taskboard.persistence.entity.BoardColumnKindEnum;
import com.satoshi.taskboard.persistence.entity.BoardEntity;
import com.satoshi.taskboard.service.BoardQueryService;
import com.satoshi.taskboard.service.BoardService;

import static com.satoshi.taskboard.persistence.config.ConnectionConfig.getConnection;
import static com.satoshi.taskboard.persistence.entity.BoardColumnKindEnum.*;

public class MainMenu {

    private final Scanner scanner = new Scanner(System.in).useDelimiter("\n");

    public void execute() throws SQLException {
        System.out.println("Bem vindo ao gerenciador de boards, escolha a opção desejada");
        var option = -1;
        while (true) {
            System.out.println("1 - Criar um novo board");
            System.out.println("2 - Selecionar um board existente");
            System.out.println("3 - Excluir um board");
            System.out.println("4 - Sair");

            System.out.print("Escolha: ");
            String input = scanner.nextLine();
            try {
                option = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida, digite um número.");
                continue;
            }

            switch (option) {
                case 1 -> createBoard();
                case 2 -> selectBoard();
                case 3 -> deleteBoard();
                case 4 -> System.exit(0);
                default -> System.out.println("Opção inválida, informe uma opção do menu");
            }
        }
    }

    private void createBoard() throws SQLException {
        var entity = new BoardEntity();

        System.out.println("Informe o nome do seu board:");
        var name = scanner.nextLine();
        entity.setName(name);

        System.out.println("Seu board terá colunas além das 3 padrões? Se sim, informe quantas. Se não, digite '0':");
        int additionalColumns = 0;
        try {
            additionalColumns = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Valor inválido. Considerando 0 colunas adicionais.");
        }

        List<BoardColumnEntity> columns = new ArrayList<>();

        System.out.println("Informe o nome da coluna inicial do board:");
        var initialColumnName = scanner.nextLine();
        columns.add(createColumn(initialColumnName, INITIAL, 0));

        for (int i = 0; i < additionalColumns; i++) {
            System.out.printf("Informe o nome da coluna de tarefa pendente %d:\n", i + 1);
            var pendingColumnName = scanner.nextLine();
            columns.add(createColumn(pendingColumnName, PENDING, i + 1));
        }

        System.out.println("Informe o nome da coluna final:");
        var finalColumnName = scanner.nextLine();
        columns.add(createColumn(finalColumnName, FINAL, additionalColumns + 1));

        System.out.println("Informe o nome da coluna de cancelamento:");
        var cancelColumnName = scanner.nextLine();
        columns.add(createColumn(cancelColumnName, CANCEL, additionalColumns + 2));

        entity.setBoardColumns(columns);

        try (var connection = getConnection()) {
            var service = new BoardService(connection);
            service.insert(entity);
        }
    }

    private void selectBoard() throws SQLException {
        System.out.println("Informe o ID do board que deseja selecionar:");
        long id;
        try {
            id = Long.parseLong(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("ID inválido.");
            return;
        }

        try (var connection = getConnection()) {
            var queryService = new BoardQueryService(connection);
            var optional = queryService.findById(id);
            optional.ifPresentOrElse(
                b -> {
                    try {
                        new BoardMenu(b).execute();
                    } catch (Exception e) {
                        System.out.println("Erro ao abrir o menu do board.");
                        e.printStackTrace();
                    }
                },
                () -> System.out.printf("Não foi encontrado um board com ID %s\n", id)
            );
        }
    }

    private void deleteBoard() throws SQLException {
        System.out.println("Informe o ID do board que será excluído:");
        long id;
        try {
            id = Long.parseLong(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("ID inválido.");
            return;
        }

        try (var connection = getConnection()) {
            var service = new BoardService(connection);
            if (service.delete(id)) {
                System.out.printf("O board %s foi excluído com sucesso.\n", id);
            } else {
                System.out.printf("Não foi encontrado um board com ID %s\n", id);
            }
        }
    }

    private BoardColumnEntity createColumn(final String name, final BoardColumnKindEnum kind, final int order) {
        var boardColumn = new BoardColumnEntity();
        boardColumn.setName(name);
        boardColumn.setKind(kind);
        boardColumn.setOrder(order);
        return boardColumn;
    }
}
