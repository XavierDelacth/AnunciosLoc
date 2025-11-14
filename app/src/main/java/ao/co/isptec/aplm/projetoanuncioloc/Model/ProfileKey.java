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

    public ProfileKey(String name, List<String> availableValues) {
        this.name = name;
        this.availableValues = availableValues != null ? availableValues : new ArrayList<>();
        this.selectedValues = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getAvailableValues() {
        return availableValues;
    }

    public void setAvailableValues(List<String> availableValues) {
        this.availableValues = availableValues != null ? availableValues : new ArrayList<>();
    }

    public List<String> getSelectedValues() {
        return selectedValues;
    }

    public void setSelectedValues(List<String> selectedValues) {
        this.selectedValues = selectedValues != null ? selectedValues : new ArrayList<>();
    }

    public boolean isValueSelected(String value) {
        return selectedValues.contains(value);
    }

    public void selectValue(String value) {
        if (!selectedValues.contains(value)) {
            selectedValues.add(value);
        }
    }

    public void deselectValue(String value) {
        selectedValues.remove(value);
    }

    public void toggleValue(String value) {
        if (isValueSelected(value)) {
            deselectValue(value);
        } else {
            selectValue(value);
        }
    }

    public boolean hasSelectedValues() {
        return !selectedValues.isEmpty();
    }

    public int getSelectedCount() {
        return selectedValues.size();
    }

    public int getAvailableCount() {
        return availableValues.size();
    }
}