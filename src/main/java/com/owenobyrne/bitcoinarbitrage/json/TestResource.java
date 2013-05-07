package com.owenobyrne.bitcoinarbitrage.json;

import java.math.BigDecimal;
import java.util.HashMap;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.owenobyrne.btce.BTCE;
import com.owenobyrne.btce.api.model.Depth;
import com.owenobyrne.mtgox.MtGox;
import com.owenobyrne.mtgox.api.model.Info;
import com.owenobyrne.mtgox.api.model.Quote;

@Path("/test1")
@Component
public class TestResource {

	@Context
	UriInfo uriInfo;
	@Context
	Request request;

	@Autowired
	MtGox mtgox;
	
	@Autowired
	BTCE btce;
	//BitcoinCentral bitcoincentral;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public HashMap<String, String> respondAsReady() throws Exception {

		//Order o = mtgox.trade("bid", new BigDecimal(0.01).scaleByPowerOfTen(8).toBigInteger());
		//BitcoinCentralOrder bco = bitcoincentral.trade("sell", new BigDecimal(0.01).round(new MathContext(4)));
		
		Info i = mtgox.getInfo();
		Quote askq = mtgox.getQuote("ask");
		
		//MarketDepth md = bitcoincentral.getMarketDepth();
		
		Depth md = btce.getMarketDepth("btc_eur");
		
		BigDecimal btcBalance = 
				(i.getData().getWallets().get("BTC").getBalance().getValue_int())
				.scaleByPowerOfTen(-8);
		
		BigDecimal askPrice = (askq.getData().getAmount()).scaleByPowerOfTen(-5);
		
		HashMap<String, String> hm = new HashMap<String, String>();
		hm.put("MtGox BTC", "" + btcBalance);
		hm.put("MtGox Ask Price", "" + askPrice);
		hm.put("MtGox EUR Value", "" + btcBalance.multiply(askPrice));
		//hm.put("BC BTC Bid", "" + md.getBids()[0].getAmount());
		//hm.put("BC Bid Price", "" + md.getBids()[0].getPrice());
		hm.put("BTCe BTC Bid", "" + md.getBids().get(0).get(0));
		hm.put("BTCe Bid Price", "" + md.getBids().get(0).get(1));
		//hm.put("BC Ask UUID", bco.getUuid());
		//hm.put("MtGox Bid Order ID", o.getData());
		
		return hm;
		
	}
}
