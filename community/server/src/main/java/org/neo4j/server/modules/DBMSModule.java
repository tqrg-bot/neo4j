/*
 * Copyright (c) 2002-2019 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.server.modules;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import org.neo4j.configuration.Config;
import org.neo4j.configuration.GraphDatabaseSettings;
import org.neo4j.server.rest.dbms.UserService;
import org.neo4j.server.rest.discovery.DiscoverableURIs;
import org.neo4j.server.rest.discovery.DiscoveryService;
import org.neo4j.server.web.WebServer;

import static java.util.Collections.singletonList;
import static org.neo4j.server.plugins.Injectable.injectable;

/**
 * Mounts the DBMS REST API.
 */
public class DBMSModule implements ServerModule
{
    private static final String ROOT_PATH = "/";

    private final WebServer webServer;
    private final Config config;
    private final Supplier<DiscoverableURIs> discoverableURIs;

    public DBMSModule( WebServer webServer, Config config, Supplier<DiscoverableURIs> discoverableURIs )
    {
        this.webServer = webServer;
        this.config = config;
        this.discoverableURIs = discoverableURIs;
    }

    @Override
    public void start()
    {
        webServer.addJAXRSClasses(
                singletonList( DiscoveryService.class ), ROOT_PATH,
                singletonList( injectable( DiscoverableURIs.class, discoverableURIs.get() ) ) );

        webServer.addJAXRSClasses( getClasses(), ROOT_PATH, null );
    }

    @Override
    public void stop()
    {
        webServer.removeJAXRSClasses( getClasses(), ROOT_PATH );
    }

    private List<Class<?>> getClasses()
    {
        if ( config.get( GraphDatabaseSettings.auth_enabled ) )
        {
            return Collections.singletonList( UserService.class );
        }
        return Collections.emptyList();
    }
}
