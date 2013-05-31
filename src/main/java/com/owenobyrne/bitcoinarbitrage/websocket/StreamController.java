package com.owenobyrne.bitcoinarbitrage.websocket;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.AtmosphereResourceEventListenerAdapter;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.owenobyrne.bitcoinarbitrage.SQLiteService;
import com.owenobyrne.bitcoinarbitrage.model.Prices;

/**
 * Handles requests for the application home page.
 */
@Controller
public class StreamController {

	private static final Logger logger = LoggerFactory
			.getLogger(StreamController.class);

	
	@RequestMapping(value = "/howya")
	@ResponseBody
	public void streamAsync(AtmosphereResource atmosphereResource) {
		
		Broadcaster bc = BroadcasterFactory.getDefault().lookup("/feeds/howya", true);
		bc.addAtmosphereResource(atmosphereResource);

		logger.info("Atmo Resource Size: " + bc.getAtmosphereResources().size());

		atmosphereResource.suspend();
	}

}