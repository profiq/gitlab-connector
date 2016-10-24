/*
 * Copyright (C) 2016 profiq Inc. http://www.profiq.com
 *
 * This program is free software: you can redistribute it and/or modify
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
package com.profiq.techresearch.mule.gitlab.config;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gitlab.api.GitlabAPI;
import org.mule.api.ConnectionException;
import org.mule.api.ConnectionExceptionCode;
import org.mule.api.annotations.*;
import org.mule.api.annotations.components.ConnectionManagement;
import org.mule.api.annotations.display.FriendlyName;
import org.mule.api.annotations.display.Password;
import org.mule.api.annotations.display.Summary;
import org.mule.api.annotations.param.ConnectionKey;
import org.mule.api.annotations.param.Default;
import org.mule.api.annotations.param.Optional;

import java.io.IOException;


/**
 * The class which holds the connector configuration.
 *
 * Configurable fields:
 * <ul>
 *     <li><strong>Gitlab host</strong> - URL for Gitlab instance</li>
 *     <li><strong>Private token</strong> - user's private token. Can be obtained automatically from given username and
 *     password so there is no need to fill it manually</li>
 * </ul>
 *
 * @author Filip Vavera
 */
@ConnectionManagement(friendlyName = "Configuration")
public class ConnectorConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectorConfig.class);

    /**
     * URL for the Gitlab instance.
     *
     * Has to begin with protocol http:// or https://. It is used as base URL to perform all API actions.
     * <strong>For example:</strong> "https://gitlab.com"
     */
    @Configurable
    @Default("https://gitlab.com")
    @FriendlyName("Gitlab Host")
    @Summary("URL with Gitlab (has to begin with \"http://\" or \"https://\")")
    private String gitlabHost = "https://gitlab.com";

    /**
     * Gitlab host getter
     *
     * @return Gitlab host URL
     */
    public String getGitlabHost() {
        return this.gitlabHost;
    }

    /**
     * Gitlab host setter
     *
     * @param host Gitlab host URL
     */
    public void setGitlabHost(final String host) {
        this.gitlabHost = host;
    }

    /**
     * Private token for the Gitlab user on Gitlab instance
     *
     * Can be obtained automatically from username and password. No need to enter it manually.
     */
    @Configurable
    @Optional
    @Default("")
    @FriendlyName("Private Token")
    @Summary("private token is obtained automatically, no need to enter it manually")
    private String privateToken;

    /**
     * Private token getter
     *
     * @return private token
     */
    public String getPrivateToken() {
        return this.privateToken;
    }

    /**
     * Private token setter
     *
     * @param token private token
     */
    public void setPrivateToken(final String token) {
        this.privateToken = token;
    }

    /**
     * This is entry point for all API actions on Gitlab. Read only.
     */
    private GitlabAPI apiHandler;

    /**
     * API handler getter
     *
     * @return Gitlab API handler
     */
    public GitlabAPI getApiHandler() {
        return this.apiHandler;
    }

    /**
     * Connect to the Gitlab API. Private token will be obtained if not filled manually.
     *
     * @param username Gitlab user username
     * @param password Gitlab user password
     * @param ignoreCertificationErrors set API to ignore all certification errors (optional)
     * @param requestTimeout manually set request timeout (optional)
     * @throws ConnectionException on connection error
     */
    @Connect(strategy = ConnectStrategy.SINGLE_INSTANCE)
    public void connect(@ConnectionKey final String username, @Password final String password,
                        @Optional final Boolean ignoreCertificationErrors, @Optional final Integer requestTimeout)
            throws ConnectionException {

        LOGGER.trace("Checking for empty Gitlab host...");
        if (StringUtils.isBlank(this.gitlabHost)) {
            LOGGER.error("Host cannot be empty.");
            throw new ConnectionException(ConnectionExceptionCode.UNKNOWN_HOST, null, "Host cannot be empty.");
        }
        LOGGER.trace("Gitlab host is not empty.");

        LOGGER.trace("Checking for Gitlab host protocol...");
        if (!(StringUtils.startsWith(this.gitlabHost, "https://") ||
                StringUtils.startsWith(this.gitlabHost, "http://"))) {
            LOGGER.error("Host has to begin with protocol \"https\" or \"http\".");
            throw new ConnectionException(ConnectionExceptionCode.UNKNOWN_HOST, null,
                    "Host has to begin with protocol \"https\" or \"http\".");
        }
        LOGGER.trace("Gitlab host protocol is correct.");

        LOGGER.trace("Checking for empty private token...");
        if (StringUtils.isBlank(this.privateToken)) {
            LOGGER.trace("Private token is empty. Username and password will be used to obtain private token.");

            LOGGER.trace("Checking for empty username...");
            if (StringUtils.isBlank(username)) {
                LOGGER.error("Username cannot be empty.");
                throw new ConnectionException(ConnectionExceptionCode.INCORRECT_CREDENTIALS, null,
                        "Username cannot be empty.");
            }
            LOGGER.trace("Username is not empty.");

            LOGGER.trace("Checking for empty password...");
            if (StringUtils.isBlank(password)) {
                LOGGER.error("Password cannot be empty.");
                throw new ConnectionException(ConnectionExceptionCode.INCORRECT_CREDENTIALS, null,
                        "Password cannot be empty.");
            }
            LOGGER.trace("Password is not empty.");

            try {
                LOGGER.trace("Trying to connect to the Gitlab and obtain private token.");
                this.privateToken = GitlabAPI.connect(this.gitlabHost, username, password).getPrivateToken();
            } catch (final IOException e) {
                LOGGER.error("Connection to the Gitlab failed.");
                throw new ConnectionException(ConnectionExceptionCode.CANNOT_REACH, e.getMessage(),
                        "Cannot connect to the \"" + this.gitlabHost + "\".");
            }

            LOGGER.trace("Checking the new private token...");
            if (StringUtils.isBlank(this.privateToken)) {
                LOGGER.error("Something went wrong and private token is still empty.");
                throw new ConnectionException(ConnectionExceptionCode.UNKNOWN, null, "Private token is blank.");
            }
        }
        LOGGER.trace("Private token is correct.");

        this.apiHandler = GitlabAPI.connect(this.gitlabHost, this.privateToken);
        LOGGER.trace("API handler for Gitlab API created.");

        LOGGER.trace("Setting Ignore certification errors...");
        if (ignoreCertificationErrors != null) {
            LOGGER.trace("Ignore certification errors is not \"null\".");
            this.apiHandler = this.apiHandler.ignoreCertificateErrors(ignoreCertificationErrors);
            LOGGER.trace("Ignore certification errors is set to: {}", ignoreCertificationErrors);
        }
        else {
            LOGGER.trace("Ignore certificate errors is \"null\". Using default value.");
        }

        LOGGER.trace("Setting Request timeout...");
        if (requestTimeout != null) {
            LOGGER.trace("Request timeout is not \"null\".");
            this.apiHandler = this.apiHandler.setRequestTimeout(requestTimeout);
            LOGGER.trace("Request timeout is set to: {}", requestTimeout);
        }
        else {
            LOGGER.trace("Request timeout is \"null\". Using default value.");
        }
    }

    /**
     * Test connection to the Gitlab
     *
     * @param username Gitlab user username
     * @param password Gitlab user password
     * @param ignoreCertificationErrors set API to ignore all certification errors (optional)
     * @param requestTimeout manually set request timeout (optional)
     * @throws ConnectionException on connection error
     */
    @TestConnectivity
    public void testConnectivity(@ConnectionKey final String username, @Password final String password,
                                 @Optional final Boolean ignoreCertificationErrors,
                                 @Optional final Integer requestTimeout) throws ConnectionException {
        LOGGER.trace("Trying to connect to the Gitlab...");
        try {
            this.connect(username, password, ignoreCertificationErrors, requestTimeout);
            LOGGER.debug("Connection successful.");
        } catch (final ConnectionException ex) {
            LOGGER.error("Connection error.", ex);
            throw ex;
        } finally {
            LOGGER.trace("Trying to disconnect...");
            this.disconnect();
            LOGGER.trace("Disconnection successful.");
        }
    }

    /**
     * Disconnect from the Gitlab API
     */
    @Disconnect
    public void disconnect() {
        this.apiHandler = null;
        LOGGER.trace("API handler is set to \"null\".");
    }

    /**
     * Check if connection is established
     *
     * @return true or false
     */
    @ValidateConnection
    public boolean isConnected() {
        LOGGER.debug("API handler: {}", this.apiHandler);
        return (this.apiHandler != null);
    }
}
