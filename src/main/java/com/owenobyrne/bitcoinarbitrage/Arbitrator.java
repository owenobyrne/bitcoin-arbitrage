package com.owenobyrne.bitcoinarbitrage;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.owenobyrne.bitcoincentral.BitcoinCentral;
import com.owenobyrne.bitcoincentral.api.model.Balances;
import com.owenobyrne.bitcoincentral.api.model.BitcoinCentralOrder;
import com.owenobyrne.bitcoincentral.api.model.BitcoinCentralTrade;
import com.owenobyrne.bitcoincentral.api.model.MarketDepth;
import com.owenobyrne.mtgox.MtGox;
import com.owenobyrne.mtgox.api.model.Info;
import com.owenobyrne.mtgox.api.model.Order;
import com.owenobyrne.mtgox.api.model.Quote;
import com.owenobyrne.mtgox.api.model.TradeResult;

@Component
public class Arbitrator {
	static final Logger logger = Logger.getLogger(Arbitrator.class.getName());
	BigDecimal mtGoxCommissionRate = new BigDecimal(0.006, new MathContext(3));
	BigDecimal bitcoinCentralCommissionRate = new BigDecimal(0.00498, new MathContext(5));
	
	@Autowired
	MtGox mtgox;
	
	@Autowired
	BitcoinCentral bitcoincentral;
	
	public void checkArbitragePossibility() {
		logger.info("checking...");
		
		BigDecimal totalCommission = mtGoxCommissionRate.add(bitcoinCentralCommissionRate);
		
		try {
		//Order o = mtgox.trade("bid", new BigDecimal(0.01).scaleByPowerOfTen(8).toBigInteger());
		//BitcoinCentralOrder bco = bitcoincentral.trade("sell", new BigDecimal(0.01).round(new MathContext(4)));
		
		Info mtGoxInfo = mtgox.getInfo();
		Quote mtGoxAskQuote = mtgox.getQuote("ask");
		Quote mtGoxBidQuote = mtgox.getQuote("bid");
		
		Balances bitcoinCentralBalances = bitcoincentral.getBalances();
		MarketDepth bitcoinCentralMarketDepth = bitcoincentral.getMarketDepth();
		
		BigDecimal mtGoxBTCBalance = mtGoxInfo.getData().getWallets().get("BTC").getBalance().getValue();
		BigDecimal mtGoxEURBalance = mtGoxInfo.getData().getWallets().get("EUR").getBalance().getValue();
		BigDecimal bitcoinCentralBTCBalance = bitcoinCentralBalances.getBtc();
		BigDecimal bitcoinCentralEURBalance = bitcoinCentralBalances.getEur();
				
		BigDecimal mtGoxAskPrice = (mtGoxAskQuote.getData().getAmount()).scaleByPowerOfTen(-5);
		BigDecimal mtGoxBidPrice = (mtGoxBidQuote.getData().getAmount()).scaleByPowerOfTen(-5);
		BigDecimal bitcoinCentralBidPrice = bitcoinCentralMarketDepth.getBids()[0].getPrice();
		BigDecimal bitcoinCentralAskPrice = bitcoinCentralMarketDepth.getAsks()[0].getPrice();
		
		logger.info("MtGox BTC Balance: " + mtGoxBTCBalance);
		logger.info("MtGox EUR Balance: " + mtGoxEURBalance);
		logger.info("BC BTC Balance: " + bitcoinCentralBTCBalance);
		logger.info("BC EUR Balance: " + bitcoinCentralEURBalance);
		logger.info("MtGox Ask Price: " + mtGoxAskPrice);
		//logger.info("MtGox EUR Value: " + btcBalance.multiply(mtGoxAskPrice));
		//logger.info("BitcoinCentral BTC Bid: " + bitcoincentralMarketDepth.getBids()[0].getAmount());
		logger.info("BitcoinCentral Bid Price: " + bitcoinCentralBidPrice);
		logger.info("BitcoinCentral Ask Price: " + bitcoinCentralAskPrice);
		logger.info("MtGox Bid Price: " + mtGoxBidPrice);
		
		BigDecimal mtGox2BitcoinCentralDifference = bitcoinCentralBidPrice.subtract(mtGoxAskPrice);
		BigDecimal bitcoinCentral2MtGoxDifference = mtGoxBidPrice.subtract(bitcoinCentralAskPrice);
		
		BigDecimal amountOfBTCToTrade = new BigDecimal(0.01);
		
		if (mtGox2BitcoinCentralDifference.divide(mtGoxAskPrice, RoundingMode.HALF_UP).compareTo(totalCommission) > 0) {
			logger.info("MtGox -> BitcoinCentral Difference is greater than " + totalCommission + "! " + mtGox2BitcoinCentralDifference.divide(mtGoxAskPrice, RoundingMode.HALF_UP));
			
			if (mtGoxEURBalance.compareTo(amountOfBTCToTrade.multiply(mtGoxBidPrice)) > 0 &&
					bitcoinCentralBTCBalance.compareTo(amountOfBTCToTrade) > 0) {
//				Order mtGoxOrder = mtgox.trade("bid", amountOfBTCToTrade.scaleByPowerOfTen(8).toBigInteger());
//				BitcoinCentralOrder bitcoinCentralOrder = bitcoincentral.trade("sell", amountOfBTCToTrade.round(new MathContext(4)));
			} else {
				logger.info("MtGox -> BitcoinCentral Insufficent funds to trade.");	
						
			}
		} else {
			logger.info("MtGox -> BitcoinCentral Difference is less than " + totalCommission + "! " + mtGox2BitcoinCentralDifference.divide(mtGoxAskPrice, RoundingMode.HALF_UP));	
		}
		
		if (bitcoinCentral2MtGoxDifference.divide(bitcoinCentralAskPrice, RoundingMode.HALF_UP).compareTo(totalCommission) > 0) {
			logger.info("BitcoinCentral -> MtGox Difference is greater than " + totalCommission + "! " + bitcoinCentral2MtGoxDifference.divide(mtGoxAskPrice, RoundingMode.HALF_UP));
			

			if (bitcoinCentralEURBalance.compareTo(amountOfBTCToTrade.multiply(bitcoinCentralBidPrice)) > 0 &&
					mtGoxBTCBalance.compareTo(amountOfBTCToTrade) > 0) {
//				BitcoinCentralOrder bitcoinCentralOrder = bitcoincentral.trade("buy", amountOfBTCToTrade.round(new MathContext(4)));
//				Order mtGoxOrder = mtgox.trade("ask", amountOfBTCToTrade.scaleByPowerOfTen(8).toBigInteger());
			} else {
				logger.info("BitcoinCentral -> MtGox Insufficent funds to trade.");	
						
			}
			
		} else {
			logger.info("BitcoinCentral -> MtGox Difference is less than " + totalCommission + "! " + bitcoinCentral2MtGoxDifference.divide(mtGoxAskPrice, RoundingMode.HALF_UP));	
			
		}
		
		TradeResult mtGoxTradeResult = mtgox.getTradeResult("ask", "3adb3f70-8e00-4105-ad56-e83a55b82db9");
		BitcoinCentralTrade bitcoinCentralTradeResult = bitcoincentral.getTradeResults("8630718d-b5b5-4294-83f3-00d7352339c3")[0];
		
		BigDecimal mtGoxEarned = mtGoxTradeResult.getData().getTotal_spent().getValue();
		BigDecimal mtGoxCommission = mtGoxEarned.multiply(mtGoxCommissionRate, new MathContext(7));
		BigDecimal bitcoinCentralSpent = bitcoinCentralTradeResult.getTraded_currency();
		BigDecimal bitcoinCentralCommission = bitcoinCentralSpent.multiply(bitcoinCentralCommissionRate, new MathContext(7));
		
		logger.info("Earned: " + mtGoxEarned + 
				", Spent (with Commission): " + 
				mtGoxCommission.add(bitcoinCentralSpent).add(bitcoinCentralCommission) + 
				" (" + bitcoinCentralSpent + ", " + mtGoxCommission + ", " + bitcoinCentralCommission + ")");
		
		
		
		
		
		
		//hm.put("BC Ask UUID", bco.getUuid());
		//hm.put("MtGox Bid Order ID", o.getData());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
