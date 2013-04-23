package com.owenobyrne.bitcoinarbitrage;

import java.util.logging.Logger;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class CheckArbitragePossibilityNow {

	static final Logger logger = Logger.getLogger(CheckArbitragePossibilityNow.class.getName());
	
	public static void main(String[] args) {
		@SuppressWarnings("resource")
		ApplicationContext context = new ClassPathXmlApplicationContext("beans.xml");
		Arbitrator arbitrator = (Arbitrator)context.getBean("arbitrator");
		
		try {
			arbitrator.checkArbitragePossibility();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
