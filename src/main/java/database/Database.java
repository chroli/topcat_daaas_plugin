package org.icatproject.topcatdoiplugin.database;

import javax.ejb.Stateless;
import javax.ejb.EJB;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

@Stateless
public class Database {

	@PersistenceContext(unitName = "topcat")
    EntityManager em;

}