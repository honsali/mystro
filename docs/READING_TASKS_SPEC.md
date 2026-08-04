# Mystro Reading Tasks Specification

## Governing principle

The choice of technical apparatus follows the analytical objective. Each reading task asks a different kind of question and therefore uses a different traditional toolset.

Mystro's canonical taxonomy contains six reading tasks:

1. Natal description
2. Life-arc prediction
3. Horary / event prediction
4. Elections
5. Mundane prediction
6. Medical astrology

Each task below is described by its question, primary apparatus, useful annexes, main historical line, and distinctive marker.

---

## 1. Natal description — who is this person?

### Typical questions

- What is the person's body, temperament, and natural style?
- What are the person's intellectual and emotional qualities?
- What are their natural inclinations, social rank, fortune, and vulnerabilities?
- What describes vitality and the sustaining life principle?
- What describes family topics such as parents, siblings, marriage, and children as natal topics?

### Primary apparatus

- Sect as the fundamental diurnal/nocturnal polarity
- Whole-sign houses for topical structure
- Essential dignity and debility: domicile, exaltation, triplicity, term, face, detriment, fall
- Lot of Fortune for body, material condition, and the involuntary field of life
- Lot of Spirit for soul, intention, action, and vocation
- Hermetic lots such as Eros, Necessity, Courage, Victory, and Nemesis
- Valens Lot of Basis/Foundation as a chart-specific foundation lot when included in the natal-description apparatus
- Triplicity lords for broad life phases, implemented as `natalChart.triplicityLifePhases` with symbolic age spans when hyleg/alcocoden vitality years are available
- Planetary joys
- Mercury configuration for intellect and speech, implemented as `natalChart.mercuryConfiguration`
- Moon configuration for body, instinct, memory, and fluctuation, implemented as `natalChart.moonConfiguration`
- Hyleg and alcocoden for vitality doctrine, implemented as `natalChart.ptolemaicHylegAlcocoden` with symbolic `vitalityYears`
- Decans/faces and sign-based character material
- Dodecatemoria and related micro-zodiacal descriptions
- Paranatellonta and Sphaera Barbarica-style sign lore as optional documentation/local-reference material, with `natalChart.fixedStars` retaining chart-specific star-level conjunction data
- Melothesia: signs mapped to body regions

### Primary masters and sources

- Ptolemy, *Tetrabiblos* III–IV: body, character, illness, parents, siblings, marriage, children, profession, hyleg doctrine
- Vettius Valens, *Anthology* I–III: lots, sect, joys, examples, natal delineation practice
- Dorotheus of Sidon, *Carmen* I–II: house effects and early natal delineation
- Firmicus Maternus, *Mathesis* III–VI: extensive Latin natal delineations
- Manilius, *Astronomica* II–IV: signs, decans, paranatellonta, poetic character material
- Hephaistio of Thebes, *Apotelesmatica* I–II: synthesis of Ptolemy and Dorotheus
- Rhetorius: compendium of delineations
- Abu Maʿshar, *Great Introduction*: systematic reformulation
- Bonatti, *Liber Astronomiae* treatise V: Latin medieval transmission

### Distinctive marker

Natal description is an anatomical snapshot of the native. It describes the natal promise rather than unfolding it through time.

---

## 2. Life-arc prediction — when, and under what theme?

### Typical questions

- What period of life is active now?
- When do marriage, career elevation, crisis, relocation, or prosperity become likely topics?
- What themes dominate a year, decade, or longer period?
- What natal promises are activated by the current chronocrators?

### Primary apparatus

- Annual, monthly, and daily profections as doctrine apparatus; local research calculators currently produce annual/monthly profection Markdown tables in the macro pack and daily profection Markdown rows in the bounded zoom pack
- Lord of the Year and activated houses; current local Markdown output includes the profected house/sign, Lord of the Year, activated natal points/lots, and matching natal topic-assessment refs
- Zodiacal Releasing from emitted lots; local research calculators currently generate a compact all-lots L1 macro file plus per-lot L1-L4 timelines with an index file
- Distributions through the bounds / bound chronocrators; local research calculation currently covers the baseline Ascendant distribution through Egyptian bounds with body/ray contacts, plus extended selected-point distributions for the selected Ptolemaic hyleg, Midheaven, Valens Fortune, Valens Spirit, Sun, and Moon
- Decennials; local research calculation currently covers normalized main/partner periods
- Firdaria; local research calculation currently covers medieval main/partner periods
- Primary directions; local research calculation currently covers the direct normalized zodiacal selected-hyleg/core-angle baseline, an explicit direct+converse normalized zodiacal variant, a separately labelled mundane/semi-arc prototype, and bounded zoom direction/contact rows; anaereta selection, deterministic lifespan prediction, death timing, and unvalidated historical variants remain out of scope
- Solar returns; local research calculation currently covers exact solar returns, solar-return-to-natal comparison, and a bounded zoom focus file for the active return chart / natal overlays / conjunctions
- Lunations and lunar returns; local research calculation currently covers exact lunar returns, exact new/full Moon lunations, Swiss Ephemeris true global eclipse rows with magnitude/contact data and subject-location visibility flags, mean-node eclipse candidates retained as supporting reference evidence, and bounded zoom Moon sign ingresses / exact Moon hits to daily-activated targets
- Classical transits to activated natal points are reserved for the bounded high-zoom range; they are not called by the current 0-100 macro dump and are emitted by the local zoom pack under `output/<alias>/<yyyyMMdd>/`
- Planetary hours are emitted in the bounded zoom pack for the focus planetary day; they are calculation rows only, not a web endpoint
- Synthesis/evidence grouping; local research calculation currently groups active life-arc evidence by repeated signs, houses, planets, points, lots, and aspects with normalized evidence-density weights, then renders a compact AI brief, topic synthesis packets, an output index, and split overview/full Markdown files for large output families
- Alcocoden and longevity doctrine where the reading task includes lifespan analysis

### Primary masters and sources

- Valens, *Anthology* IV–IX: releasing, distributions, decennials, ascensions, extensive examples
- Dorotheus, *Carmen* III–IV: profections and lord of the year
- Ptolemy, *Tetrabiblos* III–IV: prorogations and longevity
- Masha'allah: solar returns and Arabic transmission
- Abu Maʿshar: firdaria and great years
- Sahl ibn Bishr: chronocrator practice
- Bonatti, *Liber Astronomiae*: medieval compilation
- Regiomontanus and Placidus: direction tables and semi-arc method
- Morin, *Astrologia Gallica* XXII–XXIV: solar returns and directions

### Distinctive marker

Life-arc prediction turns the natal chart into a score played through time by chronocrators and return charts.

---

## 3. Horary / event prediction — this precise event?

### Typical questions

- Will the lost object be found?
- Will the marriage, lawsuit, journey, sale, message, or agreement happen?
- Is the matter safe, corrupted, delayed, prohibited, or perfected?
- When does the event perfect?

### Primary apparatus

- A chart for the moment of the question or event
- Quadrant houses, especially Alcabitius or Regiomontanus according to the selected horary lineage
- Considerations before judgment: early/late Ascendant, void Moon, Saturn in the 7th, via combusta, and related procedural checks
- Significator assignment by house rulership
- Moon as universal co-significator
- Applying and separating aspects
- Reception by domicile, exaltation, triplicity, term, and face
- Translation and collection of light
- Prohibition, refranation, frustration, and other impediments
- Antiscia and contra-antiscia
- Solar conditions such as combustion, under beams, and cazimi
- Moiety of orbs
- Testimony counting and procedural judgment rules

### Primary masters and sources

- Dorotheus, *Carmen* V: catarchic root of the genre
- Sahl ibn Bishr, *Fifty Precepts* and *Book of Judgments*: Arabic codification
- Masha'allah: horary applications
- Abu ʿAli al-Khayyat and Al-Kindi: judgment literature
- Bonatti, *Liber Astronomiae* treatise VI: medieval considerations and procedure
- Claude Dariot: French bridge tradition
- William Lilly, *Christian Astrology* book II: English synthesis with solved examples
- John Gadbury and Henry Coley: English continuation

### Distinctive marker

Horary is procedural and focused. The chart answers a specific question through significators, perfection, impediment, and testimony.

---

## 4. Elections — choosing the right moment

### Typical questions

- When should someone marry, sign, travel, operate, plant, build, begin study, or launch a venture?
- Which available moment best supports the intended action?
- Which moment minimizes the relevant risks?

### Primary apparatus

- A target action classified by house, planet, and topic
- Electional strengthening of the relevant significator and house
- Lunar phase: waxing for increase, waning for reduction
- Moon condition, application, speed, and sign
- Benefics and malefics relative to the relevant angles
- Avoidance of void Moon, severe combustion, destructive malefic angularity, and action-specific contraindications
- Planetary hours where useful
- Lunar mansions where represented by the selected electional lineage
- Medical election rules for surgery, bleeding, purging, or treatment timing when the action is medical

### Primary masters and sources

- Dorotheus, *Carmen* V: systematic electional source
- Sahl ibn Bishr, *Book of Elections*: Arabic codification
- Hephaistio, *Apotelesmatica* III: late Greek treatment
- Masha'allah and Abu Maʿshar: applications
- Al-Biruni: critical remarks and definitions
- Bonatti, *Liber Astronomiae* treatise VII: Latin synthesis
- Lilly: English electional practice

### Distinctive marker

Elections are active. The reading chooses a moment suited to an intended action.

---

## 5. Mundane prediction — what happens to the world?

### Typical questions

- What does a year or season indicate for a place?
- What happens to a kingdom, city, ruler, dynasty, harvest, weather pattern, or collective event?
- What does an eclipse, ingress, comet, or great conjunction signify for a region?

### Primary apparatus

- Aries ingress and seasonal ingresses for a location
- Solar and lunar eclipses
- Great Saturn-Jupiter conjunction cycles: small, medium, and great conjunctions
- Mundane lots such as king, kingdom, religion, and related political lots where supported
- Comets and visible celestial phenomena
- Sphaera Barbarica and paranatellonta for eclipse and omen localization
- Astrological climates and geographical rulerships
- World firdaria or large-scale planetary periods where supported by the selected mundane lineage

### Primary masters and sources

- Ptolemy, *Tetrabiblos* II: countries, peoples, weather, and eclipses
- Masha'allah, *Book of Conjunctions*: Arabic foundation of conjunctional mundane astrology
- Abu Maʿshar, *De Magnis Coniunctionibus*: major mundane summa
- Al-Kindi: mundane and weather material
- Bonatti: revolutions of the years and conjunctions
- Pierre d'Ailly and John of Eschenden: medieval historical applications
- Cardano, Kepler, Lilly, Gadbury, and Coley: later mundane and almanac traditions

### Distinctive marker

Mundane astrology attaches the chart to a place, political body, season, cosmic cycle, or public event rather than to a private nativity.

---

## 6. Medical astrology — illness, temperament, and treatment timing

### Typical questions

- What is the nature of an illness within the traditional astrological-medical framework?
- What is the patient's temperament and vulnerability pattern?
- What are the critical days of the illness?
- Which treatment timing is astrologically appropriate within the selected traditional medical lineage?

### Primary apparatus

- Decumbiture chart or appropriate illness/question chart
- Moon as principal significator of the sick body
- Critical days based on lunar motion from the decumbiture position
- Crisis indications from lunar aspects and condition
- Humoral analysis by planets, signs, elements, and qualities
- Natal temperament as the patient's baseline constitution
- Melothesia: zodiacal signs and body regions
- Planetary hours for administering medicine, bleeding, purging, and related procedures
- Herbal and planetary correspondences where represented by the selected medical lineage

### Primary masters and sources

- Hippocratic and Galenic medicine: humoral framework
- Ptolemy, *Tetrabiblos*: illness and body doctrine
- Pietro d'Abano, *Conciliator*: scholastic medico-astrological synthesis
- Marsilio Ficino, *De Vita*: astral medicine of melancholy and vitality
- Cornelius Agrippa: correspondences
- Paracelsus: alchemical-medical variant
- Nicholas Culpeper, *English Physician* and *Complete Herbal*: English popular medical astrology
- Richard Saunders: English medical practitioner tradition

### Distinctive marker

Medical astrology combines diagnosis, prognosis, constitution, crisis timing, and treatment timing inside a traditional humoral framework.
