package com.google.code.cxf.protobuf.utils;

import java.io.IOException;

import org.apache.cxf.endpoint.ClientImpl;
import org.apache.cxf.endpoint.ConduitSelector;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.Conduit;

/**
 * Utilities around CXF exchange and conduit.
 * 
 * @author Gyorgy Orban
 */
public final class ExchangeUtils {

	private ExchangeUtils() {
	}

	public static void closeConduit(Exchange exchange) throws IOException {
		ConduitSelector conduitSelector = null;
		synchronized (exchange) {
			conduitSelector = exchange.get(ConduitSelector.class);
			if (conduitSelector != null) {
				exchange.remove(ConduitSelector.class.getName());
			}
		}

		Conduit selectedConduit = null;
		Message message = exchange.getInMessage() == null ? exchange
				.getOutMessage() : exchange.getInMessage();

		if (conduitSelector != null && message != null) {
			selectedConduit = conduitSelector.selectConduit(message);
		}

		selectedConduit.close(message);
	}

	public static void setExchangeFinished(Exchange exchange) {
		synchronized (exchange) {
			if (!isExchangeFinished(exchange)) {
				exchange.put(ClientImpl.FINISHED, Boolean.TRUE);
				exchange.notifyAll();
			}
		}
	}

	public static boolean isExchangeFinished(Exchange exchange) {
		synchronized (exchange) {
			return Boolean.TRUE.equals(exchange.get(ClientImpl.FINISHED));
		}
	}

	public static Exception getException(Exchange exchange) {
		Exception throwable = exchange.get(Exception.class);

		if (exchange.getInFaultMessage() != null) {
			return exchange.getInFaultMessage().getContent(Exception.class);
		} else if (exchange.getOutFaultMessage() != null) {
			return exchange.getOutFaultMessage().getContent(Exception.class);
		}

		if (throwable != null) {
			return throwable;
		}

		throwable = getException(exchange.getOutMessage());

		if (throwable != null) {
			return throwable;
		}

		throwable = getException(exchange.getInMessage());

		if (throwable != null) {
			return throwable;
		}

		return null;
	}

	protected static Exception getException(Message message) {
		Exception ex = null;

		if (message != null) {
			ex = message.getContent(Exception.class);
		}

		return ex;
	}

}
