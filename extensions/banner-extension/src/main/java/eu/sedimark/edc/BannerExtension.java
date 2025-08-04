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

import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;

import java.io.IOException;

import static eu.sedimark.edc.BannerExtension.NAME;

@Extension(NAME)
public class BannerExtension implements ServiceExtension {
    public static final String NAME = " SEDIMARK Banner Extension";

    private Monitor monitor;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        monitor = context.getMonitor().withPrefix("SEDIMARK");
        monitor.info("Welcome to SEDIMARK. You can visit https://www.sedimark.eu for more info!!");

        try (var banner = getClass().getClassLoader().getResourceAsStream("banner.txt")) {
            if (banner != null) {
                System.out.println(new String(banner.readAllBytes()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
