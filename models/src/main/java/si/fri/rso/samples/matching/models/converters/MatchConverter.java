package si.fri.rso.samples.matching.models.converters;

import si.fri.rso.samples.matching.lib.Match;
import si.fri.rso.samples.matching.models.entities.MatchEntity;

public class MatchConverter {

    public static Match toDto(MatchEntity e) {

        Match m = new Match();
        m.setId(e.getId());
        m.setApartmentId(e.getApartmentId());
        m.setUserId(e.getUserId());
        m.setLocation(e.getLocation());
        m.setInitiator(e.getInitiator());
        m.setMutual(e.getMutual());

        return m;
    }

    public static MatchEntity toEntity(Match m) {

        MatchEntity e = new MatchEntity();
        e.setId(m.getId());
        e.setApartmentId(m.getApartmentId());
        e.setUserId(m.getUserId());
        e.setLocation(m.getLocation());
        e.setInitiator(m.getInitiator());
        e.setMutual(m.getMutual());

        return e;
    }

}
