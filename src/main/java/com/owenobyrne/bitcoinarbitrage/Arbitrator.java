package com.owenobyrne.bitcoinarbitrage;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mchange.v2.sql.SqlUtils;
import com.owenobyrne.bitcoincentral.BitcoinCentral;
import com.owenobyrne.bitcoincentral.BitcoinCentralException;
import com.owenobyrne.bitcoincentral.api.model.Balances;
import com.owenobyrne.bitcoincentral.api.model.BitcoinCentralTrade;
import com.owenobyrne.bitcoincentral.api.model.MarketDepth;
import com.owenobyrne.mtgox.MtGox;
import com.owenobyrne.mtgox.MtGoxException;
import com.owenobyrne.mtgox.api.model.Info;
import com.owenobyrne.mtgox.api.model.Quote;
import com.owenobyrne.mtgox.api.model.TradeResult;

@Component
public class Arbitrator {
	static final Logger logger = Logger.getLogger(Arbitrator.class.getName());
	BigDecimal mtGoxCommissionRate = new BigDecimal(0.006, new MathContext(3));
	BigDecimal bitcoinCentralCommissionRate = new BigDecimal(0.00498,
			new MathContext(5));

	@Autowired
	SQLiteService sqlite;

	@Autowired
	MtGox mtgox;

	@Autowired
	BitcoinCentral bitcoincentral;

	public void checkArbitragePossibility() {
		logger.info("checking...");
		
		BigDecimal totalCommission = mtGoxCommissionRate
				.add(bitcoinCentralCommissionRate);
		
		BigDecimal mtGoxBTCBalance = new BigDecimal(0);
		BigDecimal mtGoxEURBalance = new BigDecimal(0);
		BigDecimal bitcoinCentralBTCBalance = new BigDecimal(0);
		BigDecimal bitcoinCentralEURBalance = new BigDecimal(0);
		
		BigDecimal bitcoinCentralBidEUR = new BigDecimal(0);
		BigDecimal bitcoinCentralAskEUR = new BigDecimal(0);
		BigDecimal mtGoxAskEUR = new BigDecimal(0);
		BigDecimal mtGoxBidEUR = new BigDecimal(0);
		
		Info mtGoxInfo = null;

		try {
			mtGoxInfo = mtgox.getInfo();
			
			mtGoxBTCBalance = mtGoxInfo.getData().getWallets()
				.get("BTC").getBalance().getValue();
			mtGoxEURBalance = mtGoxInfo.getData().getWallets()
				.get("EUR").getBalance().getValue();
			
		} catch (MtGoxException e) {
			e.printStackTrace();
			
		}
		
		Quote mtGoxAskQuote = null;
		Quote mtGoxBidQuote = null;
		try {
			mtGoxAskQuote = mtgox.getQuote("ask");
			mtGoxBidQuote = mtgox.getQuote("bid");

			mtGoxAskEUR = (mtGoxAskQuote.getData().getAmount())
				.scaleByPowerOfTen(-5);
			mtGoxBidEUR = (mtGoxBidQuote.getData().getAmount())
				.scaleByPowerOfTen(-5);

		} catch (MtGoxException e) {
			e.printStackTrace();
			//continue anyway
		}
			
		Balances bitcoinCentralBalances = null;
		try {
			bitcoinCentralBalances = bitcoincentral.getBalances();
		
			bitcoinCentralBTCBalance = bitcoinCentralBalances.getBtc();
			bitcoinCentralEURBalance = bitcoinCentralBalances.getEur();
		
		} catch (BitcoinCentralException e) {
			e.printStackTrace();
		}

		MarketDepth bitcoinCentralMarketDepth = null;
		try {
			bitcoinCentralMarketDepth = bitcoincentral.getMarketDepth();
			bitcoinCentralBidEUR = bitcoinCentralMarketDepth.getBids()[0].getPrice();
			bitcoinCentralAskEUR = bitcoinCentralMarketDepth.getAsks()[0].getPrice();
		
		} catch (BitcoinCentralException e) {
			e.printStackTrace();
			//continue
		}
		
		HashMap<String, BigDecimal> prices = new HashMap<String, BigDecimal>();
		prices.put("mtGoxAskEUR", mtGoxAskEUR);
		prices.put("mtGoxBidEUR", mtGoxBidEUR);
		prices.put("bitcoinCentralBidEUR", bitcoinCentralBidEUR);
		prices.put("bitcoinCentralAskEUR", bitcoinCentralAskEUR);
		sqlite.addNewPriceRecord(prices);

		BigDecimal mtGox2BitcoinCentralDifference = bitcoinCentralBidEUR
				.subtract(mtGoxAskEUR);
		BigDecimal bitcoinCentral2MtGoxDifference = mtGoxBidEUR
				.subtract(bitcoinCentralAskEUR);

		BigDecimal amountOfBTCToTrade = new BigDecimal(0.01);

		if (mtGox2BitcoinCentralDifference.divide(mtGoxAskEUR,
				RoundingMode.HALF_UP).compareTo(totalCommission) > 0) {
			logger.info("MtGox -> BitcoinCentral Difference is greater than "
					+ totalCommission
					+ "! "
					+ mtGox2BitcoinCentralDifference.divide(mtGoxAskEUR,
							RoundingMode.HALF_UP));

			if (mtGoxEURBalance.compareTo(amountOfBTCToTrade
					.multiply(mtGoxBidEUR)) > 0
					&& bitcoinCentralBTCBalance
							.compareTo(amountOfBTCToTrade) > 0) {
				// Order mtGoxOrder = mtgox.trade("bid",
				// amountOfBTCToTrade.scaleByPowerOfTen(8).toBigInteger());
				// BitcoinCentralOrder bitcoinCentralOrder =
				// bitcoincentral.trade("sell", amountOfBTCToTrade.round(new
				// MathContext(4)));
			} else {
				logger.info("MtGox -> BitcoinCentral Insufficent funds to trade.");

			}
		} else {
			logger.info("MtGox -> BitcoinCentral Difference is less than "
					+ totalCommission
					+ "! "
					+ mtGox2BitcoinCentralDifference.divide(mtGoxAskEUR,
							RoundingMode.HALF_UP));
		}

		if (bitcoinCentral2MtGoxDifference.divide(bitcoinCentralAskEUR,
				RoundingMode.HALF_UP).compareTo(totalCommission) > 0) {
			logger.info("BitcoinCentral -> MtGox Difference is greater than "
					+ totalCommission
					+ "! "
					+ bitcoinCentral2MtGoxDifference.divide(mtGoxAskEUR,
							RoundingMode.HALF_UP));

			if (bitcoinCentralEURBalance.compareTo(amountOfBTCToTrade
					.multiply(bitcoinCentralBidEUR)) > 0
					&& mtGoxBTCBalance.compareTo(amountOfBTCToTrade) > 0) {
				// BitcoinCentralOrder bitcoinCentralOrder =
				// bitcoincentral.trade("buy", amountOfBTCToTrade.round(new
				// MathContext(4)));
				// Order mtGoxOrder = mtgox.trade("ask",
				// amountOfBTCToTrade.scaleByPowerOfTen(8).toBigInteger());
			} else {
				logger.info("BitcoinCentral -> MtGox Insufficent funds to trade.");

			}

		} else {
			logger.info("BitcoinCentral -> MtGox Difference is less than "
					+ totalCommission
					+ "! "
					+ bitcoinCentral2MtGoxDifference.divide(mtGoxAskEUR,
							RoundingMode.HALF_UP));

		}
/*
		TradeResult mtGoxTradeResult = mtgox.getTradeResult("ask",
				"3adb3f70-8e00-4105-ad56-e83a55b82db9");
		BitcoinCentralTrade bitcoinCentralTradeResult = bitcoincentral
				.getTradeResults("8630718d-b5b5-4294-83f3-00d7352339c3")[0];

		BigDecimal mtGoxEarned = mtGoxTradeResult.getData()
				.getTotal_spent().getValue();
		BigDecimal mtGoxCommission = mtGoxEarned.multiply(
				mtGoxCommissionRate, new MathContext(7));
		BigDecimal bitcoinCentralSpent = bitcoinCentralTradeResult
				.getTraded_currency();
		BigDecimal bitcoinCentralCommission = bitcoinCentralSpent.multiply(
				bitcoinCentralCommissionRate, new MathContext(7));

		logger.info("Earned: "
				+ mtGoxEarned
				+ ", Spent (with Commission): "
				+ mtGoxCommission.add(bitcoinCentralSpent).add(
						bitcoinCentralCommission) + " ("
				+ bitcoinCentralSpent + ", " + mtGoxCommission + ", "
				+ bitcoinCentralCommission + ")");
*/
	}
}
