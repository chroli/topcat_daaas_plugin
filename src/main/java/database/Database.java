package org.icatproject.topcatdaaasplugin.database;

import java.util.Map;
import java.util.HashMap;

import javax.ejb.Stateless;
import javax.ejb.EJB;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import java.text.ParseException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.icatproject.topcatdaaasplugin.exceptions.*;
import org.icatproject.topcatdaaasplugin.EntityList;
import org.icatproject.topcatdaaasplugin.Entity;

@Stateless
public class Database {

	@PersistenceContext(unitName = "topcat")
    EntityManager em;

    public EntityList<Entity> query(String jpqlQuery, Map<String, String> params)  throws DaaasException {
    	try {
	    	Integer limitPageSize = null;
			Integer limitOffset = null;

	   		Pattern pattern = Pattern.compile("(?i)^(.*)LIMIT\\s+(\\d+)\\s*,\\s*(\\d+)\\s*$");
			Matcher matches = pattern.matcher(jpqlQuery);
			if (matches.find()) {
				jpqlQuery = matches.group(1);
				limitOffset = Integer.parseInt(matches.group(2));
				limitPageSize = Integer.parseInt(matches.group(3));
			}

			Query query = em.createQuery(jpqlQuery);

			for (Map.Entry<String, String> entry : params.entrySet()) {
			    query.setParameter(entry.getKey(), entry.getValue());
			}
			
			if (limitOffset != null) {
				query.setFirstResult(limitOffset);
				query.setMaxResults(limitPageSize);
			}

			EntityList out = new EntityList<Entity>();

			for(Object entity  : query.getResultList()){
				out.add((Entity) entity);
			}

			return out;
		} catch(Exception e){
			throw new BadRequestException(e.getMessage());
		}
    }

    public EntityList<Entity> query(String jpqlQuery)  throws DaaasException {
    	return query(jpqlQuery, new HashMap<String, String>());
    }

    public void persist(Object entity){
    	em.persist(entity);
    }

    public void remove(Object entity){
    	em.remove(entity);
    }

}