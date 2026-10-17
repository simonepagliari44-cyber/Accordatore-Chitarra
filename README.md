<div align="center">

# 🎸 **Accordatore e Riconoscitore Note per Chitarra** 🎶

### *App Android per l'Accordatura Intelligente e il Riconoscimento delle Note*

---

</div>

## 📌 **Panoramica del Progetto**

Il progetto **Accordatore e Riconoscitore Note per Chitarra** nasce con l'obiettivo di offrire uno strumento preciso, reattivo ed essenziale per i chitarristi: riconoscere in tempo reale la nota suonata attraverso il microfono del dispositivo e guidare l'utente verso l'accordatura perfetta.

L'architettura è suddivisa in un **motore di elaborazione audio** indipendente dall'interfaccia e in una **UI moderna in Jetpack Compose**, garantendo prestazioni elevate anche su dispositivi di fascia bassa.

---

## ✨ **Caratteristiche Principali**

* 🎯 **Accordatore Preciso:** Sonda a indicatore con tolleranza di **±5 cents**, con indicazioni visive chiare: **IN ACCORDO**, **TIRA** (troppo basso) o **MOLLA** (troppo alto).
* 🎵 **Riconoscitore di Note:** Modalità separata che identifica qualsiasi nota suonata vicino al microfono, mostrando nome in notazione **italiana e internazionale**, ottava e frequenza in Hz.
* 🧠 **Algoritmo YIN Personalizzato:** Rilevamento della fondamentale con interpolazione parabolica e **mediana mobile** su 5 campioni per una lettura stabile e senza flickering.
* 🎸 **Selezione Corda Automatica o Manuale:** L'app riconosce automaticamente la corda più vicina, oppure permette di fissarla manualmente con un tocco sui chip numerati.
* 📊 **Indicatori in Tempo Reale:** Lancetta angolare dei cents, barra del livello del segnale e calcolo dell'intonazione in base alla nota più prossima.
* 🎨 **Interfaccia Material You:** Tema Material 3 con **colori dinamici** (Android 12+), supporto al dark mode e layout edge-to-edge.
* ⚡ **Audio a Bassa Latenza:** Cattura su thread dedicato con priorità urgente, campionamento a **44.100 Hz** e frame da 4096 campioni.
* 🔒 **Gestione Permessi Esplicita:** Schermata dedicata per la richiesta del microfono, con avvio/stop automatico del motore secondo il ciclo di vita dell'activity.

---

## 🛠️ **Stack Tecnologico**

| Componente | Tecnologia |
| --- | --- |
| 🧩 Linguaggio | **Kotlin 2.1** |
| 🎨 UI | **Jetpack Compose** + Material 3 |
| 🔊 Audio | **Android AudioRecord** (PCM 16-bit, mono) |
| 🧮 Rilevamento pitch | Algoritmo **YIN** custom (nessuna libreria esterna) |
| 📱 Piattaforma | **Android 8.0+** (API 26) — target API 35 |
| 🏗️ Build | **Gradle 8.x** + Android Gradle Plugin 8.7 |

---

## 📂 **Struttura del Progetto**

```
app/src/main/java/com/example/guitartuner/
├── MainActivity.kt          # Entry point + gate dei permessi
├── tuner/
│   ├── TunerEngine.kt       # Motore audio + stabilizzazione del segnale
│   ├── PitchDetector.kt     # Rilevamento pitch (algoritmo YIN)
│   └── Notes.kt             # Note, frequenze, cents, corde della chitarra
└── ui/
    ├── TunerApp.kt          # Schermate Accordatore e Riconoscitore
    └── Theme.kt             # Tema Material 3 con colori dinamici
```

---

## 🚀 **Come Compilare**

1. 📥 Clona il repository:
   ```bash
   git clone <url-del-repository>
   ```
2. 📂 Apri la cartella del progetto in **Android Studio**.
3. ▶️ Attendi la sincronizzazione Gradle, quindi premi **Run** ▶️ su un dispositivo o emulatore con microfono.

> ⚠️ **Nota:** l'app richiede il permesso di registrazione audio per funzionare.

---
