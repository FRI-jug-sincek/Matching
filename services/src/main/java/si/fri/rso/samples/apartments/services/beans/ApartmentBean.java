package si.fri.rso.samples.apartments.services.beans;

import si.fri.rso.samples.apartments.lib.Apartment;
import si.fri.rso.samples.apartments.models.converters.ApartmentConverter;
import si.fri.rso.samples.apartments.models.entities.ApartmentEntity;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.NotFoundException;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;


@RequestScoped
public class ApartmentBean {

    private Logger log = Logger.getLogger(ApartmentBean.class.getName());

    @Inject
    private EntityManager em;

    public List<Apartment> getApartments() {

        TypedQuery<ApartmentEntity> query = em.createNamedQuery(
                "ApartmentEntity.getAll", ApartmentEntity.class);

        List<ApartmentEntity> resultList = query.getResultList();

        return resultList.stream().map(ApartmentConverter::toDto).collect(Collectors.toList());

    }

    public Apartment getApartment(Integer id) {

        ApartmentEntity apartmentEntity = em.find(ApartmentEntity.class, id);

        if (apartmentEntity == null) {
            throw new NotFoundException();
        }

        Apartment apartment = ApartmentConverter.toDto(apartmentEntity);

        return apartment;
    }

    public Apartment createApartment(Apartment apartment) {

        ApartmentEntity apartmentEntity = ApartmentConverter.toEntity(apartment);

        try {
            beginTx();
            em.persist(apartmentEntity);
            commitTx();
        }
        catch (Exception e) {
            rollbackTx();
        }

        if (apartmentEntity.getId() == null) {
            throw new RuntimeException("Entity was not persisted");
        }

        return ApartmentConverter.toDto(apartmentEntity);
    }

    public Apartment updateApartment(Integer id, Apartment apartment) {

        ApartmentEntity c = em.find(ApartmentEntity.class, id);

        if (c == null) {
            return null;
        }

        ApartmentEntity updatedApartmentEntity = ApartmentConverter.toEntity(apartment);

        try {
            beginTx();
            updatedApartmentEntity.setId(c.getId());
            updatedApartmentEntity = em.merge(updatedApartmentEntity);
            commitTx();
        }
        catch (Exception e) {
            rollbackTx();
        }

        return ApartmentConverter.toDto(updatedApartmentEntity);
    }

    public boolean deleteApartment(Integer id) {

        ApartmentEntity apartmentEntity = em.find(ApartmentEntity.class, id);

        if (apartmentEntity != null) {
            try {
                beginTx();
                em.remove(apartmentEntity);
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
