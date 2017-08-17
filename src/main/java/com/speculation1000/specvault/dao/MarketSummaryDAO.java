package com.speculation1000.specvault.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.logging.Level;

import com.speculation1000.specvault.db.DbConnection;
import com.speculation1000.specvault.db.DbConnectionEnum;
import com.speculation1000.specvault.db.QueryTable;
import com.speculation1000.specvault.log.SpecDbLogger;
import com.speculation1000.specvault.market.Market;
import com.speculation1000.specvault.time.SpecVaultDate;

public class MarketSummaryDAO {
	
	private static final SpecDbLogger specLogger = SpecDbLogger.getSpecDbLogger();
	
	public static List<Market> getLongEntries(DbConnectionEnum dbce){
		String sqlCommand = "SELECT DISTINCT * FROM markets WHERE date > " + SpecVaultDate.getTodayMidnightEpochSeconds(Instant.now())
				+ " AND Exchange = 'POLO' ORDER BY Volume DESC LIMIT 100";
		Connection conn = DbConnection.connect(dbce);
		List<Market> marketList = QueryTable.genericMarketQuery(conn, sqlCommand);
		try{
			conn.close();
		}catch(SQLException e){
			while (e != null) {
            	specLogger.logp(Level.INFO, QueryTable.class.getName(), "getLongEntries", e.getMessage());
	            e = e.getNextException();
	        }
		}
		return marketList;
	}
	
	public static void main(String[] args){
		//THIS!!!
		Instant now = Instant.now();
		List<Market> marketList = getLongEntries(DbConnectionEnum.H2_TEST);
		Instant end = Instant.now();
		for(Market market : marketList){
			System.out.println(market.toString());
		}
		System.out.println("total time taken: " + String.valueOf(end.getEpochSecond() - now.getEpochSecond()));
	}

}