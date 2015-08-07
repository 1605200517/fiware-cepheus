/*
 * Copyright (C) 2015 Orange
 *
 * This software is distributed under the terms and conditions of the 'GNU GENERAL PUBLIC LICENSE
 * Version 2' license which can be found in the file 'LICENSE.txt' in this package distribution or
 * at 'http://www.gnu.org/licenses/gpl-2.0-standalone.html'.
 */

package com.orange.cepheus;

import com.orange.cepheus.cep.ComplexEventProcessor;
import com.orange.cepheus.cep.SubscriptionManager;
import com.orange.cepheus.exception.ConfigurationException;
import com.orange.cepheus.exception.PersistenceException;
import com.orange.cepheus.model.Configuration;
import com.orange.cepheus.persistence.Persistence;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;

import static org.mockito.Mockito.*;

/**
 * Test the Init bean.
 */
@RunWith(MockitoJUnitRunner.class)
public class InitTest {

    static Configuration configuration = new Configuration();

    @Mock
    public ComplexEventProcessor complexEventProcessor;

    @Mock
    public Persistence persistence;

    @Mock
    public SubscriptionManager subscriptionManager;

    @Autowired
    @InjectMocks
    public Init init;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Check that CEP engine is called when configuration avail during Init initialization
     */
    @Test
    public void checkConfOk() throws ConfigurationException, PersistenceException {
        when(persistence.checkConfigurationDirectory()).thenReturn(true);
        when(persistence.loadConfiguration()).thenReturn(configuration);

        init.loadConfigurationOnStartup();

        verify(complexEventProcessor).setConfiguration(eq(configuration));
        verify(subscriptionManager).setConfiguration(eq(configuration));
    }

    /**
     * Check that the CEP engine is not called when no configuration exist on initialization
     */
    public void checkNoConf() throws ConfigurationException, PersistenceException {
        when(persistence.checkConfigurationDirectory()).thenReturn(false);
        when(persistence.loadConfiguration()).thenReturn(null);

        init.loadConfigurationOnStartup();

        verify(complexEventProcessor, never()).setConfiguration(anyObject());
        verify(subscriptionManager, never()).setConfiguration(anyObject());
    }

    @After
    public void resetMock() {
        reset(complexEventProcessor);
        reset(persistence);
        reset(subscriptionManager);
    }
}
