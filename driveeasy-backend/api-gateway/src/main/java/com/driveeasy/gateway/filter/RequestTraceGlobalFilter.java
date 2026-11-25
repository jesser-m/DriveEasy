package com.driveeasy.gateway.filter;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Filtre personnalise : genere un identifiant X-Gateway-Request-Id (transmis au service et renvoye au client)
 * et journalise methode, chemin, statut et duree de chaque requete.
 */
@Component
public class RequestTraceGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RequestTraceGlobalFilter.class);
    public static final String HEADER = "X-Gateway-Request-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String requestId = UUID.randomUUID().toString();
        long debut = System.currentTimeMillis();
        ServerHttpRequest request = exchange.getRequest().mutate().header(HEADER, requestId).build();
        exchange.getResponse().getHeaders().add(HEADER, requestId);
        return chain.filter(exchange.mutate().request(request).build()).doFinally(signal ->
                log.info("[{}] {} {} -> {} ({} ms)", requestId, request.getMethod(), request.getURI().getPath(),
                        exchange.getResponse().getStatusCode(), System.currentTimeMillis() - debut));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
