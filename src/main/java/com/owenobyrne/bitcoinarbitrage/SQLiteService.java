package com.owenobyrne.bitcoinarbitrage;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.owenobyrne.bitcoinarbitrage.model.Prices;


@Service
public class SQLiteService {
	private static Logger log = Logger.getLogger(SQLiteService.class);

	Connection connection = null;

	@PreDestroy
	public void cleanUp() {
		try {
			if (connection != null)
				connection.close();
		} catch (SQLException e) {
			// connection close failed.
			System.err.println(e);
		}
	}

	// better than using a constructor as a constructor might get called twice.
	@PostConstruct
	public void setup() {
		log.info("Loading SQLite Database...");
		try {
			Class.forName("org.sqlite.JDBC");
			// create a database connection
			connection = DriverManager
					.getConnection("jdbc:sqlite:/owen/bitcoinarbitrage.sqlite");

			Statement statement = connection.createStatement();
			statement.setQueryTimeout(5); // set timeout to 30 sec.

			// statement.executeUpdate("drop table if exists transactions");
			statement
				.executeUpdate("CREATE  TABLE  IF NOT EXISTS prices (id INTEGER PRIMARY KEY AUTOINCREMENT, timestamp DATETIME NOT NULL  DEFAULT CURRENT_TIMESTAMP, mtGoxBidEUR NUMERIC, mtGoxAskEUR NUMERIC, bitcoinCentralBidEUR NUMERIC, bitcoinCentralAskEUR NUMERIC, btceBidEUR NUMERIC, btceAskEUR NUMERIC)");
			
			//statement
				//.executeUpdate("create table if not exists regular_transactions (id INTEGER PRIMARY KEY AUTOINCREMENT, account string, narrative string, amount string, isdr integer, cron string, next_date tdate)");

			
		} catch (SQLException e) {
			// if the error message is "out of memory",
			// it probably means no database file is found
			log.error(e.getMessage(), e);
		} catch (ClassNotFoundException e) {
			log.error(e.getMessage(), e);
		}
	}

	
	public void addNewPriceRecord(HashMap<String, BigDecimal> prices) {
		PreparedStatement insertTransaction = null;

		String insertString = "insert into prices " + 
			"(mtGoxBidEUR, mtGoxAskEUR, btceBidEUR, btceAskEUR)" + 
			" values " + 
			"(?, ?, ?, ?)";

		try {
			insertTransaction = connection.prepareStatement(insertString);
			insertTransaction.setFloat(1, prices.get("mtGoxBidEUR").floatValue());
			insertTransaction.setFloat(2, prices.get("mtGoxAskEUR").floatValue());
			insertTransaction.setFloat(3, prices.get("btceBidEUR").floatValue());
			insertTransaction.setFloat(4, prices.get("btceAskEUR").floatValue());
			insertTransaction.executeUpdate();

		} catch (SQLException e) {
			log.error(e.getMessage(), e);
		}

	}

	/*
	public Vector<AIBAMTransaction> findTransactionInDatabase(Transaction t) {

		String SQL = "select * from transactions where narrative = ? and amount = ?";
		Vector<AIBAMTransaction> matchedTransactions = new Vector<AIBAMTransaction>();

		try {
			PreparedStatement preparedStatement = connection
					.prepareStatement(SQL);
			preparedStatement.setString(1, t.getNarrative());
			preparedStatement.setString(2, t.getAmount());

			ResultSet rs = preparedStatement.executeQuery();
			if (rs.next()) {
				do {
					// read the result set
					long tdate = rs.getLong("tdate");
					String subnarrative = rs.getString("subnarrative");

					if (t.getTransDate() == tdate * 1000
							&& t.getSubNarrative().equalsIgnoreCase(
									subnarrative)) {
						log.debug("Transaction matched: " + t.getNarrative()
								+ ": api date = " + t.getTransDate()
								+ ", DB date = " + rs.getLong("tdate"));
						
						AIBAMTransaction at = new AIBAMTransaction();
						at.setTransaction(new Transaction()
							.setId(rs.getInt("id"))
							.setNarrative(rs.getString("narrative"))
							.setSubNarrative(rs.getString("subnarrative"))
							.setAmount(rs.getString("amount"))
							.setAccount(rs.getString("account"))
							.setIsDR(rs.getBoolean("isdr"))
							.setTransDate(tdate * 1000));
						
						at.setRegularTransaction(getRegularTransaction(rs.getInt("regular_transaction_id")));
						
						matchedTransactions.add(at);

					} else {
						log.debug("Transaction nearly matched: "
								+ t.getNarrative() + ": api date = "
								+ t.getTransDate() + ", DB date = "
								+ rs.getLong("tdate"));
					}

				} while (rs.next());
			} else {
				log.debug("Transaction not matched: " + t.getNarrative()
						+ ": api date = " + t.getTransDate());

			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return matchedTransactions;
	}
	*/
	
	public ArrayList<Prices> getPrices() {
		String SQL = "select * from prices order by id desc limit 0,20";
		ArrayList<Prices> prices = new ArrayList<Prices>();
		
		try {
			PreparedStatement preparedStatement = connection
					.prepareStatement(SQL);
			//preparedStatement.setInt(1, id);
			
			ResultSet rs = preparedStatement.executeQuery();
			while (rs.next()) {
				prices.add(new Prices()
					.setMtGoxBidEUR(new BigDecimal(rs.getFloat("mtGoxBidEUR")))
					.setMtGoxAskEUR(new BigDecimal(rs.getFloat("mtGoxAskEUR")))
					.setBtceBidEUR(new BigDecimal(rs.getFloat("btceBidEUR")))
					.setBtceAskEUR(new BigDecimal(rs.getFloat("btceAskEUR")))
				);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return prices;
	}

	public Prices getLastPrices() {
		String SQL = "select * from prices order by id desc limit 0,1";
		
		try {
			PreparedStatement preparedStatement = connection
					.prepareStatement(SQL);
			//preparedStatement.setInt(1, id);
			
			ResultSet rs = preparedStatement.executeQuery();
			if (rs.next()) {
				return new Prices()
					.setMtGoxBidEUR(new BigDecimal(rs.getFloat("mtGoxBidEUR")))
					.setMtGoxAskEUR(new BigDecimal(rs.getFloat("mtGoxAskEUR")))
					.setBtceBidEUR(new BigDecimal(rs.getFloat("btceBidEUR")))
					.setBtceAskEUR(new BigDecimal(rs.getFloat("btceAskEUR")))
				;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

}
