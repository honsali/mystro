package app.local;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import app.chart.model.Subject;

final class LocalOutputIndexMarkdownRenderer {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm xxx");

    String render(Subject subject,
                  LocalDate inquiryDate,
                  Path indexOutput,
                  List<LifeArcAiBriefMarkdownRenderer.FileReference> fileReferences) {
        StringBuilder out = new StringBuilder();
        out.append("# Mystro Local Output Index\n\n");
        out.append("## Subject\n\n");
        out.append("- Subject: `").append(subject.getId()).append("`\n");
        out.append("- Birth date/time: `").append(format(subject.getLocalBirthDateTime())).append("`\n");
        if (inquiryDate != null) {
            out.append("- Inquiry date: `").append(inquiryDate).append("`\n");
        }
        out.append("\n");

        out.append("## Purpose of this directory\n\n");
        out.append("This directory is the local/research Module-2 macro pack for the native. It is calculated timing evidence, not a finished interpretation. ");
        out.append("Use it to ground an analysis in dated chronocrator periods, return charts, releasing periods, directions, and lunar timing. ");
        out.append("The command-line JSON remains natal-description context; the Markdown files are the life-arc timing layer.\n\n");
        out.append("When answering a user question, do not read every long file first. Use this index to pick the specific calculation file needed for evidence.\n\n");

        out.append("## Start here\n\n");
        out.append("1. Use this `index.md` as the map of available calculation files.\n");
        out.append("2. Open `reading_output.json` only when you need the natal promise / chart context behind the timing evidence.\n");
        out.append("3. For broad annual/monthly context, use `annual_profections.md`, `monthly_profections.md`, `firdaria.md`, and `decennials.md`.\n");
        out.append("4. For Zodiacal Releasing macro structure, start with `zodiacal_releasing_l1_all_lots.md`; use `zodiacal_releasing/zr_index.md` only when a specific lot needs deeper L2-L4 detail.\n");
        out.append("5. For directed timing, use `distributions_through_bounds.md`, `distributions_extended.md`, `primary_directions.md`, and `primary_directions_mundane.md`.\n");
        out.append("6. For return/lunar context, use `solar_returns.md`, `solar_return_natal_comparison.md`, and the `lunar_timing*.md` files.\n");
        out.append("7. Date zoom directories such as `20240615/` are separate high-resolution packs; open their `zoom_overview.md` first.\n\n");

        out.append("## Output files and aims\n\n");
        out.append("| Output | Link | Aim / contents | How an AI should use it | Boundary |\n");
        out.append("|---|---|---|---|---|\n");
        for (LifeArcAiBriefMarkdownRenderer.FileReference ref : fileReferences) {
            FileAim aim = aimFor(ref);
            out.append("| ").append(ref.label())
                    .append(" | ").append(fileLink(indexOutput, ref.path(), ref.label()))
                    .append(" | ").append(aim.contents())
                    .append(" | ").append(aim.use())
                    .append(" | ").append(aim.boundary())
                    .append(" |\n");
        }
        out.append("\n");

        out.append("## Date zoom packs\n\n");
        out.append("Subdirectories named like `yyyyMMdd/` are bounded high-zoom packs for one requested date and a ±15-day window. ");
        out.append("They are not replacements for the macro files above. Open `<yyyyMMdd>/zoom_overview.md` first, then use the listed zoom files for exact daily/proximate calculations.\n\n");
        out.append("Current zoom-file meanings when present:\n\n");
        out.append("| Zoom file | Aim |\n");
        out.append("|---|---|\n");
        out.append("| `zoom_overview.md` | Index and reading order for that date window. |\n");
        out.append("| `active_periods.md` | Active profections, chronocrators, distributions, solar-return context, and lunar focus rows for the window. |\n");
        out.append("| `zodiacal_releasing_active.md` | Active Zodiacal Releasing chains and boundaries inside the window. |\n");
        out.append("| `daily_profections.md` | Daily profection rows across the ±15-day window. |\n");
        out.append("| `planetary_hours.md` | Planetary hours for the focus planetary day, sunrise to next sunrise. |\n");
        out.append("| `lunar_30d.md` | Moon sign ingresses, lunations/eclipses, and exact Moon hits to daily-activated targets. |\n");
        out.append("| `solar_return_focus.md` | Active solar-return chart, natal overlays, and conjunctions. |\n");
        out.append("| `directions_30d.md` | Active distribution bounds plus exact distribution / primary-direction contacts inside the window. |\n");
        out.append("| `transits_30d.md` | Exact transit hits inside the bounded window using activated targets. |\n\n");

        out.append("## Large-file notes\n\n");
        out.append("- `lunar_timing.md` is the compact lunar overview; `lunar_timing_eclipses.md` contains all true-eclipse and mean-node candidate rows; `lunar_timing_full.md` contains the full lunar-return and lunation tables.\n");
        out.append("- `zodiacal_releasing_l1_all_lots.md` gives one compact L1 macro table across all emitted lots.\n");
        out.append("- `zodiacal_releasing/zr_index.md` links each per-lot timeline so an AI reader does not need to load all long releasing files at once.\n");
        out.append("- Every large generated file starts with summary and active/top rows before full detail.\n");
        out.append("- These files provide calculation rows and evidence. Final interpretation, ranking, and synthesis should be done explicitly by the downstream AI/user, not assumed from file order alone.\n\n");
        return out.toString();
    }

    private FileAim aimFor(LifeArcAiBriefMarkdownRenderer.FileReference ref) {
        return switch (ref.label()) {
            case "Life-arc AI brief" -> new FileAim(
                    "Compact map of active periods and recommended reading order.",
                    "Read first when an AI needs fast orientation before opening larger evidence files.",
                    "Orientation only; verify details in the linked technique files."
            );
            case "Output index" -> new FileAim(
                    "This master map of local files and their purpose.",
                    "Use to decide which file to open next and avoid loading long tables unnecessarily.",
                    "Does not contain the calculations themselves."
            );
            case "Reading bundle JSON" -> new FileAim(
                    "Natal-description JSON: subject metadata, Valens-led natal chart, lots, dignities, topics, and natal promise.",
                    "Use as the root natal context for timing claims made from Module-2 files.",
                    "Does not contain life-arc timing tables."
            );
            case "Annual profections" -> new FileAim(
                    "Year-by-year profected sign/house, Lord of the Year, and activated natal points/lots.",
                    "Use to identify the annual activation frame for a date or age.",
                    "Annual layer only; use monthly/daily zoom files for finer timing."
            );
            case "Monthly profections" -> new FileAim(
                    "Month-by-month profected sign/house, Lord of the Month, and activated natal points/lots.",
                    "Use after annual profections to narrow activation to a month.",
                    "Macro month layer only; daily profections live in date zoom packs."
            );
            case "Firdaria" -> new FileAim(
                    "Medieval planetary main and partner chronocrator periods over the life span.",
                    "Use to see which planets hold broad period rulership at a date/age.",
                    "Broad chronocrator context; not an event trigger by itself."
            );
            case "Decennials" -> new FileAim(
                    "Normalized decennial main/partner chronocrator periods.",
                    "Use as another broad period-rulership layer to compare with firdaria/profections.",
                    "Broad structural timing; not fine-grain date timing."
            );
            case "Zodiacal Releasing L1 all lots" -> new FileAim(
                    "Compact L1 Zodiacal Releasing macro table for every emitted lot.",
                    "Use first for releasing because it shows the long arcs across lots without loading every per-lot file.",
                    "L1 only; open per-lot files for L2-L4 detail."
            );
            case "Zodiacal Releasing index" -> new FileAim(
                    "Index of detailed per-lot Zodiacal Releasing files.",
                    "Use when a specific lot, topic, or releasing chain needs deeper inspection.",
                    "Index only; each linked file may be long."
            );
            case "Distributions through bounds" -> new FileAim(
                    "Ascendant distribution through Egyptian bounds with bound rulers and exact body/ray contacts.",
                    "Use for the baseline distribution chronocrator and contacts by age/date.",
                    "Ascendant baseline only; see extended distributions for other directed points."
            );
            case "Extended distributions through bounds" -> new FileAim(
                    "Distribution-through-bounds tables for selected hyleg, Midheaven, Fortune, Spirit, Sun, and Moon.",
                    "Use to compare distribution activity across major directed points.",
                    "Extended research layer; keep the directed point clear when citing evidence."
            );
            case "Primary directions" -> new FileAim(
                    "0-100 normalized zodiacal primary-direction contacts for selected hyleg/core angles.",
                    "Use for direction contacts by date/age and compare with distributions/returns.",
                    "Research calculation; no deterministic lifespan or death-timing conclusion."
            );
            case "Mundane primary-direction prototype" -> new FileAim(
                    "Separately labelled mundane/semi-arc body-contact prototype.",
                    "Use only as supporting direction evidence when it repeats other timing signals.",
                    "Prototype, not final historical authority; do not treat as replacement for normalized zodiacal directions."
            );
            case "Solar returns" -> new FileAim(
                    "Exact annual solar-return charts across the configured age span.",
                    "Use to inspect the return chart for a particular birthday year.",
                    "Raw return charts; use the comparison file for natal overlays and annual context."
            );
            case "Solar return to natal comparison" -> new FileAim(
                    "0-100 solar-return overlays to natal houses, profection context, and conjunction/contact rows.",
                    "Use for a particular age/year to connect its return chart back to natal promise.",
                    "Comparison layer only; not a complete narrative reading."
            );
            case "Lunar timing overview" -> new FileAim(
                    "Overview of lunar returns, lunations, and eclipse pointers across the configured age span.",
                    "Use for lunar timing orientation before opening full lunar/eclipses tables.",
                    "Overview only; exact full rows are split into companion files."
            );
            case "Lunar timing eclipse tables" -> new FileAim(
                    "True global eclipses with subject-location visibility plus mean-node eclipse candidates.",
                    "Use when eclipse evidence matters to the question or repeats active signs/houses/planets.",
                    "Eclipse evidence table; interpretation requires cross-checking with natal and active timing layers."
            );
            case "Lunar timing full tables" -> new FileAim(
                    "Full 0-100 lunar-return and lunation tables.",
                    "Use only when exact lunar rows outside the overview are needed.",
                    "Large file; avoid as first read."
            );
            case "Life-arc synthesis" -> new FileAim(
                    "Grouped active evidence by repeated signs, houses, planets, points, lots, and aspects.",
                    "Use to see which symbols are repeatedly activated across methods before forming an interpretation.",
                    "Evidence grouping, not final judgment or ranking of life outcomes."
            );
            case "Topic synthesis index" -> new FileAim(
                    "Index of practical topic packets combining natal promise references with active timing evidence.",
                    "Use for user questions about love, career, family, body, money, travel, vocation, and similar topics.",
                    "Topic packets organize evidence; downstream AI still performs interpretation."
            );
            default -> new FileAim(
                    ref.purpose(),
                    "Use when its label matches the user's question or when referenced by the AI brief/index.",
                    "Calculation evidence; avoid unsupported narrative leaps."
            );
        };
    }

    private String fileLink(Path indexOutput, Path target, String label) {
        if (target == null) {
            return "—";
        }
        String href = target.toString();
        Path base = indexOutput == null ? null : indexOutput.getParent();
        if (base != null) {
            try {
                href = base.normalize().relativize(target.normalize()).toString();
            } catch (IllegalArgumentException ignored) {
                href = target.toString();
            }
        }
        return "[" + label + "](" + href.replace('\\', '/') + ")";
    }

    private String format(OffsetDateTime dateTime) {
        return dateTime.format(DATE_TIME);
    }

    private record FileAim(String contents, String use, String boundary) {}
}
