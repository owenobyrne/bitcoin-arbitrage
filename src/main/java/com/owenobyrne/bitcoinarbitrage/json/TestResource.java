package com.owenobyrne.bitcoinarbitrage.json;

import java.math.BigDecimal;
import java.math.MathContext;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.owenobyrne.mtgox.MtGox;
import com.owenobyrne.mtgox.api.model.Info;
import com.owenobyrne.mtgox.api.model.Order;
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
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String respondAsReady() throws Exception {

		Order o = mtgox.trade("bid", new BigDecimal(0.04).scaleByPowerOfTen(8).toBigInteger());
		
		Info i = mtgox.getInfo();
		Quote askq = mtgox.getQuote("ask");
		
		BigDecimal btcBalance = 
				(i.getData().getWallets().get("BTC").getBalance().getValue_int())
				.scaleByPowerOfTen(-8);
		
		BigDecimal askPrice = (askq.getData().getAmount()).scaleByPowerOfTen(-5);
		
		return "Order ID: " + o.getData() + " - remaining MtGox portfolio value = " + btcBalance + " BTC @ " + askPrice + " EUR: " + btcBalance.multiply(askPrice);
		
	}
}
