package com.satoshi.taskboard;

import static com.satoshi.taskboard.persistence.config.ConnectionConfig.getConnection;

import java.sql.SQLException;

import com.satoshi.taskboard.persistence.migration.MigrationStrategy;
import com.satoshi.taskboard.ui.MainMenu;

public class Main {

	public static void main(String[] args) throws SQLException {
		try(var connection = getConnection()){
			new MigrationStrategy(connection).executeMigration();
		}
		 new MainMenu().execute();
	}

}