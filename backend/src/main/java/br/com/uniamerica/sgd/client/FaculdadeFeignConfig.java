package br.com.uniamerica.sgd.client;

import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;

import feign.Request;
import feign.Retryer;

/**
 * Configuração exclusiva do {@link FaculdadeFeignClient}.
 * Sem {@code @Configuration} de propósito, para não valer para outros clients Feign.
 */
public class FaculdadeFeignConfig {

    private static final long CONNECT_TIMEOUT_SEGUNDOS = 3;
    private static final long READ_TIMEOUT_SEGUNDOS = 5;

    @Bean
    Request.Options faculdadeRequestOptions() {
        return new Request.Options(CONNECT_TIMEOUT_SEGUNDOS, TimeUnit.SECONDS,
                READ_TIMEOUT_SEGUNDOS, TimeUnit.SECONDS, true);
    }

    /** Login não deve ser repetido automaticamente. */
    @Bean
    Retryer faculdadeRetryer() {
        return Retryer.NEVER_RETRY;
    }
}
