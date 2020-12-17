package si.fri.rso.samples.apartments.api.v1.resources;

import si.fri.rso.samples.apartments.lib.Apartment;
import si.fri.rso.samples.apartments.services.beans.ApartmentBean;

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
@Path("/apartments")

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)

public class ApartmentResource {

    private Logger log = Logger.getLogger(ApartmentResource.class.getName());

    @Inject
    private ApartmentBean ab;

    @Context
    protected UriInfo uriInfo;

    @GET
    public Response getApartments() {

        List<Apartment> apartments = ab.getApartments();

        return Response.status(Response.Status.OK).entity(apartments).build();
    }

    @GET
    @Path("/{apartmentId}")
    public Response getApartment(@PathParam("apartmentId") Integer apartmentId) {

        Apartment apartment = ab.getApartment(apartmentId);

        if (apartment == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.status(Response.Status.OK).entity(apartment).build();
    }

    @POST
    public Response createApartment(Apartment apartment) {

        if (apartment.getTitle() == null || apartment.getTenantId() == null || apartment.getLocation() == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        else {
            apartment = ab.createApartment(apartment);
        }

        return Response.status(Response.Status.OK).entity(apartment).build();

    }

    @PUT
    @Path("{apartmentId}")
    public Response updateApartment(@PathParam("apartmentId") Integer apartmentId,
                                     Apartment apartment) {

        apartment = ab.updateApartment(apartmentId, apartment);

        if (apartment == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.status(Response.Status.NOT_MODIFIED).build();

    }

    @DELETE
    @Path("{apartmentId}")
    public Response deleteApartment(@PathParam("apartmentId") Integer apartmentId) {

        boolean deleted = ab.deleteApartment(apartmentId);

        if (deleted) {
            return Response.status(Response.Status.NO_CONTENT).build();
        }
        else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
