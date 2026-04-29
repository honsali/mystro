package app.model.input;

import app.doctrine.Doctrine;

public final class Input {

    private final Subject subject;
    private final Doctrine doctrine;
    private final CalculationSetting calculationSetting;

    public Input(Subject subject, Doctrine doctrine, CalculationSetting calculationSetting) {
        this.subject = subject;
        this.doctrine = doctrine;
        this.calculationSetting = calculationSetting;
    }

    public Subject getSubject() {
        return subject;
    }

    public Doctrine getDoctrine() {
        return doctrine;
    }

    public CalculationSetting getCalculationSetting() {
        return calculationSetting;
    }



}
