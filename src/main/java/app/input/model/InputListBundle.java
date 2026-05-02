package app.input.model;

import java.util.List;
import app.doctrine.Doctrine;

public final class InputListBundle {

    private List<String> subjectIds;
    private List<String> doctrineIds;
    private List<Subject> subjects;
    private List<Doctrine> doctrines;
    private CalculationSetting calculationSetting;

    public InputListBundle(List<String> subjectIds, List<String> doctrineIds) {
        this.subjectIds = subjectIds;
        this.doctrineIds = doctrineIds;
    }

    public List<String> getSubjectIds() {
        return subjectIds;
    }

    public void setSubjectIds(List<String> subjectIds) {
        this.subjectIds = subjectIds;
    }

    public List<String> getDoctrineIds() {
        return doctrineIds;
    }

    public void setDoctrineIds(List<String> doctrineIds) {
        this.doctrineIds = doctrineIds;
    }

    public List<Subject> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<Subject> subjects) {
        this.subjects = subjects;
    }

    public List<Doctrine> getDoctrines() {
        return doctrines;
    }

    public void setDoctrines(List<Doctrine> doctrines) {
        this.doctrines = doctrines;
    }

    public CalculationSetting getCalculationSetting() {
        return calculationSetting;
    }

    public void setCalculationSetting(CalculationSetting calculationSetting) {
        this.calculationSetting = calculationSetting;
    }
}
