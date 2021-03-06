/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the LGNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  LGNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.system.webserver;

import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.webserver.auth.Authentication;
import com.sun.net.httpserver.HttpExchange;

import java.io.InputStream;
import java.util.Optional;

/**
 * Represents a HttpExchange Request.
 * <p>
 * Automatically gets the Basic Auth from headers.
 *
 * @author Rsl1122
 */
public class Request {
    private final String requestMethod;
    private final String target;
    private final HttpExchange exchange;
    private final String remoteAddress;
    private final Locale locale;
    private Authentication auth;

    public Request(HttpExchange exchange, Locale locale) {
        this.requestMethod = exchange.getRequestMethod();
        this.target = exchange.getRequestURI().getPath();

        remoteAddress = exchange.getRemoteAddress().getAddress().getHostAddress();

        this.exchange = exchange;

        this.locale = locale;
    }

    public Optional<Authentication> getAuth() {
        return Optional.ofNullable(auth);
    }

    public void setAuth(Authentication authentication) {
        auth = authentication;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public String getTarget() {
        return target;
    }

    public InputStream getRequestBody() {
        return exchange.getRequestBody();
    }

    @Override
    public String toString() {
        return "Request:" + requestMethod + " " + target;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public Locale getLocale() {
        return locale;
    }
}
