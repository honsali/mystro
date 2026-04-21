package app.mystro.processor;

import app.common.NativeReportBuilder;

public abstract class MystroProcessor extends MystroProcessorUtil {

    public abstract void populate(NativeReportBuilder builder);
}
