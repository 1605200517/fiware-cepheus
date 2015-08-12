/*
 * Copyright (C) 2015 Orange
 *
 * This software is distributed under the terms and conditions of the 'GNU GENERAL PUBLIC LICENSE
 * Version 2' license which can be found in the file 'LICENSE.txt' in this package distribution or
 * at 'http://www.gnu.org/licenses/gpl-2.0-standalone.html'.
 */

package com.orange.cepheus.cep;

import com.espertech.esper.client.*;
import com.orange.cepheus.cep.exception.ConfigurationException;
import com.orange.cepheus.cep.exception.EventProcessingException;
import com.orange.cepheus.cep.exception.EventTypeNotFoundException;
import com.orange.cepheus.cep.model.Attribute;
import com.orange.cepheus.cep.model.EventType;
import com.orange.cepheus.cep.model.Configuration;
import com.orange.cepheus.cep.model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * ComplexEventProcessor implementation using EsperTech Esper CEP
 */
@Component
public class EsperEventProcessor implements ComplexEventProcessor {

    private static Logger logger = LoggerFactory.getLogger(EsperEventProcessor.class);

    private final EPServiceProvider epServiceProvider;
    private Configuration configuration;

    @Autowired
    public EventMapper eventMapper;

    @Autowired
    public EventSinkListener eventSinkListener;

    public EsperEventProcessor() {
        epServiceProvider = EPServiceProviderManager.getDefaultProvider(new com.espertech.esper.client.Configuration());
    }

    /**
     * Apply a new configuration to the Esper CEP.
     * @param configuration the new configuration to apply
     */
    public void setConfiguration(Configuration configuration) throws ConfigurationException {
        Configuration previousConfiguration = this.configuration;
        ConfigurationOperations operations = epServiceProvider.getEPAdministrator().getConfiguration();
        try {
            Collection<EventType> previousEventTypes = Collections.emptyList();

            // Update incoming event types
            Collection<EventType> newEventTypes = Collections.unmodifiableList(configuration.getEventTypeIns());
            if (previousConfiguration != null) {
                previousEventTypes = Collections.unmodifiableList(previousConfiguration.getEventTypeIns());
            }
            this.updateEventTypes(previousEventTypes, newEventTypes, operations);

            // Update outgoing event types
            newEventTypes = Collections.unmodifiableList(configuration.getEventTypeOuts());
            if (previousConfiguration != null) {
                previousEventTypes = Collections.unmodifiableList(previousConfiguration.getEventTypeOuts());
            }
            this.updateEventTypes(previousEventTypes, newEventTypes, operations);

            // Update the statements
            this.updateStatements(configuration.getStatements());

            this.configuration = configuration;
            eventSinkListener.setConfiguration(configuration);
        } catch (Exception e) {
            // Try to restore the previous configuration (if any)
            restoreConfiguration();

            throw new ConfigurationException("Failed to apply new configuration", e);
        }
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Restores the active configuration by wiping out the complete set of active statements and event types.
     * This operation will lock the entire CEP engine.
     * @return true if the restoration was successful, false if the CEP failed to reinitialize from the active configuration
     */
    private boolean restoreConfiguration() {
        // Cannot restore when no previous configuration is defined
        Configuration previousConfiguration = this.configuration;
        if (previousConfiguration == null) {
            return false;
        }

        epServiceProvider.getEngineInstanceWideLock().writeLock().lock();

        try {
            ConfigurationOperations operations = epServiceProvider.getEPAdministrator().getConfiguration();

            // Cleanup previous configuration
            epServiceProvider.getEPAdministrator().destroyAllStatements();
            for (com.espertech.esper.client.EventType eventType : operations.getEventTypes()) {
                operations.removeEventType(eventType.getName(), true);
            }

            // Adding back in/out events, then statements
            Collection<EventType> inEventTypes = Collections.unmodifiableList(previousConfiguration.getEventTypeIns());
            Collection<EventType> outEventTypes = Collections.unmodifiableList(previousConfiguration.getEventTypeOuts());
            this.updateEventTypes(Collections.emptyList(), inEventTypes, operations);
            this.updateEventTypes(Collections.emptyList(), outEventTypes, operations);
            this.updateStatements(previousConfiguration.getStatements());

        } catch (Exception e) {
            logger.error("Failed to restore active configuration", e);
            this.configuration = null;
            return false;
        } finally {
            epServiceProvider.getEngineInstanceWideLock().writeLock().unlock();
        }

        return true;
    }

    /**
     * Make Esper process an event
     * @param event
     * @throws EventProcessingException
     */
    public void processEvent(Event event) throws EventProcessingException {
        logger.debug("Event sent to Esper {}", event.toString());

        try {
            this.epServiceProvider.getEPRuntime().sendEvent(event.getValues(), event.getType());
        } catch (com.espertech.esper.client.EPException e) {
            throw new EventProcessingException(e.getMessage());
        }
    }

    /**
     * Return a list of Attribute for a given even type. This is mainly useful for testing.
     * @param eventTypeName
     * @return
     * @throws EventTypeNotFoundException
     */
    public Map<String, Attribute> getEventTypeAttributes(String eventTypeName) throws EventTypeNotFoundException {
        Map<String, Attribute> attributes = new HashMap<>();

        com.espertech.esper.client.EventType eventType = epServiceProvider.getEPAdministrator().getConfiguration().getEventType(eventTypeName);
        if (eventType != null){
            for (String name : eventType.getPropertyNames()) {
                if (!("id".equals(name))) {
                    String type = eventType.getPropertyType(name).getSimpleName().toLowerCase();
                    attributes.put(name, new Attribute(name, type));
                }
            }
        } else {
            throw new EventTypeNotFoundException("The event type does not exist.");
        }
        return attributes;
    }

    /**
     * Return the list of EPL statements. This is mainly useful for testing.
     * @return a list of EPL statements
     */
    public List<String> getStatements() {
        List<String> statements = new LinkedList<>();
        for (String statementName : epServiceProvider.getEPAdministrator().getStatementNames()) {
            EPStatement statement = epServiceProvider.getEPAdministrator().getStatement(statementName);
            if (statement != null) {
                statements.add(statement.getText());
            }
        }
        return statements;
    }

    /**
     * Update the CEP event types by adding new types and removing the older ones.
     *
     * @param oldList the previous list of event types
     * @param newList the new list of event types
     * @param operations the CEP configuration
     */
    private void updateEventTypes(Collection<EventType> oldList, Collection<EventType> newList, ConfigurationOperations operations) {
        List<EventType> eventTypesToRemove = new LinkedList<>(oldList);
        eventTypesToRemove.removeAll(newList);

        List<EventType> eventTypesToAdd = new LinkedList<>(newList);
        eventTypesToAdd.removeAll(oldList);

        // List all statements depending on the event types to remove
        Set<String> statementsToDelete = new HashSet<>();
        for (EventType eventType : eventTypesToRemove) {
            statementsToDelete.addAll(operations.getEventTypeNameUsedBy(eventType.getType()));
        }
        // Delete all the statements depending on the event types to remove
        for (String statementName : statementsToDelete) {
            EPStatement statement = epServiceProvider.getEPAdministrator().getStatement(statementName);
            if (statement != null) {
                statement.stop();
                statement.destroy();
            }
        }
        // Finally remove the event types
        for (EventType eventType : eventTypesToRemove) {
            operations.removeEventType(eventType.getType(), false);
        }

        for (EventType eventType : eventTypesToAdd) {
            String eventTypeName = eventType.getType();
            // Add event type mapped to esper representation
            operations.addEventType(eventTypeName, eventMapper.esperTypeFromEventType(eventType));
        }
    }

    /**
     * Update the EPL statements by adding new statements, and removing unused statements
     * @param statements
     * @throws NoSuchAlgorithmException
     */
    private void updateStatements(Collection<String> statements) throws NoSuchAlgorithmException {
        // Keep a list of MD5 hash of all added statements
        Set<String> hashes = new HashSet<>();

        // Update EPL statements
        for (String eplStatement : statements) {
            String hash = MD5(eplStatement);
            hashes.add(hash);

            // Create statement if does not already exist
            EPStatement statement = epServiceProvider.getEPAdministrator().getStatement(hash);
            if (statement == null) {
                statement = epServiceProvider.getEPAdministrator().createEPL(eplStatement, hash);
                statement.addListener(eventSinkListener);
            }

        }

        // Removed unused statements
        for (String hash : epServiceProvider.getEPAdministrator().getStatementNames()) {
            if (!hashes.contains(hash)) {
                EPStatement statement = epServiceProvider.getEPAdministrator().getStatement(hash);
                if (statement != null) {
                    statement.destroy();
                }
            }
        }
    }

    /**
     * Generate the MD5 hash of a message
     * @param message
     * @return the hash
     * @throws NoSuchAlgorithmException
     */
    private String MD5(String message) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] array = md.digest(message.getBytes());
        return new BigInteger(1, array).toString(16);
        /*StringBuffer sb = new StringBuffer();
        for (int i = 0; i < array.length; ++i) {
            sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
        }
        return sb.toString();*/
    }
}
