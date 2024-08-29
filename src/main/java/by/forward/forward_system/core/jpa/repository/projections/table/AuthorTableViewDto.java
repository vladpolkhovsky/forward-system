package by.forward.forward_system.core.jpa.repository.projections.table;

import java.util.ArrayList;
import java.util.List;

public class AuthorTableViewDto implements Named, OrderParticipant, HasDiscipline {

    private final List<String> disciplinesExcellent = new ArrayList<>();
    private final List<String> disciplinesGood = new ArrayList<>();
    private final List<String> disciplinesMaybe = new ArrayList<>();

    private final List<String> orderTechNumbers = new ArrayList<>();

    private final String username;

    public AuthorTableViewDto(String username) {
        this.username = username;
    }

    @Override
    public List<String> excellent() {
        return disciplinesExcellent;
    }

    @Override
    public List<String> good() {
        return disciplinesGood;
    }

    @Override
    public List<String> maybe() {
        return disciplinesMaybe;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public List<String> orderTechNumbers() {
        return orderTechNumbers;
    }

    @Override
    public int activeOrderCount() {
        return orderTechNumbers.size();
    }
}
