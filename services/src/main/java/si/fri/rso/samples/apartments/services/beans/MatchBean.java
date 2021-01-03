package si.fri.rso.samples.apartments.services.beans;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.utils.JPAUtils;
import kong.unirest.Unirest;
import si.fri.rso.samples.matching.lib.Apartment;
import si.fri.rso.samples.matching.lib.Match;
import si.fri.rso.samples.matching.lib.User;
import si.fri.rso.samples.matching.models.converters.MatchConverter;
import si.fri.rso.samples.matching.models.entities.MatchEntity;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;


@RequestScoped
public class MatchBean {

    private Logger log = Logger.getLogger(MatchBean.class.getName());

    @Inject
    private EntityManager em;

    public List<Integer> getApartmentRecommendations(User u) {
        ArrayList<Integer> apartmentsForRecommendation = new ArrayList<>();

        // find all the matches that have the same userId as the user from the request
        List<Match> matchesByUser = getMatchesFilterString("filter=userId:eq:" + u.getUserId());

        ArrayList<Integer> knownApartmentIdsUnique = new ArrayList<>();

        //find all matches from the users location that were initialised by an appartment and are not yet mutual
        //and add their apartmentIds to the results (result = max 20 entries)
        for(Match m : matchesByUser) {
            if(m.getLocation().equals(u.getLocation()) && m.getInitiator().equals("APT") && m.getMutual().equals("TBD")) {
                apartmentsForRecommendation.add(m.getApartmentId());
            }

            if(!knownApartmentIdsUnique.contains(m.getApartmentId())) {
                knownApartmentIdsUnique.add(m.getApartmentId());
            }

            if(apartmentsForRecommendation.size() > 19) {
                break;
            }
        }

        log.info("Searching for apartments in location " + u.getLocation());

        //get apartments from the users location
        String jsonResponse = null;
        try {
            // jsonResponse = Unirest.get("http://localhost:8080/v1/apartments/filtered?filter=location:EQ:{location}")
            //                 .routeParam("location", u.getLocation())
            //                 .asString().getBody();
            jsonResponse = Unirest.get("http://40.76.169.130/apartments/v1/apartments/filtered?filter=location:EQ:{location}")
                            .routeParam("location", u.getLocation())
                            .asString().getBody();
        } catch (Exception ex) {
            log.info("Error making a GET request - " + ex.getMessage());
            return apartmentsForRecommendation;
        }

        JsonArray responseArray = null;

        try {
            JsonParser parser = new JsonParser();
            JsonElement tmp = parser.parse(jsonResponse);
            responseArray = tmp.getAsJsonArray();
        } catch (Exception ex) {
            log.info("Error when decoding: " + ex.getMessage());
            return apartmentsForRecommendation;
        }

        //add the ids of the ones that were not recommeneded yet to the result (result = max 20 entries)
        for (JsonElement el : responseArray) {
            if(apartmentsForRecommendation.size() > 19) {
                break;
            }

            log.info("ResponseElement: " + el.toString());

            if(!knownApartmentIdsUnique.contains(el.getAsJsonObject().get("id").getAsInt())) {
                apartmentsForRecommendation.add(el.getAsJsonObject().get("id").getAsInt());
            }
        }

        return apartmentsForRecommendation;
    }

    public List<Integer> getUserRecommendations(Apartment a) {
        ArrayList<Integer> usersForRecommendation = new ArrayList<>();

        // find all the matches that have the same apartmentId as the apartment from the request
        List<Match> matchesByApartment = getMatchesFilterString("filter=apartmentId:eq:" + a.getId());

        ArrayList<Integer> knownUserIdsUnique = new ArrayList<>();

        //find all matches from the apartments location that were initialised by an user and are not yet mutual
        //and add their userIds to the results (result = max 20 entries)
        for(Match m : matchesByApartment) {
            if(m.getLocation().equals(a.getLocation()) && m.getInitiator().equals("USR") && m.getMutual().equals("TBD")) {
                usersForRecommendation.add(m.getUserId());
            }

            if(!knownUserIdsUnique.contains(m.getUserId())) {
                knownUserIdsUnique.add(m.getUserId());
            }

            if(usersForRecommendation.size() > 19) {
                break;
            }
        }

        log.info("Searching for users in location " + a.getLocation());

        //get apartments from the users location
        String jsonResponse = null;
        try {
            // jsonResponse = Unirest.get("http://localhost:8082/v1/users/filtered?filter=location:EQ:{location}")
            //         .routeParam("location", a.getLocation())
            //         .asString().getBody();
            jsonResponse = Unirest.get("http://40.76.169.130/users/v1/users/filtered?filter=location:EQ:{location}")
                    .routeParam("location", a.getLocation())
                    .asString().getBody();
        } catch (Exception ex) {
            log.info("Error making a GET request - " + ex.getMessage());
            return usersForRecommendation;
        }

        JsonArray responseArray = null;

        try {
            JsonParser parser = new JsonParser();
            JsonElement tmp = parser.parse(jsonResponse);
            responseArray = tmp.getAsJsonArray();
        } catch (Exception ex) {
            log.info("Error when decoding: " + ex.getMessage());
            return usersForRecommendation;
        }

        //add the ids of the ones that were not recommeneded yet to the result (result = max 20 entries)
        for (JsonElement el : responseArray) {
            if(usersForRecommendation.size() > 19) {
                break;
            }

            log.info("ResponseElement: " + el.toString());

            if(!knownUserIdsUnique.contains(el.getAsJsonObject().get("userId").getAsInt())) {
                usersForRecommendation.add(el.getAsJsonObject().get("userId").getAsInt());
            }
        }

        return usersForRecommendation;
    }

    public Match match(Match m1) {

        log.info("Try to match: " + MatchConverter.toString(m1));

        //find all the previous matches wih this user
        String queryString = "filter=" +
                             "userId:EQ:" + m1.getUserId() + " " +
                             "apartmentId:EQ:" + m1.getApartmentId();
        List <Match> ms = getMatchesFilterString(queryString);

        Match temp = new Match();

        if (ms.size() == 0) {
            return createMatch(m1);
        } else {
            boolean changed = false;
            for (Match m2 : ms) {
                log.info("Compare with match: " + MatchConverter.toString(m2));
                //if for some reason we already had a mutual match the one party said no do nothing
                if(m2.getMutual().equals("YES") || m2.getMutual().equals("NO")) {
                    System.out.println("00");
                    return null;
                }
                //the new match is of the opposite initiation but the mutuality is NO
                //this means we should update the old TBD with NO
                if(!m1.getInitiator().equals(m2.getInitiator()) && m1.getMutual().equals("NO")) {
                    changed = true;
                    temp.setUserId(m2.getUserId());
                    temp.setApartmentId(m2.getApartmentId());
                    temp.setLocation(m2.getLocation());
                    temp.setMutual("NO");
                    temp.setInitiator(m2.getInitiator());
                    System.out.println("11");
                    break;
                }
                //the new match is of the opposite initiation and is mutual
                //this means we should update the old TBD with YES
                if(!m1.getInitiator().equals(m2.getInitiator()) && m1.getMutual().equals("TBD")) {
                    changed = true;
                    temp.setUserId(m2.getUserId());
                    temp.setApartmentId(m2.getApartmentId());
                    temp.setLocation(m2.getLocation());
                    temp.setMutual("YES");
                    temp.setInitiator(m2.getInitiator());
                    System.out.println("22");
                    break;
                }
            }
            System.out.println(changed);
            //we have a match pair
            if(changed) {
                //we remove all the old useless matches
                for (Match m2 : ms) {
                    deleteMatch(m2.getId());
                }
                //we create a new one from the pair
                return createMatch(temp);
            } else {
                return null;
            }
        }
    }

    public List<Match> getMatchesFilter(UriInfo uriInfo) {

        QueryParameters queryParameters = QueryParameters.query(uriInfo.getRequestUri().getQuery()).defaultOffset(0)
                .build();

        System.out.println("Search for: " + uriInfo.getRequestUri().getQuery());

        return JPAUtils.queryEntities(em, MatchEntity.class, queryParameters).stream()
                .map(MatchConverter::toDto).collect(Collectors.toList());
    }

    public List<Match> getMatchesFilterString(String queryString) {

        QueryParameters queryParameters = QueryParameters.query(queryString).defaultOffset(0).build();

        System.out.println("Search for: " + queryString);

        return JPAUtils.queryEntities(em, MatchEntity.class, queryParameters).stream()
                .map(MatchConverter::toDto).collect(Collectors.toList());
    }

    public Match createMatch(Match match) {

        MatchEntity matchEntity = MatchConverter.toEntity(match);

        try {
            beginTx();
            em.persist(matchEntity);
            commitTx();
        }
        catch (Exception e) {
            rollbackTx();
        }

        if (matchEntity.getId() == null) {
            throw new RuntimeException("Entity was not persisted");
        }

        return MatchConverter.toDto(matchEntity);
    }

    public boolean deleteMatch(Integer id) {

        MatchEntity matchEntity = em.find(MatchEntity.class, id);

        if (matchEntity != null) {
            try {
                beginTx();
                em.remove(matchEntity);
                commitTx();
            }
            catch (Exception e) {
                rollbackTx();
            }
        }
        else {
            return false;
        }

        return true;
    }

    private void beginTx() {
        if (!em.getTransaction().isActive()) {
            em.getTransaction().begin();
        }
    }

    private void commitTx() {
        if (em.getTransaction().isActive()) {
            em.getTransaction().commit();
        }
    }

    private void rollbackTx() {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
    }
}
