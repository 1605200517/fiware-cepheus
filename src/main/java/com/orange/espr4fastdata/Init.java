/*
 * Copyright (C) 2015 Orange
 *
 * This software is distributed under the terms and conditions of the 'GNU GENERAL PUBLIC LICENSE
 * Version 2' license which can be found in the file 'LICENSE.txt' in this package distribution or
 * at 'http://www.gnu.org/licenses/gpl-2.0-standalone.html'.
 */

package com.orange.espr4fastdata;

import com.orange.espr4fastdata.cep.ComplexEventProcessor;
import com.orange.espr4fastdata.exception.ConfigurationException;
import com.orange.espr4fastdata.exception.PersistenceException;
import com.orange.espr4fastdata.model.cep.Configuration;
import com.orange.espr4fastdata.persistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by pborscia on 03/07/2015.
 */
@Component
public class Init {

    private static Logger logger = LoggerFactory.getLogger(Init.class);

    private final ComplexEventProcessor complexEventProcessor;

    private final Persistence persistence;

    @Autowired
    public Init(ComplexEventProcessor complexEventProcessor, Persistence persistence) throws PersistenceException, ConfigurationException {

        this.complexEventProcessor = complexEventProcessor;
        this.persistence = persistence;

        if (this.persistence.checkConfigurationDirectory()) {
            Configuration configuration = null;

            configuration = this.persistence.loadConfiguration();

            this.complexEventProcessor.setConfiguration(configuration);


        }
    }

}
