/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2012 The eXist Project
 *  http://exist-db.org
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 *  $Id$
 */
package org.exist.monitoring.jms;

import java.net.URI;

import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;
import org.exist.EXistException;
import org.exist.config.Configurable;
import org.exist.config.Configuration;
import org.exist.config.ConfigurationException;
import org.exist.config.Configurator;
import org.exist.config.Startable;
import org.exist.config.annotation.ConfigurationClass;
import org.exist.config.annotation.ConfigurationFieldAsAttribute;
import org.exist.config.annotation.ConfigurationFieldAsElement;
import org.exist.monitoring.MonitoringManager;
import org.exist.storage.DBBroker;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
@ConfigurationClass("service-broker")
public class Broker implements Configurable, Startable {
	
	@ConfigurationFieldAsAttribute("id")
	protected String id;

	@ConfigurationFieldAsElement("uri")
	protected String uri = "broker:tcp://localhost:61616";

	private BrokerService serviceBroker;

	private Subscriber subscriber = null;
	
	private Configuration configuration = null;
	private MonitoringManager manager;
	
	public Broker(MonitoringManager manager, Configuration config) throws ConfigurationException {

		this.manager = manager;
		
        configuration = Configurator.configure(this, config);

		try {
			serviceBroker = BrokerFactory.createBroker(
				new URI(uri)
			);
		} catch (Exception e) {
			throw new ConfigurationException(e);
		}
			
		serviceBroker.setBrokerName("eXmin");
		serviceBroker.setUseJmx(false);
		serviceBroker.setPersistent(false);
		
//		broker.setSslContext(
//			new SslContext()
//		);

//		broker.addConnector("tcp://localhost:61616?needClientAuth=true");
	}
	

	@Override
	public void startUp(DBBroker broker) throws EXistException {
		if (serviceBroker.isStarted())
			return;
		
		try {
			serviceBroker.start();
		} catch (Exception e) {
			throw new EXistException(e);
		}
		
		subscriber = new Subscriber(manager.jms);
	}
	
	public void stop() {
		
		subscriber.shutdown();

		try {
			serviceBroker.stop();
		} catch (Exception e) {
			//TODO: log
			e.printStackTrace();
		}
	}

	@Override
	public boolean isConfigured() {
		return configuration != null;
	}

	@Override
	public Configuration getConfiguration() {
		return configuration;
	}
}
