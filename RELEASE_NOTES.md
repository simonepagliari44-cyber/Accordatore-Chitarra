<div align="center">

# 🚀 **Release Notes — Accordatore Chitarra** 🎸

### *Versione 1.0 — Prima Ufficiale*

---

</div>

## 🏷️ **Versione 1.0.0** — *17 Ottobre 2026*

> 🎉 **Rilascio iniziale** dell'applicazione Android per l'accordatura e il riconoscimento delle note della chitarra.

---

## ✨ **Cosa c'è di Nuovo**

### 🎯 **Accordatore**
* 🔹 Sonda a indicatore semicircolare con lettura dei **cents in tempo reale** (±50 cents).
* 🔹 Zona verde di **intonazione corretta** (±5 cents) con messaggio **IN ACCORDO**.
* 🔹 Avvisi visivi **TIRA** (troppo basso) e **MOLLA** (troppo alto) con codici colore arancione/rosso.
* 🔹 Selezione corda **automatica** oppure **manuale** tramite chip numerati 1–6.
* 🔹 Doppia notazione: nome della nota in **italiano** (Do, Re, Mi…) e **internazionale** (C, D, E…).

### 🎵 **Riconoscitore di Note**
* 🔹 Modalità separata per identificare **qualsiasi nota** suonata vicino al microfono.
* 🔹 Visualizzazione di nome della nota, ottava, **frequenza in Hz** e scostamento in cents.
* 🔹 **Barra del livello del segnale** per guidare l'utente nella posizione corretta del microfono.

### 🧠 **Motore Audio**
* 🔹 Rilevamento della fondamentale con **algoritmo YIN custom** (nessuna dipendenza esterna).
* 🔹 **Interpolazione parabolica** per una precisione sub-cents.
* 🔹 **Mediana mobile** su 5 frame per eliminare i salti e lo sfarfallio dell'indicatore.
* 🔹 **Noise gate** adattivo e soglia di probabilità per ignorare rumori di fondo.
* 🔹 Cattura audio a **44.100 Hz** su thread dedicato con priorità urgente del sistema.

### 🎨 **Interfaccia Utente**
* 🔹 Design **Material 3** con supporto ai **colori dinamici** (Material You, Android 12+).
* 🔹 **Dark mode** automatica in base alle impostazioni di sistema.
* 🔹 Layout **edge-to-edge** sfruttando l'intero schermo.
* 🔹 Grafica dell'accordatore disegnata interamente con **Canvas** di Jetpack Compose.

### 🔒 **Sistema**
* 🔹 **Gate dei permessi** dedicato: schermata esplicita per autorizzare il microfono.
* 🔹 **Avvio/stop automatico** del motore audio legato al ciclo di vita dell'app (onResume/onPause).
* 🔹 Compatibilità con **Android 8.0 (API 26)** e successivi.
* 🔹 Build **release** con ofuscazione e minificazione (R8/ProGuard) attive.

---

## 📦 **Dettagli Tecnici**

| | |
| --- | --- |
| 📱 **Versione Name** | `1.0` |
| 🔢 **Versione Code** | `1` |
| 🎯 **SDK Minimo** | Android 8.0 (API 26) |
| 🎯 **SDK Target** | Android 15 (API 35) |
| 🧩 **Linguaggio** | Kotlin 2.1.0 |
| 🎨 **UI** | Jetpack Compose (BOM 2024.12.01) |
| 🏗️ **Build System** | Gradle + AGP 8.7.3 |

---

<div align="center">

Made with ❤️ and 🎸 by Simone

</div>
