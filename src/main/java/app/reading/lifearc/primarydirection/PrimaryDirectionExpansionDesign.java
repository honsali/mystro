package app.reading.lifearc.primarydirection;

import java.util.List;

/**
 * Session 13 implementation plan for expanding local/research primary directions.
 *
 * <p>This is a method-decision document in code. It remains separate from the public calculator
 * methods so the current normalized direct-zodiacal output stays unchanged while explicit variant
 * methods add later local/research slices.</p>
 *
 * <h2>Scope decisions</h2>
 * <ul>
 *   <li>Keep the existing direct normalized zodiacal hyleg/angle table unchanged.</li>
 *   <li>Support converse normalized zodiacal directions through explicit variant methods, as
 *       clearly labelled converse rows or a separately labelled local/research table. Direct rows
 *       must remain distinguishable from converse rows.</li>
 *   <li>Support mundane/semi-arc work only as a later, separate prototype. Do not silently replace
 *       or merge it with the normalized zodiacal directions.</li>
 *   <li>Do not implement anaereta selection, deterministic lifespan prediction, or death timing.
 *       The selected hyleg may be directed as vitality evidence only.</li>
 * </ul>
 *
 * <h2>Variant method notes</h2>
 * <ul>
 *   <li>Normalized direct zodiacal: current method; selected hyleg and Ascendant use oblique
 *       ascension at birth latitude, Midheaven uses right ascension, and one equatorial degree maps
 *       to one mean tropical year.</li>
 *   <li>Normalized converse zodiacal: use the same coordinates, significators, and promissors as
 *       the current normalized direct table, but reverse the arc direction. Method labels say
 *       {@code CONVERSE}; direct output remains unchanged unless a caller explicitly asks for an
 *       expanded direct+converse table.</li>
 *   <li>Mundane/semi-arc prototype: calculate in a separate table/Markdown file with a prototype
 *       method id, finite-arc tests, and strong caveats. The first prototype should use traditional
 *       planet bodies before adding rays.</li>
 * </ul>
 */
public final class PrimaryDirectionExpansionDesign {
    public static final boolean SUPPORT_CONVERSE_ZODIACAL_NEXT = true;
    public static final boolean SUPPORT_MUNDANE_SEMI_ARC_AS_SEPARATE_PROTOTYPE = true;
    public static final String CURRENT_DIRECT_METHOD_ID = PrimaryDirectionCalculator.METHOD_ID;
    public static final String CONVERSE_ZODIACAL_METHOD_ID = "PTOLEMAIC_NORMALIZED_DIRECT_CONVERSE_ZODIACAL_PRIMARY_DIRECTIONS_V1";
    public static final String MUNDANE_SEMI_ARC_PROTOTYPE_METHOD_ID = "PTOLEMAIC_NORMALIZED_MUNDANE_SEMI_ARC_PRIMARY_DIRECTION_PROTOTYPE_V1";
    public static final String ANAERETA_AND_LONGEVITY_TIMING_POLICY = "DEFERRED_OUT_OF_SCOPE; HYLEG_DIRECTIONS_ARE_VITALITY_EVIDENCE_ONLY; NO_DETERMINISTIC_LIFESPAN_OR_DEATH_TIMING";
    public static final String ZODIACAL_CONVERSE_ARC_RULE = "DIRECT_ARC=TARGET_COORDINATE_MINUS_SIGNIFICATOR_COORDINATE_FORWARDS; CONVERSE_ARC=SIGNIFICATOR_COORDINATE_MINUS_TARGET_COORDINATE_BACKWARDS; BOTH_WRAP_TO_POSITIVE_0_360_ARCS";
    public static final String SEMI_ARC_PROTOTYPE_RULE = "SEPARATE_LOCAL_RESEARCH_PROTOTYPE; EQUATORIAL_RA_DECLINATION_WITH_BIRTH_LATITUDE_SEMI_ARC_GEOMETRY; TRADITIONAL_PLANET_BODY_PROMISSORS_FIRST; RAYS_DEFERRED_UNTIL_METHOD_LABELS_AND_TESTS_ARE_CLEAR";

    private static final List<PurposeSet> PURPOSE_SETS = List.of(
            new PurposeSet(
                    "VITALITY_HYLEG",
                    "Direct the selected Ptolemaic hyleg as vitality/prorogation evidence only.",
                    List.of("SELECTED_PTOLEMAIC_HYLEG", "ASCENDANT_FALLBACK_ONLY_WHEN_NO_HYLEG_IS_AVAILABLE"),
                    List.of("SEVEN_TRADITIONAL_PLANET_BODIES", "PTOLEMAIC_RAYS_CONJUNCTION_SEXTILE_SQUARE_TRINE_OPPOSITION"),
                    List.of(DirectionVariant.NORMALIZED_ZODIACAL_DIRECT, DirectionVariant.NORMALIZED_ZODIACAL_CONVERSE, DirectionVariant.MUNDANE_SEMI_ARC_PROTOTYPE),
                    "No anaereta, death timing, or deterministic lifespan judgment."
            ),
            new PurposeSet(
                    "ANGULAR_LIFE_ARC_FRAME",
                    "Direct the main natal angles as broad life-arc timing evidence.",
                    List.of("ASCENDANT", "MIDHEAVEN"),
                    List.of("SEVEN_TRADITIONAL_PLANET_BODIES", "PTOLEMAIC_RAYS_CONJUNCTION_SEXTILE_SQUARE_TRINE_OPPOSITION"),
                    List.of(DirectionVariant.NORMALIZED_ZODIACAL_DIRECT, DirectionVariant.NORMALIZED_ZODIACAL_CONVERSE, DirectionVariant.MUNDANE_SEMI_ARC_PROTOTYPE),
                    "Descendant and IC are deferred unless a later method label explicitly adds them."
            ),
            new PurposeSet(
                    "TOPIC_SIGNIFICATORS_ZODIACAL_ONLY",
                    "Optional lower-risk topic timing from already emitted natal-description anchors.",
                    List.of("LOT_FORTUNE", "LOT_SPIRIT", "SUN", "MOON"),
                    List.of("SEVEN_TRADITIONAL_PLANET_BODIES", "PTOLEMAIC_RAYS_CONJUNCTION_SEXTILE_SQUARE_TRINE_OPPOSITION"),
                    List.of(DirectionVariant.NORMALIZED_ZODIACAL_DIRECT, DirectionVariant.NORMALIZED_ZODIACAL_CONVERSE),
                    "If added, keep these separate from the current hyleg/angle baseline and label them as topic significators."
            ),
            new PurposeSet(
                    "MUNDANE_SEMI_ARC_PROTOTYPE_BODY_CONTACTS",
                    "Prototype one mundane/semi-arc variant without pretending final historical authority.",
                    List.of("SELECTED_PTOLEMAIC_HYLEG", "ASCENDANT", "MIDHEAVEN"),
                    List.of("SEVEN_TRADITIONAL_PLANET_BODIES"),
                    List.of(DirectionVariant.MUNDANE_SEMI_ARC_PROTOTYPE),
                    "Body contacts first; add rays only after prototype labels, caveats, and tests are stable."
            )
    );

    private PrimaryDirectionExpansionDesign() {}

    public static List<PurposeSet> purposeSets() {
        return PURPOSE_SETS;
    }

    public enum DirectionVariant {
        NORMALIZED_ZODIACAL_DIRECT,
        NORMALIZED_ZODIACAL_CONVERSE,
        MUNDANE_SEMI_ARC_PROTOTYPE
    }

    public enum DirectionPolarity {
        DIRECT,
        CONVERSE
    }

    public record PurposeSet(
            String name,
            String purpose,
            List<String> significators,
            List<String> promissors,
            List<DirectionVariant> variants,
            String scopeNote
    ) {
        public PurposeSet {
            significators = significators == null ? List.of() : List.copyOf(significators);
            promissors = promissors == null ? List.of() : List.copyOf(promissors);
            variants = variants == null ? List.of() : List.copyOf(variants);
        }
    }
}
