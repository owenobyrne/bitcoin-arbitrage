package com.owenobyrne.bitcoinarbitrage.model;

import java.math.BigDecimal;

public class Prices {
	BigDecimal mtGoxBidEUR;
	BigDecimal mtGoxAskEUR;
	BigDecimal btceBidEUR;
	BigDecimal btceAskEUR;
	
	public BigDecimal getMtGoxBidEUR() {
		return mtGoxBidEUR;
	}
	public Prices setMtGoxBidEUR(BigDecimal mtGoxBidEUR) {
		this.mtGoxBidEUR = mtGoxBidEUR;
		return this;
	}
	public BigDecimal getMtGoxAskEUR() {
		return mtGoxAskEUR;
	}
	public Prices setMtGoxAskEUR(BigDecimal mtGoxAskEUR) {
		this.mtGoxAskEUR = mtGoxAskEUR;
		return this;
	}
	public BigDecimal getBtceBidEUR() {
		return btceBidEUR;
	}
	public Prices setBtceBidEUR(BigDecimal btceBidEUR) {
		this.btceBidEUR = btceBidEUR;
		return this;
	}
	public BigDecimal getBtceAskEUR() {
		return btceAskEUR;
	}
	public Prices setBtceAskEUR(BigDecimal btceAskEUR) {
		this.btceAskEUR = btceAskEUR;
		return this;
	}
	
	
}
