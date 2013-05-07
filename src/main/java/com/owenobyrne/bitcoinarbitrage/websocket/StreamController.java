package com.owenobyrne.bitcoinarbitrage.websocket;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.AtmosphereResourceEventListenerAdapter;
import org.atmosphere.cpr.Broadcaster;
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

	@Autowired
	SQLiteService sqlite;

	private void suspend(final AtmosphereResource resource) {
		final CountDownLatch countDownLatch = new CountDownLatch(1);
		resource.addEventListener(new AtmosphereResourceEventListenerAdapter() {
			@Override
			public void onSuspend(AtmosphereResourceEvent event) {
				countDownLatch.countDown();
				resource.removeEventListener(this);
			}
		});
		resource.suspend();
		try {
			countDownLatch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@RequestMapping(value = "/howya")
	@ResponseBody
	public void streamAsync(AtmosphereResource atmosphereResource) {
		final ObjectMapper mapper = new ObjectMapper();

		this.suspend(atmosphereResource);

		final Broadcaster bc = atmosphereResource.getBroadcaster();

		logger.info("Atmo Resource Size: " + bc.getAtmosphereResources().size());

		bc.scheduleFixedBroadcast(new Callable<String>() {

			// @Override
			public String call() throws Exception {
				//ArrayList<Prices> prices = sqlite.getPrices();	
				Prices p = sqlite.getLastPrices();
				return mapper.writeValueAsString(p);
			}

		}, 5, TimeUnit.SECONDS);
	}

}