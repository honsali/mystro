# Questions à poser à l’IA d’analyse Mystro

Ces questions sont conçues pour exploiter pleinement un JSON de lecture Mystro produit par la ligne de commande, ainsi que les rapports Markdown locaux produits par les calculateurs de recherche Life-arc quand ils existent (`output/<alias>/*.md`). Elles couvrent les sections importantes du JSON et des rapports de calcul afin que rien d’essentiel ne soit oublié, et elles guident l’IA pour rester ancrée dans les données calculées, distinguer les doctrines, et éviter les jugements déterministes.

L’ordre est progressif : on commence par la synthèse, on traverse les grands thèmes natals, puis on plonge dans les zones plus techniques (vitalité symbolique, dodécatémories, étoiles fixes, méta-questions), on ajoute si besoin la lecture temporelle Life-arc à partir des rapports Markdown locaux, et on termine par une lecture intégrative.

Ces questions forment une **banque de lecture** : il n’est pas nécessaire de toutes les poser pour chaque thème. On peut sélectionner les sections utiles selon le niveau de détail souhaité et selon les sujets que la personne concernée veut explorer.

---

## Cadre méthodologique à donner à l’IA en début de conversation

Avant la première question, il est utile de coller ce bloc une seule fois pour cadrer toute la conversation :

> Lis ce thème comme une nativité hellénistique/traditionnelle conduite par Valens, enrichie par des annexes ptolemaïques, dorothéennes, hermétiques, étoiles fixes et dodécatémories quand le JSON les inclut. Si je fournis aussi des rapports Markdown locaux Life-arc (`life_arc_synthesis.md`, profections, retours solaires/lunaires, directions, distributions, releasing, etc.), utilise-les comme données calculées temporelles. Pour **chaque réponse** :
>
> 1. distingue clairement **faits calculés** (positions, maisons, secte, dignités, aspects, lots), **témoignages astrologiques** (ce qu’ils signifient traditionnellement) et **interprétation humanisée** ;
> 2. cite explicitement les champs du JSON ou les sections/lignes Markdown sur lesquels tu t’appuies (ex. `points.MOON`, `topicAssessments.topic=MARRIAGE_UNIONS`, `derivedHouseFrames.fromFortune`, `lotAssessments.lot=FORTUNE`, `life_arc_synthesis.md > Top evidence groups`) ;
> 3. n’invente jamais une donnée absente. Si une section est omise du JSON ou d’un rapport Markdown, c’est volontaire — pas un échec ;
> 4. évite les prédictions déterministes (santé, durée de vie, événements, destinée amoureuse, succès/échec) ;
> 5. utilise un vocabulaire moderne lisible, dans l’esprit d’un astrologue traditionnel contemporain — sans cadres jungiens, karmiques, vies antérieures ou New Age ;
> 6. quand plusieurs techniques convergent vers une même zone, dis-le explicitement : c’est le cœur du témoignage.

---

## 1. Synthèse et premier contact

1. Peux-tu faire une **synthèse de cette nativité** en distinguant clairement les faits calculés, les témoignages astrologiques et l’interprétation humanisée ?
2. Quels sont les **5 témoignages les plus structurants** de ce thème natal, et pourquoi ? Donne pour chacun le champ JSON sur lequel tu t’appuies.
3. Quelles parties du thème **se répètent ou se renforcent entre plusieurs techniques** : maisons, lots, secte, dignités, configurations, doryphories, étoiles fixes, dodécatémories ?
4. Quels éléments semblent **centraux selon Valens**, et lesquels sont des **annexes** ptolemaïques, dorothéennes, hermétiques ou autres ?
5. Peux-tu éviter toute prédiction déterministe et donner une lecture **strictement symbolique** fondée uniquement sur les données du JSON ?

## 2. Architecture générale du thème

6. À partir de `sect`, du luminaire de secte, du bénéfique et du maléfique de secte, quelle est la **logique de fond** de ce thème — diurne ou nocturne, et quelles planètes deviennent prioritaires ?
7. Quels sont les **signes, maisons et planètes les plus dominants** dans la description natale ? Y a-t-il un élément (feu/terre/air/eau), une modalité (cardinal/fixe/mutable) ou un mode (angulaire/succédent/cadent) prédominant ?
8. À partir de `houseTopicRulers`, quels **maîtres de maison** sont bien placés, lesquels sont en difficulté, et comment cela colore-t-il les domaines de vie correspondants ?
9. À partir de `pairwiseRelations`, quels sont les **aspects par degré les plus serrés** (carrés, oppositions, trigones, sextiles, conjonctions) ? Y a-t-il des **réceptions mutuelles** notables ?
10. Quels **angles** (Asc, MC, Desc, IC) reçoivent des contacts importants, et de la part de quelles planètes ?

## 3. Identité, corps et tempérament

11. À partir de `topicAssessments.topic=BODY_TEMPERAMENT` (`PTOLEMAIC_BODY_V1`), de l’Ascendant, de la Lune, du maître de l’Ascendant et du Lot de Fortune, quel **portrait du corps, du tempérament et du style naturel** ressort ?
12. Quels sont les **contrastes entre Ascendant, Soleil et Lune** ? Où la personne concernée semble-t-elle cohérente, et où semble-t-elle intérieurement partagée ?
13. À partir du `signMelothesia` du fichier compagnon et des planètes en difficulté, quelles **correspondances corporelles traditionnelles** peut-on mentionner symboliquement ? Lecture descriptive uniquement, pas médicale.
14. Quels témoignages indiquent la **manière spontanée de réagir au monde** — premier réflexe, démarche, présence visible ?
15. À partir de `moonPhase` (phase, fraction d’illumination, waxing/waning), que peut-on dire du **tempérament lunaire** spécifique à cette nativité ?

## 4. Esprit, parole, émotion et qualité d’âme

16. À partir de `mercuryConfiguration`, que peut-on dire de l’**intelligence, du langage, de la manière de penser et de communiquer** ? Mercure est-il oriental, occidental, en phase exacte, sous les rayons ?
17. À partir de `moonConfiguration`, que peut-on dire de la **mémoire, des habitudes, des besoins émotionnels et des fluctuations** ? Quelle est sa dernière séparation et sa prochaine application ?
18. Comment **Mercure et la Lune** interagissent-ils ? Est-ce que l’esprit rationnel et le monde émotionnel coopèrent ou se défient ?
19. À partir de `topicAssessments.topic=CHARACTER_SOUL_QUALITY` (`PTOLEMAIC_SOUL_QUALITY_V1`), quelle **qualité d’âme** se dégage — passions, rationalité, intensité, modération ?
20. Quelles **forces et vulnérabilités intellectuelles** ressortent sans tomber dans le jugement psychologique abusif ?

## 5. Vocation, action et direction de vie

21. À partir de `topicAssessments.topic=VOCATION_ACTION` (`PTOLEMAIC_PROFESSION_V1`), du MC, de la maison 10, du Lot de Spirit et de Mercure/Vénus/Mars, quels **types d’actions ou vocations** semblent les plus naturels ?
22. Quelle est la différence entre ce que le thème décrit comme **fortune matérielle** (Fortune, maison 2) et ce qu’il décrit comme **intention/action/vocation** (Spirit, maison 10) ?
23. La vocation semble-t-elle plutôt **visible, discrète, intellectuelle, technique, relationnelle, artistique, combative ou de service** ? Quels témoignages la soutiennent ?
24. À partir de `derivedHouseFrames.fromSpirit`, comment se distribue l’**action volontaire** dans les douze lieux comptés depuis Spirit ?
25. Quels **obstacles ou fragilités** peuvent compliquer l’expression professionnelle, et quels **soutiens** peuvent la renforcer ?

## 6. Fortune, ressources et conditions matérielles

26. À partir du **Lot de Fortune**, de `derivedHouseFrames.fromFortune` et de `lotAssessments.lot=FORTUNE`, quels sont les grands thèmes de **condition matérielle, corps, chance et contrainte** ?
27. À partir du **Lot de Basis/Foundation** (`lotAssessments.lot=BASIS`), que peut-on dire de la **fondation secondaire** du thème — sur quoi la personne concernée s’appuie en pratique ?
28. Que disent les maîtres de la maison 2 et de la maison 8, ainsi que **Jupiter**, sur les ressources, l’argent, la sécurité matérielle et les ressources partagées ?

## 7. Relations, amour et mariage

29. À partir de `topicAssessments.topic=MARRIAGE_UNIONS` (`DOROTHEAN_MARRIAGE_V1`), de Vénus, de la maison 7 et du Lot de Wedding, quels **schémas relationnels majeurs** ressortent ?
30. Quels témoignages indiquent les **besoins affectifs, attirances, difficultés relationnelles ou modes d’attachement** ?
31. Peux-tu distinguer ce qui relève de **Vénus**, de la **maison 7**, du **Lot de Wedding** et des **configurations bénéfiques/maléfiques** sur ces facteurs ?
32. Le **Lot d’Eros** (et d’autres lots hermétiques liés au désir s’ils sont présents) renforce-t-il les témoignages dorothéens, ou ouvre-t-il une autre lecture ?
33. Quels **conseils de conscience relationnelle** peut-on tirer symboliquement de ces témoignages, sans prédire un destin amoureux ?

## 8. Enfants, créativité et plaisir (maison 5)

34. À partir de `topicAssessments.topic=CHILDREN` (`DOROTHEAN_CHILDREN_V1`), de la maison 5, de Jupiter et du Lot des Enfants, que peut-on dire des thèmes natals de **filiation, créativité et fécondité symbolique** ?
35. La maison 5 raconte-t-elle plutôt une histoire de **création artistique, de transmission, de jeu, de plaisir, ou de descendance** ? Quels lots et planètes le confirment ?

## 9. Famille et racines

36. À partir des packets dorothéens `topicAssessments.topic=FATHER`, `topicAssessments.topic=MOTHER`, `topicAssessments.topic=SIBLINGS`, que peut-on dire des **thèmes familiaux natals** sans tomber dans des affirmations factuelles invérifiables ?
37. Quels témoignages concernent le **père**, lesquels concernent la **mère**, et où les traditions (Ptolémée vs Dorothée/Hephaistio) peuvent-elles **différer** ?
38. Les **lots familiaux** (Father, Mother, Siblings) confirment-ils ou contredisent-ils les **maisons familiales** (4, 10, 3) et leurs maîtres ?

## 10. Rang, visibilité et reconnaissance

39. À partir de `topicAssessments.topic=EMINENCE_RANK` (`VALENS_EMINENCE_RANK_V1`), des luminaires, de la maison 10, de **Fortune et Spirit**, des **doryphories** et des **étoiles fixes**, quelle forme de **visibilité ou de reconnaissance** le thème suggère-t-il ?
40. À partir de `doryphories`, quelles planètes accompagnent les luminaires comme **porte-lances** ? Que cela ajoute-t-il en termes de soutien, d’honneur ou de qualité de présence ?
41. Le thème semble-t-il chercher la **reconnaissance publique, la maîtrise discrète, l’influence intellectuelle, l’autorité technique** ou une autre forme de contribution ?

## 11. Vulnérabilités, tensions et zones sensibles

42. À partir de `topicAssessments.topic=VULNERABILITY_INDICATORS` (`PTOLEMAIC_VULNERABILITY_INDICATORS_V1`), des maisons 6, 8 et 12, des maléfices, et des Lots de Nécessité et Némésis, quels sont les **points de fragilité symbolique** ? Sans diagnostic médical.
43. Selon `points` et les sections de configuration planétaire, quelles planètes sont les **plus soutenues**, lesquelles sont **les plus sous tension** ? Pourquoi ?
44. À partir de `beneficMaleficAssessment`, quels **maltraitements et bonifications** sont les plus importants, et comment les comprendre sans fatalisme ?
45. Y a-t-il des **carrés ou oppositions par degré très serrés** entre maléfices, ou entre maléfices et points sensibles (luminaires, Ascendant, Fortune) ? Comment les replacer dans le tableau global ?

## 12. Vitalité symbolique, phases et lunation prénatale

46. À partir de `ptolemaicHylegAlcocoden`, quel **giver of life** et quel **principal dignity lord** sont identifiés ? Comment lire `vitalityYears` comme indicateur **symbolique** et non comme prédiction de durée de vie ?
47. À partir de `triplicityLifePhases`, que disent les **rulers des phases précoce, médiane et tardive** de la vie sur les conditions générales de chaque tiers symbolique ?
48. À partir de `syzygy`, que peut-on dire de la **lunation prénatale** (nouvelle/pleine Lune avant la naissance), de son signe, de sa maison et de son maître ? Cela colore-t-il le contexte natal ?

## 13. Textures secondaires : étoiles fixes, dodécatémories, lore des signes

49. À partir de `fixedStars`, quelles **conjonctions stellaires** sont présentes, et quels thèmes natals déjà visibles viennent-elles renforcer ?
50. À partir de `dodecatemoria`, quels **douzièmes de signe** envoient des planètes ou des angles dans des signes très différents de leur position de base ? Ces déplacements ajoutent-ils une nuance utile ?
51. À partir du `signCharacters` et du `paranatellonta` du fichier compagnon, quels signes occupés portent une **lore constellationnelle** intéressante pour ce thème ?

## 14. Méta-questions et auto-critique

52. Y a-t-il des **contradictions ou tensions internes** dans les témoignages donnés ? Comment les comprendre sans les aplatir ?
53. Si l’on n’a que **dix minutes** pour comprendre l’essentiel de ce thème, quels sont les **trois éléments** par lesquels commencer, et pourquoi ?
54. Peux-tu expliquer ce thème en **trois phrases simples**, comme à une personne sans culture astrologique ?
55. Quelles **questions de vie concrètes** ce thème invite-t-il la personne concernée à se poser, sans lui dicter de réponse ?
56. Qu’est-ce que ce thème **ne dit pas** ou **ne permet pas de conclure** ? Où l’interprétation s’appuie-t-elle sur du symbolique plutôt que du calculé ?

## 15. Lecture intégrative finale

57. Si tu devais donner une **lecture en trois niveaux — nature profonde, défis principaux, meilleure manière d’honorer ce thème** — que dirais-tu en citant les éléments précis du JSON pour chaque niveau ?
58. Pour terminer : **quelle est la phrase centrale** de ce thème — celle qu’il faudrait retenir si tout le reste devait s’effacer ?

## 16. Lecture Life-arc à partir des rapports Markdown locaux

Ces questions s’utilisent si l’on fournit à l’IA les fichiers locaux générés sous `output/<alias>/`, en particulier `life_arc_synthesis.md` et éventuellement les fichiers techniques complets. Le bon ordre de lecture est généralement : `life_arc_synthesis.md`, puis `annual_profections.md`, `monthly_profections.md`, `solar_return_natal_comparison.md`, `monthly_transit_checkpoints.md`, `primary_directions.md`, `distributions_through_bounds.md`, `firdaria.md`, `decennials.md`, `lunar_timing.md`, et `zodiacal_releasing/index.md`.

59. À partir de `life_arc_synthesis.md`, quels sont les **groupes d’évidence les plus répétés** par signe, maison, planète, point, lot et aspect ? Ne donne pas encore de prédiction : décris seulement les zones activées.
60. Quels témoignages temporels convergent entre **profections annuelles/mensuelles**, **retour solaire**, **chronocrateurs longs** (firdaria, décennials, releasing) et **directions/distributions** ? Cite les lignes ou sections Markdown.
61. Quelle est la différence entre ce que montre le **chronocrateur principal** de l’année et ce que montre le **mois actif** ? Appuie-toi sur `annual_profections.md`, `monthly_profections.md` et `life_arc_synthesis.md`.
62. À partir du retour solaire et de `solar_return_natal_comparison.md`, quelles planètes ou maisons du thème natal sont réactivées cette année ? Distingue superposition de maison, signe profecté, Lord of the Year et conjonctions serrées.
63. À partir de `monthly_transit_checkpoints.md`, quels contacts de transit sont seulement des **checkpoints mensuels** et lesquels mériteraient une recherche exacte courte autour de la période active ? Rappelle que ce n’est pas un scan quotidien complet.
64. À partir de `primary_directions.md` et `distributions_through_bounds.md`, y a-t-il