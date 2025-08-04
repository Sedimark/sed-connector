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

package eu.sedimark.edc;

import eu.sedimark.edc.resolution.IotaDidDltBoothResolver;
import org.eclipse.edc.http.spi.EdcHttpClient;
import org.eclipse.edc.iam.did.spi.resolution.DidResolverRegistry;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.types.TypeManager;


/**
 * Initializes support for resolving IOTA DIDs through the SEDIMARK DLT-Booth service.
 */
@Extension(value = IotaDidDltBoothExtension.NAME)
public class IotaDidDltBoothExtension implements ServiceExtension {
    public static final String NAME = "IOTA DID DLT-Booth";

    private Monitor monitor;

    @Setting(description = "DLT-Booth method path to resolve DIDs on the verifiable data registry.", key = "sed.dlt.booth.base.url", required = true)
    private String dltBoothBaseUrl;

    @Inject
    private DidResolverRegistry resolverRegistry;

    @Inject
    private EdcHttpClient httpClient;

    @Inject
    private TypeManager typeManager;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        var mapper = typeManager.getMapper();
        monitor = context.getMonitor().withPrefix("SEDIMARK");

        monitor.debug(String.format("%s extension is using DLT-Booth available at %s", NAME, this.dltBoothBaseUrl));

        var resolver = new IotaDidDltBoothResolver(httpClient, mapper, this.dltBoothBaseUrl, monitor);

        resolverRegistry.register(resolver);
    }

}
