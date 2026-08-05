package app.reading.description.common.calculator;

import app.chart.AstroMath;
import app.chart.CalculationContext;
import app.chart.data.Planet;
import app.chart.data.SyzygyType;
import app.reading.description.common.model.PrenatalSyzygyEntry;
import app.ephemeris.SweConst;
import app.ephemeris.SwissEphAdapter;
import app.chart.search.SyzygyEventSearch;
import app.chart.search.SyzygyEventSearch.SyzygyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shared doctrine-layer helper for finding the prenatal lunation.
 *
 * <p>
 * The event search itself is astronomical geometry. The selected point of the syzygy is
 * doctrine-sensitive, so doctrines can subclass and override
 * {@link #syzygyLongitude(SyzygyType, double, double, double, CalculationContext)}.
 */
public class SyzygyCalculator {

    private static final Logger LOG = LoggerFactory.getLogger(SyzygyCalculator.class);
    private static final SyzygyEventSearch EVENT_SEARCH = new SyzygyEventSearch();

    public PrenatalSyzygyEntry calculate(CalculationContext ctx) {
        SyzygyEvent syzygy;
        try {
            syzygy = EVENT_SEARCH.previous(ctx.getFullJulianDay(), ctx);
        } catch (IllegalArgumentException exception) {
            LOG.error("subject={} {}", ctx.getSubject().getId(), exception.getMessage());
            throw new IllegalArgumentException("Calculation failed. See application logs.", exception);
        }
        double sunLongitude = ctx.longitudeFor(Planet.SUN, SweConst.SE_SUN, syzygy.julianDay());
        double moonLongitude = ctx.longitudeFor(Planet.MOON, SweConst.SE_MOON, syzygy.julianDay());
        double syzygyLongitude = syzygyLongitude(syzygy.type(), sunLongitude, moonLongitude, syzygy.julianDay(), ctx);
        double natalAscendant = AstroMath.normalize(ctx.getAscmc()[0]);
        return new PrenatalSyzygyEntry(syzygy.type(), syzygy.julianDay(), SwissEphAdapter.julianDayUtToUtc(syzygy.julianDay()), syzygyLongitude, AstroMath.signOf(syzygyLongitude), AstroMath.degreeInSign(syzygyLongitude), ctx.houseOf(syzygyLongitude, natalAscendant), sunLongitude, moonLongitude,
                AstroMath.rawAngularSeparation(sunLongitude, moonLongitude), AstroMath.signOf(sunLongitude), AstroMath.signOf(moonLongitude));
    }

    /**
     * Default Hellenistic selection: conjunction uses the shared Sun/Moon longitude; opposition uses
     * the Moon's longitude.
     */
    protected double syzygyLongitude(SyzygyType type, double sunLongitude, double moonLongitude, double julianDay, CalculationContext ctx) {
        return type == SyzygyType.FULL_MOON ? moonLongitude : sunLongitude;
    }

}
