# Sistem za podršku trijažnom odlučivanju u urgentnoj medicini

**Sistemi bazirani na znanju — Specifikacija projekta**

**Autor:** Veljko Joksović `SV56/2022`

---

## 1. Motivacija

Hitne medicinske službe svakodnevno se suočavaju sa izazovom brze i tačne trijaže pacijenata, procesa kojim se određuje prioritet lečenja na osnovu hitnosti stanja. Greške u trijaži mogu direktno ugroziti živote: prekasno prepoznata sepsa ili potcenjena respiratorna insuficijencija često završavaju fatalno.

Postojeći sistemi (Manchester Triage System, ESI skala) nude strukturirane protokole, ali su u potpunosti zavisni od iskustva medicinskog osoblja i podložni ljudskim greškama usled umora, stresa ili nedovoljnog iskustva. Cilj ovog projekta je razvoj ekspertnog sistema koji formalizuje medicinsko znanje i pruža automatizovanu podršku odlučivanju u realnom vremenu.

---

## 2. Pregled problema

Trijažni proces se tradicionalno oslanja na subjektivnu procenu simptoma na osnovu vitalnih znakova. Prema istraživanjima, pogrešan trijažni prioritet dodeljuje se u 10–30% slučajeva u standardnim hitnim službama.

**Nedostaci postojećih rešenja:**

- Nekonzistentnost: isti pacijent može dobiti različit prioritet od različitih medicinara
- Spora adaptacija: protokoli se sporo ažuriraju kada se pojave novi paterni bolesti
- Nedostatak formalne reprezentacije znanja: iskustvo svakog lekara je jedinstveno
- Odsustvo automatskog zaključivanja na osnovu kombinacije više faktora istovremeno

**Predloženo rešenje:**

- Formalizuje medicinsko znanje u transparentna, auditabilna pravila (Drools DRL)
- Primenjuje forward chaining sa više nivoa za automatsko izvođenje dijagnoza
- Koristi CEP (Complex Event Processing) za praćenje promena vitalnih znakova kroz vreme
- Podržava backward chaining za odgovaranje na konkretna dijagnostička pitanja
- Parametrizuje pragove vitalnih znakova po starosnim grupama (Java klasa `VitalSignThresholds`)
- Primenjuje accumulate pravila za nadzor opterećenja odeljenja i preusmeravanje pacijenata

Sistem je fokusiran na trijažne protokole za **infektivna stanja (sepsa)** i **respiratorne smetnje**.

---

## 3. Metodologija rada

### 3.1 Ulazi u sistem (Input)

| Parametar | Opis |
|---|---|
| Ime i prezime | Identifikacija pacijenta |
| Godine starosti | Utiče na pragove rizika i starosnu grupu |
| Meseci starosti | Preciznija klasifikacija novorođenčadi |
| Preterm | Oznaka nedonočeta |
| Temperatura (°C) | Vitalni znak — febrilnost |
| Sistolni pritisak (mmHg) | Vitalni znak — kardiovaskularni status |
| Dijastolni pritisak (mmHg) | Vitalni znak — kardiovaskularni status |
| Puls (otkucaji/min) | Vitalni znak — srčana funkcija |
| SpO₂ (%) | Saturacija kiseonikom |
| Lista simptoma | Ručno uneti: dispneja, konfuzija |
| Hronične dijagnoze | Dijabetes, HOBP (COPD) |
| ID slučaja | Jedinstveni identifikator pacijenta u sesiji (za praćenje opterećenja odeljenja) |
| Vreme merenja | Za CEP praćenje vremenskih obrazaca |

Simptomi izvedeni iz vitalnih znakova (groznica, tahikardija, hipotenzija, hipoksemija) ne unosi lekar — sistem ih automatski detektuje pravilima nivoa 1.

### 3.2 Izlazi iz sistema (Output)

| Izlaz | Vrednosti | Opis |
|---|---|---|
| Trijažni prioritet | P1 / P2 / P3 / P4 / P5 | P1 — resuscitacija; P2 — hitno; P3 — urgentno; P4 — manje urgentno; P5 — nehitno |
| Preporučeno odeljenje | JIL, Pulmologija, Infektologija, Opšta ambulanta | Na osnovu dijagnoze, simptoma i komorbiditeta |
| Upozorenja | Lista tekstova | Klinički saveti (npr. hemokulture, antibiotici) |
| Objašnjenje odluke | Lista aktiviranih pravila | Audit trail zaključivanja |
| CEP alarmi | Real-time eventi | P1 alarmi na osnovu trenda SpO₂ i pulsa |
| Backward chaining odgovor | Da/Ne + stablo dokaza | Odgovor na pitanje o sumnji na sepsu |
| Opterećenje odeljenja | Broj P1 pacijenata, preusmeravanje | Status odeljenja i informacija o sekundarnoj ustanovi |

### 3.3 Baza znanja

Baza znanja se sastoji od pet slojeva:

1. **Sloj klasifikacije uzrasta** — dodela starosne grupe pacijentu pre detekcije vitalnih znakova
2. **Sloj vitalnih znakova** — pragovi po starosnim grupama (`VitalSignThresholds` + DRL pravila)
3. **Sloj dijagnostičkih pravila** — forward chaining: simptom + simptom → dijagnoza
4. **Sloj trijažnih pravila** — forward chaining: dijagnoza + simptom + pacijent → prioritet i odeljenje
5. **Sloj agregacije (accumulate)** — brojanje aktivnih P1 slučajeva i preusmeravanje pri preopterećenju

Dodatni slojevi:

- **CEP sloj** — obrada strima vitalnih događaja u vremenskom prozoru
- **Backward chaining sloj** — rekurzivni upit `prove(goal)` za dijagnostička pitanja o sepsi

---

## 4. Starosne grupe i pragovi vitalnih znakova

### 4.1 Starosne grupe

Sistem koristi sedam pedijatrijskih grupa po uzrastu plus adolescent/adult:

| Grupa | Uslov klasifikacije |
|---|---|
| PRETERM | `preterm == true` |
| NEWBORN | `age == 0`, `ageInMonths < 1` |
| INFANT | `age == 0`, inače (≥ 1 mesec) |
| TODDLER | `age` 1–2 godine |
| PRESCHOOL | `age` 3–5 godina |
| SCHOOL_AGE | `age` 6–12 godina |
| ADOLESCENT | `age ≥ 13` godina |

Klasifikacija se izvršava pravilima nivoa 0 (salijence 1000), pre detekcije vitalnih znakova.

### 4.2 Pragovi vitalnih znakova

| Simptom | PRETERM | NEWBORN | INFANT | TODDLER | PRESCHOOL | SCHOOL_AGE / ADOLESCENT |
|---|---|---|---|---|---|---|
| **Tahikardija** (puls &gt;) | 180 | 160 | 140 | 130 | 110 | 100 |
| **Groznica** (temp &gt; °C) | 37.5* | 37.5* | 37.5* | 37.5* | 37.5* | 38.0 |
| **Hipotenzija** (SBP &lt;) | 90* | 90* | 90* | 90* | 90* | 100 |
| **Hipoksemija** (SpO₂ &lt;) | 95* | 95* | 95* | 95* | 95* | 94 |

\*Pedijatrijski prag = sve grupe osim ADOLESCENT.

Pragovi su centralizovani u klasi `VitalSignThresholds`. Tahikardija se detektuje per-grupa pravilima; groznica, hipotenzija i hipoksemija kroz jedinstvena pravila.

---

## 5. Pravila sistema

### 5.1 Ručno uneseno u Working Memory

```
Patient(fullName, age, ageInMonths?, preterm?, chronicConditions: [DIABETES, COPD])
Vitals(temperature, systolicBP, diastolicBP, pulse, spo2, measuredAt)
Symptom(DISPNEA)    ← lekar bira
Symptom(CONFUSION)  ← lekar bira
```

### 5.2 Nivo 0 — Klasifikacija uzrasta

| Pravilo | Uslov | Efekat |
|---|---|---|
| Classify preterm patient | `preterm == true` | `ageGroup = PRETERM` |
| Classify newborn patient | `age == 0`, `ageInMonths < 1` | `ageGroup = NEWBORN` |
| Classify infant patient | `age == 0`, inače | `ageGroup = INFANT` |
| Classify toddler patient | `age` 1–2 | `ageGroup = TODDLER` |
| Classify preschool patient | `age` 3–5 | `ageGroup = PRESCHOOL` |
| Classify school-age patient | `age` 6–12 | `ageGroup = SCHOOL_AGE` |
| Classify adolescent patient | `age ≥ 13` | `ageGroup = ADOLESCENT` |

### 5.3 Nivo 1 — Detekcija simptoma iz vitalnih znakova

Ulaz: `Vitals` + `Patient` | Izlaz: `Symptom`

| Pravilo | Uslov | Ubacuje u WM |
|---|---|---|
| Detect fever | temperatura iznad praga za uzrast | `Symptom(FEVER)` |
| Detect hypotension | sistolni pritisak ispod praga | `Symptom(HYPOTENSION)` |
| Detect hypoxemia | SpO₂ ispod praga | `Symptom(HYPOXEMIA)` |
| Detect tachycardia | puls iznad praga za grupu | `Symptom(TACHYCARDIA)` |

### 5.4 Nivo 2 — Dijagnostička pravila (Double Join)

Ulaz: `Symptom` + `Symptom` | Izlaz: `Diagnosis` | Salijence: 100

| Pravilo | Uslov (oba moraju biti u WM) | Ubacuje u WM |
|---|---|---|
| Diagnose sepsis preliminary | `FEVER` + `TACHYCARDIA` | `Diagnosis(SEPSIS_SUSPECTED)` |
| Diagnose respiratory failure | `HYPOXEMIA` + `DISPNEA` | `Diagnosis(RESPIRATORY_FAILURE)` |

### 5.5 Nivo 3 — Trijažna pravila (Triple Join)

Ulaz: `Diagnosis` + `Symptom` + `Patient(chronicConditions)` | Izlaz: `TriageResult`

| Pravilo | Činilac 1 | Činilac 2 | Činilac 3 | Salijence | Prioritet | Odeljenje |
|---|---|---|---|---|---|---|
| Triage sepsis with diabetes | `SEPSIS_SUSPECTED` | `HYPOTENSION` | `DIABETES` | 110 | P1 | JIL |
| Triage sepsis with COPD | `SEPSIS_SUSPECTED` | `HYPOTENSION` | `COPD` | 110 | P1 | JIL |
| Triage respiratory failure with diabetes | `RESPIRATORY_FAILURE` | `TACHYCARDIA` | `DIABETES` | 105 | P1 | Pulmologija |
| Triage respiratory failure with COPD | `RESPIRATORY_FAILURE` | `TACHYCARDIA` | `COPD` | 105 | P1 | Pulmologija |
| Triage sepsis without comorbidity | `SEPSIS_SUSPECTED` | `HYPOTENSION` | bez DIABETES/COPD | 90 | P1 | Infektologija |
| Triage respiratory failure without comorbidity | `RESPIRATORY_FAILURE` | `TACHYCARDIA` | bez DIABETES/COPD | 90 | P2 | Pulmologija |
| Triage without diagnosis | `not Diagnosis()` | — | bilo koji `Patient` | −100 | P3 | Opšta ambulanta |

Pri konfliktu više pravila, primenjuje se pravilo sa višim salijence-om (110 &gt; 105 &gt; 90).

### 5.6 CEP — Real-time monitoring

Ulaz: strim vitalnih događaja kroz vreme | Izlaz: alarm (P1)  
Prozor: 10 minuta, pseudo-sat za simulaciju trenda.

| Pravilo | Uslov | Rezultat |
|---|---|---|
| SpO2 Rapid Drop | SpO₂ pao &gt; 5% od maksimuma u 10 min i puls &gt; minimum + 8 (kompenzatorna tahikardija) | P1 alarm |
| Acute Respiratory Crash — Pulse Collapse | SpO₂ pad &gt; 5% i puls ≤ vrh − 10 | P1 alarm |
| Acute Respiratory Crash — Failed Compensation | SpO₂ pad &gt; 5% i puls ≤ minimum + 8 (bez kompenzacije) | P1 alarm |

Dva „acute crash“ pravila dele activation-group — aktivira se samo jedno po ciklusu.

Nakon svake trijaže, sistem čuva istoriju očitanja i evaluira trend, ne samo trenutni snimak.

### 5.7 Backward chaining — Stablo zaključivanja

Lekar postavlja pitanje sistemu: *„Da li trenutni profil pacijenta sugeriše sumnju na sepsu?“*

Sistem koristi rekurzivni Drools upit `prove(String goal)` bez pokretanja forward trijažnih pravila nivoa 2 i 3:

```
isSepsaSuspected
├── hasInfectionRisk          [OR]
│   ├── hasFever              → temperatura iznad praga za uzrast
│   └── hasConfusion          → Symptom(CONFUSION) ručno uneseno
└── hasHemodynamicInstability [AND]
    ├── hasTachycardia        → puls iznad praga za uzrast
    └── hasHypotension        → sistolni pritisak ispod praga
```

| Cilj (`goal`) | Logika | Zaključak |
|---|---|---|
| `isSepsaSuspected` | `hasInfectionRisk` AND `hasHemodynamicInstability` | Sepsa sumnjiva |
| `hasInfectionRisk` | `hasFever` OR `hasConfusion` | Postoji rizik od infekcije |
| `hasHemodynamicInstability` | `hasTachycardia` AND `hasHypotension` | Hemodinamska nestabilnost |

Agenda grupa `"backward"` vraća odgovor `SepsisSuspectedAnswer(true/false)`. Sistem gradi vizuelno stablo dokaza za svaki pod-cilj.

| Aspekt | Forward trijaža | Backward chaining |
|---|---|---|
| Kriterijum infekcije | `FEVER` + `TACHYCARDIA` → dijagnoza | `FEVER` ili `CONFUSION` |
| Hemodinamska nestabilnost | `HYPOTENSION` kao treći činilac trijaže | `TACHYCARDIA` i `HYPOTENSION` |
| Nivo 2/3 pravila | Pokreću se | Ne pokreću se |

### 5.8 Accumulate — Agregacija na nivou odeljenja

Prag: **5** već primljenih P1 pacijenata — šesti novi P1 se preusmerava u sekundarnu ustanovu.

Pre svake trijaže, u Working Memory se ubacuju fakti `DepartmentTriageCase` za sve aktivne P1 slučajeve u odeljenju. Novi pacijent se trijažira normalno; accumulate pravilo se izvršava posle nivoa 3 (salijence −50).

| Pravilo | Uslov | Rezultat |
|---|---|---|
| Redirect new P1 patient when department overloaded | Novi `TriageResult(P1)` + `accumulate(DepartmentTriageCase(P1), count) >= 5` | Preusmeravanje u Opštu ambulantu (sekundarna ustanova), upozorenje, čuvanje originalnog odeljenja |

**Tok:**

1. Klijent šalje `caseId` uz podatke pacijenta.
2. Sistem ubacuje postojeće P1 slučajeve u sesiju (isključujući trenutni `caseId` pri ponovnoj trijaži).
3. Nakon trijaže, P1 slučaj se registruje u registru opterećenja odeljenja.
4. Otpust pacijenta uklanja slučaj iz registra.

---

## 6. Konkretan primer rezonovanja

**Scenario — Pacijent: Elena Marković, 68 god.**

| Parametar | Vrednost |
|---|---|
| Starost | 68 godina → ADOLESCENT |
| Temperatura | 39.2 °C |
| Krvni pritisak | 88/54 mmHg |
| Puls | 128 otkucaja/min |
| SpO₂ | 91 % |
| Simptomi (ručno) | Konfuzija |
| Hronične dijagnoze | Dijabetes |

### 6.1 Forward trijaža

**1. Unos podataka**

```
Patient(age: 68, chronicConditions: [DIABETES])
Vitals(temperature: 39.2, systolicBP: 88, pulse: 128, spo2: 91)
Symptom(CONFUSION)
```

**2. Klasifikacija uzrasta** → `ADOLESCENT`

**3. Nivo 1 — detekcija simptoma**

```
Symptom(FEVER)        ← temp > 38.0
Symptom(TACHYCARDIA)  ← pulse > 100
Symptom(HYPOTENSION)  ← SBP < 100
Symptom(HYPOXEMIA)    ← SpO2 < 94
```

**4. Nivo 2 — dijagnoza**

```
Diagnosis(SEPSIS_SUSPECTED)  ← FEVER + TACHYCARDIA
```

**5. Nivo 3 — trijažna odluka**

```
TriageResult(priority: P1, ward: JIL)
WARNING: "Sepsis risk - obtain blood cultures immediately and start antibiotics."
```

### 6.2 CEP monitoring

Nakon više očitanja sa padajućim SpO₂ u 10-minutnom prozoru mogu se aktivirati alarmi SpO2 Rapid Drop ili Acute Respiratory Crash.

### 6.3 Backward chaining

Upit `prove("isSepsaSuspected")` vraća pozitivan odgovor: groznica zadovoljava rizik infekcije, tahikardija i hipotenzija zadovoljavaju hemodinamsku nestabilnost.

### 6.4 Accumulate — preopterećenje odeljenja

Kada je u odeljenju već 5 P1 pacijenata, šesti novi P1 pacijent se preusmerava u Opštu ambulantu (sekundarna ustanova), uz zadržavanje informacije o originalnom odeljenju (JIL).

---

## 7. Tehnička arhitektura

### 7.1 Pregled komponenti

| Komponenta | Tehnologija | Uloga |
|---|---|---|
| Rule Engine | Drools 7.49 | Forward/backward chaining, CEP, accumulate |
| Backend | Spring Boot 2.7 (Java 11) | REST API, integracija sa Drools KieSession |
| Frontend | Angular 19 | TriageOS — unos, trijaža, CEP, backward chaining, opterećenje odeljenja |
| CEP | Drools Fusion (pseudo-clock) | Monitoring trenda vitalnih znakova |

### 7.2 Struktura projekta

```
triage-system/
├── triage-back/
│   ├── model/     # Domen model
│   ├── kjar/      # DRL pravila
│   └── service/   # Spring Boot aplikacija (port 8090)
└── triage-front/  # Angular SPA (port 4200)
```

### 7.3 Kie sesije

| Sesija | Paketi | Režim |
|---|---|---|
| `triageKSession` | level1, level2, level3, accumulate, backward | Forward + backward + agregacija |
| `cepKsessionPseudoClock` | cep | Stream + pseudo sat |

### 7.4 REST API

Baza: `/api/triage`

| Metoda | Putanja | Opis |
|---|---|---|
| POST | `/evaluate` | Forward trijaža |
| POST | `/backward/sepsis` | Backward chaining — sumnja na sepsu + stablo dokaza |
| POST | `/cep/monitor` | CEP analiza strima očitanja |
| GET | `/department/load` | Opterećenje odeljenja (P1 brojač i lista) |
| DELETE | `/department/cases/{caseId}` | Otpust pacijenta iz registra |

### 7.5 Korisnički interfejs (TriageOS)

| Funkcionalnost | Opis |
|---|---|
| Višestruki tabovi pacijenata | Rad sa više profila istovremeno |
| Unos pacijenta i vitalnih znakova | Ime, godine, temperatura, BP, puls, SpO₂ |
| Klinički kontekst | Dijabetes, COPD, dispneja, konfuzija |
| Run triage | Trijaža sa prikazom prioriteta, odeljenja i kliničkog rezonovanja |
| CEP monitoring | Automatski nakon trijaže; alarm ili stabilan tok |
| SpO₂ / Pulse trend | Istorija očitanja po pacijentu |
| Backward chaining | Dokaz sumnje na sepsu — stablo `prove(goal)` |
| Department load | P1 brojač, lista primljenih, upozorenje pre preusmeravanja |
| Secondary redirect | Prikaz originalnog i preusmerenog odeljenja |

---

## 8. Testiranje

| Oblast | Pokrivenost |
|---|---|
| Backward chaining | Registracija upita `prove`, odgovor na sepsu upit |
| CEP | SpO2 Rapid Drop, Acute Respiratory Crash, negativni slučajevi |
| Accumulate | Preusmeravanje 6. P1 pacijenta kada je 5 već u odeljenju |
| Frontend | Inicijalizacija aplikacije i demo podaci |

---
