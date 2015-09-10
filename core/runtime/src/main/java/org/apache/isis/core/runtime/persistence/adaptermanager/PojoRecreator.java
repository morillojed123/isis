/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.core.runtime.persistence.adaptermanager;

import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;

public class PojoRecreator {

    private final PersistenceSession persistenceSession;
    private final SpecificationLoaderSpi specificationLoader;

    public PojoRecreator(
            final PersistenceSession persistenceSession,
            final SpecificationLoaderSpi specificationLoader) {
        this.persistenceSession = persistenceSession;
        this.specificationLoader = specificationLoader;
    }

    public Object recreatePojo(RootOid oid) {
        if(oid.isTransient() || oid.isViewModel()) {
            return recreatePojoDefault(oid);
        } else {
            return getPersistenceSession().loadPojo(oid);
        }
    }

    private Object recreatePojoDefault(final RootOid rootOid) {
        final ObjectSpecification spec =
                getSpecificationLoader().lookupBySpecId(rootOid.getObjectSpecId());
        final Object pojo = spec.createObject();
        if(rootOid.isViewModel()) {
            // initialize the view model pojo from the oid's identifier

            final ViewModelFacet facet = spec.getFacet(ViewModelFacet.class);
            if(facet == null) {
                throw new IllegalArgumentException("spec does not have RecreatableObjectFacet; " + rootOid.toString() + "; spec is " + spec.getFullIdentifier());
            }

            final String memento = rootOid.getIdentifier();

            facet.initialize(pojo, memento);
        }
        return pojo;
    }




    ///////////////////////////////


    protected SpecificationLoaderSpi getSpecificationLoader() {
        return specificationLoader;
    }

    protected PersistenceSession getPersistenceSession() {
        return persistenceSession;
    }

}
