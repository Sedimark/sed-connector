/*
 *  Copyright (c) 2025 Universidad de Cantabria (UC)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Pablo Sotres (UC)
 */

package eu.sedimark.edc.resolution;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.HttpUrl;
import okhttp3.Request;
import org.eclipse.edc.http.spi.EdcHttpClient;
import org.eclipse.edc.iam.did.spi.document.DidDocument;
import org.eclipse.edc.iam.did.spi.resolution.DidResolver;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.result.Result;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;

import static java.lang.String.format;

/**
 * Resolves an IOTA DID according to the <a href="https://docs.iota.org/developer/iota-identity/references/iota-did-method-spec">IOTA DID specification</a>
 * by using the <a href="https://github.com/Sedimark/dlt-booth">SEDIMARK DLT-Booth component</a> as proxy.
 */
public class IotaDidDltBoothResolver implements DidResolver {
    private static final String DID_METHOD = "iota";

    private final EdcHttpClient httpClient;
    private final ObjectMapper mapper;
    private final Monitor monitor;
    private final String dltBoothBaseUrl;

    /**
     * Creates a resolver that executes standard DNS lookups.
     */
    public IotaDidDltBoothResolver(EdcHttpClient httpClient, ObjectMapper mapper, String dltBoothBaseUrl, Monitor monitor) {
        this.httpClient = httpClient;
        this.mapper = mapper;
        this.dltBoothBaseUrl = dltBoothBaseUrl;
        this.monitor = monitor;
    }

    @Override
    public @NotNull String getMethod() {
        return DID_METHOD;
    }

    @Override
    @NotNull
    public Result<DidDocument> resolve(String didKey) {
        HttpUrl url = Objects.requireNonNull(HttpUrl.parse(this.dltBoothBaseUrl)).newBuilder().addPathSegment("dids").addQueryParameter("did", didKey).build();

        var request = new Request.Builder().url(url).get().build();
        try (var response = httpClient.execute(request)) {
            if (response.code() != 200) {
                return Result.failure(format("Error resolving DID: %s. HTTP Code was: %s", didKey, response.code()));
            }
            try (var body = response.body()) {
                if (body == null) {
                    return Result.failure("DID response contained an empty body: " + didKey);
                }
                var didDocument = mapper.readValue(body.string(), DidDocument.class);
                return Result.success(didDocument);
            }
        } catch (IOException e) {
            monitor.severe("Error resolving DID: " + didKey, e);
            return Result.failure("Error resolving DID: " + e.getMessage());
        }
    }

}
