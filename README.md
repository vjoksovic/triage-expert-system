# Sistem za podršku trijažnom odlučivanju u urgentnoj medicini
### Sistemi bazirani na znanju — Predlog projekta

**Autor:** Veljko Joksović `SV56/2022`

---

## 1. Motivacija

Hitne medicinske službe svakodnevno se suočavaju sa izazovom brze i tačne trijaže pacijenata, procesa kojim se određuje prioritet lečenja na osnovu hitnosti stanja. Greške u trijaži mogu direktno ugroziti živote: prekasno prepoznat infarkt ili potcenjena sepsa često završavaju fatalno.

Postojeći sistemi (Manchester Triage System, ESI skala) nude strukturirane protokole, ali su u potpunosti zavisni od iskustva medicinskog osoblja i podložni ljudskim greškama usled umora, stresa ili nedovoljnog iskustva. Cilj ovog projekta je razvoj ekspertnog sistema koji formalizuje medicinsko znanje i pruža automatizovanu podršku odlučivanju u realnom vremenu.

---

## 2. Pregled problema

Trijažni proces se tradicionalno oslanja na subjektivnu procenu simptoma na osnovu vitalnih znakova. Prema istraživanjima, pogrešan trijažni prioritet dodeljuje se u 10–30% slučajeva u standardnim hitnim službama.

**Nedostaci postojećih rešenja:**
- Nekonzistentnost: isti pacijent može dobiti različit prioritet od različitih medicinara
- Spora adaptacija: protokoli se sporo ažuriraju kada se pojave novi paterni bolesti
- Nedostatak formalne reprezentacije znanja: iskustvo svakog lekara je jedinstveno
- Odsustvo automatskog zaključivanja na osnovu kombinacije više faktora istovremeno

**Predloženo rešenje se razlikuje po tome što:**
- Formalizuje medicinsko znanje u transparentna, auditabilna pravila
- Primenjuje forward chaining sa više nivoa za automatsko izvođenje dijagnoza
- Koristi CEP (Complex Event Processing) za praćenje promena vitalnih znakova u realnom vremenu
- Podržava backward chaining za odgovaranje na konkretna dijagnostička pitanja
- Koristi Drools templejte za parametrizovana pravila po kategorijama pacijenata

Sistem će se primarno fokusirati na trijažne protokole za infektivna stanja (sepsa) i respiratorne smetnje, što omogućava duboku implementaciju pravila bez nepotrebnog širenja baze znanja na manje hitne medicinske oblasti.

---

## 3. Metodologija rada

### 3.1 Ulazi u sistem (Input)

| Parametar | Opis |
|---|---|
| Godine starosti | Utiče na pragove rizika |
| Temperatura (°C) | Vitalni znak — febrilnost |
| Sistolni pritisak (mmHg) | Vitalni znak — kardiovaskularni status |
| Dijastolni pritisak (mmHg) | Vitalni znak — kardiovaskularni status |
| Puls (otkucaji/min) | Vitalni znak — srčana funkcija |
| SpO2 (%) | Saturacija kiseonikom |
| Lista simptoma | Npr. bol u grudima, dispneja, konfuzija... |
| Hronične dijagnoze | Dijabetes i HOBP (Hronična opstruktivna bolest pluća) |
| Vreme dolaska | Za CEP praćenje vremenskih obrazaca |

### 3.2 Izlazi iz sistema (Output)

| Izlaz | Vrednosti | Opis |
|---|---|---|
| Trijažni prioritet | P1 / P2 / P3 / P4 / P5 | P1=Pacijent je ugrožen, P2=Stanje se brzo može pogoršati , P3=Može čekati 1 sat, P4=Hronični stabilni pacijent ili ne može ugroziti život, P5=Nije hitan |
| Preporučeno odeljenje | JIL, Pulmologija, Infektologija, Opšta ambulanta | Na osnovu primarnih simptoma i dijagnoze |
| Upozorenja | Lista tekstova | Npr. `Rizik od sepse — odmah uzeti hemokulture` |
| Objašnjenje odluke | Lista aktiviranih pravila | Audit trail zaključivanja |
| CEP alarmi | Real-time eventi | Npr. pad SpO2 za >5% za 10 minuta |

### 3.3 Baza znanja

Baza znanja sistema sastoji se od tri sloja:

1. **Sloj vitalnih znakova** — pragovi normalnih vrednosti po starosnim grupama, definisani kao Drools templejti
2. **Sloj dijagnostičkih pravila** — forward chaining pravila koja iz simptoma i vitalnih znakova izvode dijagnostičke zaključke
3. **Sloj trijažnih pravila** — pravila koja kombinuju dijagnostičke zaključke i dodeljuju prioritet i odeljenje

Sistem je fokusiran isključivo na infektivne (sepsa) i respiratorne baze znanja. Sloj vitalnih znakova koristi Drools templejte za definisanje granica normalnih vrednosti (puls, pritisak, temperatura) koje se automatski menjaju na osnovu uzrasta pacijenta (npr. različiti pragovi za decu, odrasle i starije osobe).

---

## 3.4 Pravila sistema

### Ručno uneseno u Working Memory (Input od lekara)

```
Patient(age, chronicConditions: [DIABETES, COPD])
Vitals(pulse, systolicBP, spo2, temperature, timestamp)
Symptom(DISPNEA)    ← lekar čekira
Symptom(CONFUSION)  ← lekar čekira
```

---

### Nivo 1 — Template pravila (Single)

Ulaz: `Vitals` + `Patient(category)` | Izlaz: `Symptom`

| Pravilo | Uslov | Ubacuje u WM |
|---|---|---|
| Tachycardia | pulse > prag za uzrast | Symptom(TACHYCARDIA) |
| Hypotension | systolicBP < prag za uzrast | Symptom(HYPOTENSION) |
| Fever | temperature > prag za uzrast | Symptom(FEVER) |
| Hypoxemia | spo2 < prag za uzrast | Symptom(HYPOXEMIA) |

Pragovi po kategoriji:

| Simptom | CHILD (<18) | ADULT (18–65) | SENIOR (>65) |
|---|---|---|---|
| Tachycardia | pulse > 100 | pulse > 100 | pulse > 90 |
| Hypotension | systolic < 90 | systolic < 100 | systolic < 110 |
| Fever | temp > 37.5 | temp > 38.0 | temp > 37.8 |
| Hypoxemia | spo2 < 95 | spo2 < 94 | spo2 < 92 |

---

### Nivo 2 — Dijagnostička pravila (Double Join)

Ulaz: `Symptom` + `Symptom` | Izlaz: `Diagnosis`

| Pravilo | Uslov (oba moraju biti u WM) | Ubacuje u WM |
|---|---|---|
| Respiratory Failure | Symptom(HYPOXEMIA) + Symptom(DISPNEA) | Diagnosis(RESPIRATORY_FAILURE) |
| Sepsis Preliminary | Symptom(FEVER) + Symptom(TACHYCARDIA) | Diagnosis(SEPSIS_SUSPECTED) |

Ostale kombinacije simptoma nisu dijagnostički zaključci na ovom nivou jer su ili posledice već pokrivenih dijagnoza, ili su van skopa sistema (fokus: sepsa i respiratorno).

---

### Nivo 3 — Trijažna pravila (Triple Join)

Ulaz: `Diagnosis` + `Symptom` + `Patient(category | chronicConditions)` | Izlaz: `Triage`

Svako pravilo sadrži tačno tri činioca. Kod pacijenata bez komorbiditeta, treći činilac je starosna kategorija.

| Pravilo | Činilac 1 | Činilac 2 | Činilac 3 | Prioritet | Odeljenje |
|---|---|---|---|---|---|
| Sepsa + Dijabetes | Diagnosis(SEPSIS_SUSPECTED) | Symptom(HYPOTENSION) | Patient(DIABETES) | P1 | JIL |
| Sepsa + COPD | Diagnosis(SEPSIS_SUSPECTED) | Symptom(HYPOTENSION) | Patient(COPD) | P1 | JIL |
| Resp. insuf. + Dijabetes | Diagnosis(RESPIRATORY_FAILURE) | Symptom(TACHYCARDIA) | Patient(DIABETES) | P1 | Pulmologija |
| Resp. insuf. + COPD | Diagnosis(RESPIRATORY_FAILURE) | Symptom(TACHYCARDIA) | Patient(COPD) | P1 | Pulmologija |
| Sepsa + SENIOR | Diagnosis(SEPSIS_SUSPECTED) | Symptom(HYPOTENSION) | Patient(SENIOR) | P1 | Infektologija |
| Sepsa + ADULT | Diagnosis(SEPSIS_SUSPECTED) | Symptom(HYPOTENSION) | Patient(ADULT) | P1 | Infektologija |
| Sepsa + CHILD | Diagnosis(SEPSIS_SUSPECTED) | Symptom(HYPOTENSION) | Patient(CHILD) | P1 | Infektologija |
| Resp. insuf. + SENIOR | Diagnosis(RESPIRATORY_FAILURE) | Symptom(TACHYCARDIA) | Patient(SENIOR) | P2 | Pulmologija |
| Resp. insuf. + ADULT | Diagnosis(RESPIRATORY_FAILURE) | Symptom(TACHYCARDIA) | Patient(ADULT) | P2 | Pulmologija |
| Resp. insuf. + CHILD | Diagnosis(RESPIRATORY_FAILURE) | Symptom(TACHYCARDIA) | Patient(CHILD) | P2 | Pulmologija |
| Bez dijagnoze | not Diagnosis() | Symptom(bilo koji) | Patient(bilo koji) | P3 | Opšta ambulanta |

---

### CEP — Real-time monitoring

Ulaz: stream `Vitals` kroz vreme | Izlaz: alarm

| Pravilo | Uslov | Rezultat |
|---|---|---|
| SpO2 Rapid Drop | spo2 pao za > 5% u poslednjih 10 minuta | ALARM → P1, upozorenje za disajni put |

---

### Accumulate — Agregacija na nivou odeljenja

Ulaz: sve `Triage` u WM | Izlaz: akcija na nivou odeljenja

| Pravilo | Uslov | Rezultat |
|---|---|---|
| Overload odeljenja | count(Triage(priority == P1)) > 5 | novi P1 pacijenti → preusmeravanje u sekundarnu ustanovu |

---

### Backward Chaining — Stablo zaključivanja

Lekar postavlja direktno pitanje sistemu: *"Da li ovaj pacijent ima sepsu?"*

Sistem ne prolazi kroz sva pravila već ide **unazad** kroz hijerarhiju zavisnosti i proverava samo ono što mu je potrebno da potvrdi ili opovrgne cilj.

```
isSepsaSuspected($p)?
│
├── hasInfectionRisk($p)
│   ├── hasFever($p)
│   │   └── Vitals(temperature > prag za uzrast)
│   └── hasConfusion($p)
│       └── Symptom(type: CONFUSION)  ← ručno uneseno
│
└── hasHemodynamicInstability($p)
    ├── hasTachycardia($p)
    │   └── Vitals(pulse > prag za uzrast)
    └── hasHypotension($p)
        └── Vitals(systolicBP < prag za uzrast)
```

Pravila stabla:

| Query / Subquery | Proverava | Zaključuje |
|---|---|---|
| `isSepsaSuspected` | `hasInfectionRisk` AND `hasHemodynamicInstability` | Sepsa suspektna |
| `hasInfectionRisk` | `hasFever` OR `hasConfusion` | Postoji rizik od infekcije |
| `hasHemodynamicInstability` | `hasTachycardia` AND `hasHypotension` | Hemodinamska nestabilnost |
| `hasFever` | Vitals(temperature > prag) | Povišena temperatura |
| `hasConfusion` | Symptom(CONFUSION) | Prisutna konfuzija |
| `hasTachycardia` | Vitals(pulse > prag) | Ubrzan puls |
| `hasHypotension` | Vitals(systolicBP < prag) | Nizak pritisak |

---

## 4. Konkretan primer rezonovanja

**Scenario — Pacijent: Petar, 68 god.**

| Parametar | Vrednost |
|---|---|
| Starost | 68 godina |
| Temperatura | 38.9°C |
| Krvni pritisak | 95/60 mmHg |
| Puls | 118 otkucaja/min |
| SpO2 | 91% |
| Simptomi | Konfuzija, otežano disanje, ubrzano disanje |
| Hronične dijagnoze | Dijabetes tip 2 |

**Tok rezonovanja (korak po korak):**

**1. Unos podataka**
U Working Memory se ubacuje:
```
Patient(age: 68)
Vitals(pulse: 118, bp: 95/60, spo2: 91)
```

**2. Klasifikacija: Osnovni fakti** *(Single — Template pravila)*
Aktivira se pravilo iz templejta; pošto je puls > 100 za uzrast `Adult`, sistem u WM ubacuje novi fakt. Isto se dešava za hipotenziju i hipoksemiju:
```
Symptom(type: TACHYCARDIA)
Symptom(type: HYPOTENSION)
Symptom(type: HYPOXEMIA)
```

**3. Double Join: Kombinacija simptoma**
Pravilo pronalazi u WM istovremeno fakte `HYPOXEMIA` i `Symptom(type: DISPNEA)`. Na osnovu njihove koegzistencije ubacuje se novi fakt:
```
Diagnosis(type: RESPIRATORY_FAILURE)
```

**4. Triple Join: Finalna odluka**

*Sepsa:* Pravilo detektuje istovremeno u WM: `Symptom(TACHYCARDIA)`, `Symptom(HYPOTENSION)` i `Condition(DIABETES)`. Sistem zaključuje da postoji visok rizik od sepse:
```
INSERT: Diagnosis(type: SEPSIS_SUSPECTED)
SET:    Triage(priority: P1, ward: JIL)
WARN:   "Rizik od sepse — hitno uzeti hemokulture i primeniti antibiotike"
```

*Respiratorna insuficijencija:* Pravilo detektuje istovremeno `Diagnosis(RESPIRATORY_FAILURE)`, `Symptom(TACHYCARDIA)` i `Condition(DIABETES)`:
```
SET:  Triage(priority: P1, ward: Pulmologija)
WARN: "Respiratorna insuficijencija kod dijabetičara — razmotriti mehaničku ventilaciju"
```

**5. CEP: Praćenje trenda SpO2**
Prati se isključivo trend pada saturacije kiseonikom. Alarm se aktivira ako vrednost padne za više od 5% u periodu od 10 minuta, što ukazuje na naglo pogoršanje koje statična pravila ne bi odmah uočila.
```
SpO2Event(value < prev - 5) over window:time(10m)
→ ALARM: "Nagli pad saturacije — proveriti disajni put"
```

**6. Backward Chaining: Dijagnostičko pitanje**
Backward chaining je podržan kroz upit `isSepsaSuspected`. Upit omogućava lekaru da postavi direktno pitanje: *"Da li trenutni profil pacijenta sugeriše početak sepse?"* — sistem unazad proverava da li su prisutni specifični markeri (hipotenzija, povišeni laktati, konfuzija) i da li postoji verovatnoća infekcije.

**7. Accumulate: Broj kritičnih pacijenata**
Sistem koristi `accumulate` funkciju za nadzor opterećenja odeljenja. Pravilo u realnom vremenu broji pacijente u WM sa statusom `priority == P1`. Ukoliko `count` vrati vrednost veću od praga (npr. 5 pacijenata u kritičnom stanju), sistem preusmerava novopristigle P1 pacijente u najbližu sekundarnu ustanovu kako bi se sačuvali resursi za najteže slučajeve.

---

## 5. Tehnička arhitektura

| Komponenta | Tehnologija | Uloga |
|---|---|---|
| Rule Engine | Drools | Forward/backward chaining, CEP, templejti |
| Backend | Spring Boot | REST API, integracija sa Drools `KieSession` |
| Frontend | Angular | Prikaz rezultata i unos merenja |
| Baza podataka | PostgreSQL | Čuvanje istorije pacijenata i aktiviranih pravila |
| CEP | Drools Fusion | Real-time monitoring vitalnih znakova |
