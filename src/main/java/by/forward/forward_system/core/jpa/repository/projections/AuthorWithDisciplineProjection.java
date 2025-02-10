package by.forward.forward_system.core.jpa.repository.projections;

public interface AuthorWithDisciplineProjection {
    Long getId();

    ;

    String getUsername();

    Long getDisciplineId();

    String getDiscipline();

    String getDisciplineQuality();
}
