package ao.co.isptec.aplm.projetoanuncioloc.Model;

import java.util.ArrayList;
import java.util.List;

public class ProfileKey {
    private String name;
    private List<String> availableValues;
    private List<String> selectedValues;

    public ProfileKey(String name) {
        this.name = name;
        this.availableValues = new ArrayList<>();
        this.selectedValues = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<String> getAvailableValues() {
        return availableValues;
    }

    public void setAvailableValues(List<String> availableValues) {
        this.availableValues = availableValues;
    }

    public List<String> getSelectedValues() {
        return selectedValues;
    }

    public void setSelectedValues(List<String> selectedValues) {
        this.selectedValues = selectedValues;
    }
}