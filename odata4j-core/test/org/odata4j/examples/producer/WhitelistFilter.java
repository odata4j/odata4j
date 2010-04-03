package org.odata4j.examples.producer;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Logger;


import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

import core4j.Enumerable;

public class WhitelistFilter extends Filter {

	private static final Logger log = Logger.getLogger(WhitelistFilter.class.getName());
	
	private final Set<String> whitelist;
	
	public WhitelistFilter(String... allowedAddresses){
		whitelist = Enumerable.create(allowedAddresses).toSet();
	}
	
	@Override
	public String description() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void doFilter(HttpExchange exchange, Chain chain)
			throws IOException {
		
		String ipaddr =  exchange.getRemoteAddress().getAddress().getHostAddress();
		String path = exchange.getRequestURI().getPath();
		
		if (whitelist.contains(ipaddr)){
			log.info("allow "+ ipaddr + " for" + path);
			chain.doFilter(exchange);
		} else {
			log.info("DENY "+ ipaddr + " for " + path);
			exchange.close();
		}
		
		
	}

}
