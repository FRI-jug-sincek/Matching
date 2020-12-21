package si.fri.rso.samples.matching.api.v1.resources;

import si.fri.rso.samples.matching.lib.Match;
import si.fri.rso.samples.matching.lib.Apartment;
import si.fri.rso.samples.apartments.services.beans.MatchBean;
import si.fri.rso.samples.matching.lib.User;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.logging.Logger;

@ApplicationScoped
@Path("/matching")

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)

public class MatchResource {

    @Inject
    private MatchBean mb;

    @Context
    protected UriInfo uriInfo;

    //NEUPORABLJENO OD ZUNAJ --SAMO ZA TESTIRANJE
    //localhost:8080/v1/matching/filtered?filter=id:EQ:4&filter=id:EQ:2
    @GET
    @Path("/filtered")
    public Response getMatchesFiltered() {

        List<Match> matches;

        matches = mb.getMatchesFilter(uriInfo);

        return Response.status(Response.Status.OK).entity(matches).build();
    }

    private Logger log = Logger.getLogger(MatchResource.class.getName());

    @POST
    @Path("apartment-recommendations/")
    public Response getApartmentRecommendations(User u) {

        List<Integer> apartments = mb.getApartmentRecommendations(u);

        return Response.status(Response.Status.OK).entity(apartments).build();
    }

    @POST
    @Path("user-recommendations/")
    public Response getUserRecommendations(Apartment a) {

        List<Integer> users = mb.getUserRecommendations(a);

        return Response.status(Response.Status.OK).entity(users).build();
    }



    @POST
    @Path("match/")
    public Response match(Match m) {

        if (m.getMutual() == null || m.getInitiator() == null || m.getLocation() == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        Match res = mb.match(m);

        return Response.status(Response.Status.OK).entity(res).build();
    }

    @POST
    @Path("unmatch/")
    public Response unmatch(Match match) {

        boolean deleted = mb.deleteMatch(match.getId());

        if (deleted) {
            return Response.status(Response.Status.NO_CONTENT).build();
        }
        else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @DELETE
    @Path("{matchId}")
    public Response deleteMatch(@PathParam("matchId") Integer matchId) {

        boolean deleted = mb.deleteMatch(matchId);

        if (deleted) {
            return Response.status(Response.Status.NO_CONTENT).build();
        }
        else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
