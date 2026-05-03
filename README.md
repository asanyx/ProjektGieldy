# StockWatch 📈

Aplikacja mobilna do śledzenia kryptowalut w czasie rzeczywistym.
Zbudowana w Kotlin + Jetpack Compose z architekturą MVVM.

---

## Technologie

- Kotlin + Jetpack Compose
- MVVM + Hilt (Dependency Injection)
- Room (lokalna baza danych)
- Retrofit + CoinGecko API (dane rynkowe, bez klucza)
- Firebase Authentication (logowanie e-mail/hasło)
- Google Maps (maps-compose)
- DataStore Preferences (ustawienia)
- Accompanist Permissions (uprawnienia runtime)

---

## Funkcje aplikacji

| Ekran | Funkcja |
|---|---|
| Logowanie / Rejestracja | Firebase Authentication (e-mail + hasło) |
| Home | Lista kryptowalut z CoinGecko, wyszukiwarka, auto-odświeżanie co 2 min |
| Szczegóły monety | Cena, zmiana 24h, wykres 7-dniowy ze skalą, opis |
| Watchlist | Obserwowane monety z Room, notatki (long press → edycja) |
| Ustawienia | Tryb ciemny, zmiana waluty (USD/EUR/PLN/GBP) |
| Kontakt | Mapa GPW Warszawa, przyciski WWW / telefon / nawigacja |
| Navigation Drawer | Nawigacja boczna + wylogowanie |

---

## Instrukcja uruchomienia

### Wymagania
- Android Studio Hedgehog lub nowszy
- JDK 17
- Android SDK 26+
- Konto Google (Firebase + Google Cloud)

### Kroki

1. Sklonuj repozytorium:
```bash
git clone https://github.com/asanyx/ProjektGieldy.git
cd ProjektGieldy
```

2. Dodaj plik `google-services.json` do katalogu `app/`:
   - Pobierz go z Firebase Console (instrukcja poniżej)
   - Umieść w: `app/google-services.json`

3. Dodaj klucz Google Maps do `local.properties`:
   MAPS_API_KEY=twój_klucz_api

4. Otwórz projekt w Android Studio i poczekaj na synchronizację Gradle

5. Uruchom aplikację: **Run → Run 'app'** (Shift+F10)

---

## Konfiguracja Firebase

### 1. Utwórz projekt Firebase

1. Wejdź na [console.firebase.google.com](https://console.firebase.google.com)
2. Kliknij **Dodaj projekt** i wpisz nazwę np. `StockWatch`

### 2. Dodaj aplikację Android

1. W panelu projektu kliknij ikonę **Android**
2. Wpisz nazwę pakietu: `com.example.stockwatch`
3. Kliknij **Zarejestruj aplikację**
4. Pobierz plik `google-services.json` i umieść w folderze `app/`

### 3. Włącz Authentication

1. W lewym menu wybierz **Authentication → Rozpocznij**
2. Przejdź do zakładki **Metody logowania**
3. Kliknij **E-mail/hasło** → włącz → **Zapisz**

---

## Konfiguracja Google Maps

1. Wejdź na [console.cloud.google.com](https://console.cloud.google.com)
2. Wybierz **ten sam projekt co Firebase**
3. Przejdź do **APIs & Services → Library**
4. Wyszukaj **Maps SDK for Android** → **Enable**
5. Przejdź do **APIs & Services → Credentials → Create Credentials → API Key**
6. Skopiuj klucz i wklej do `local.properties`:
   MAPS_API_KEY=tutaj_wklej_klucz

---

## Generowanie APK

```bash
./gradlew assembleDebug
```

Plik APK znajdziesz w: `app/build/outputs/apk/debug/app-debug.apk`

Lub przez Android Studio: **Build → Build Bundle(s) / APK(s) → Build APK(s)**

---

## APK

Plik `app-debug.apk` dostępny jest w zakładce
[Releases](https://github.com/asanyx/ProjektGieldy/releases).

---

## Użyte API

- [CoinGecko API](https://api.coingecko.com/api/v3/) — darmowe, bez klucza API
- Firebase Authentication — plan Spark (bezpłatny)
- Google Maps Android SDK