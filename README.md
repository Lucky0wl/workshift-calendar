# Календарь рабочих смен (Android) [![Android CI](https://github.com/USERNAME/REPO/actions/workflows/android-ci.yml/badge.svg)](https://github.com/USERNAME/REPO/actions)

Простое Android‑приложение на Jetpack Compose для планирования рабочих смен с готовыми шаблонами и несколькими визуальными стилями.

**Текущая версия:** 4.0.0

## Возможности

### 📅 Календарь
- Помесячный календарь с подсветкой смен и текущей даты
- Отображение периодов отпуска
- Копирование смен на другой месяц

### 🔄 Типы смен
- Утро, вечер, ночь и выходной — у каждого свой цвет
- Кастомизация цветов для каждого типа смены
- Заметки, местоположение, время начала/конца

### 📋 Шаблоны графиков
- Готовые схемы: 2/2, 3/3, 5/2, сутки через трое и др.
- Создание собственных шаблонов
- Применение шаблона на весь месяц

### 🎨 Стили оформления
- Современный синий (светлая/тёмная тема)
- Тёмный AMOLED
- Тёплый пастельный (светлая/тёмная тема)

### 📊 Статистика
- Количество смен за месяц
- Отработанные часы
- Зарплата (с учётом ставок)
- Круговая диаграмма распределения смен

### 💰 Бюджет
- Учёт расходов по категориям
- Расчёт баланса доходы/расходы
- Статистика по категориям

### ⚙️ Настройки
- Ставки за смены для расчёта зарплаты
- Периоды отпуска
- Экспорт/импорт данных (JSON)
- Ежедневные уведомления о смене

## Как запустить

### Через Android Studio
1. Откройте папку проекта (`workshift`) в Android Studio.
2. Дождитесь синхронизации Gradle.
3. Запустите конфигурацию `app` на эмуляторе или реальном устройстве (мин. Android 7.0 / API 24).

### Локальная сборка
```bash
# Debug APK
./gradlew assembleDebug

# Release APK
./gradlew assembleRelease

# Запуск тестов
./gradlew test
```

### GitHub Actions
При пуше в репозиторий автоматически собирается APK:
- **Actions** → выбранная сборка → **Artifacts** → `app-debug.apk`

## Структура проекта

```
app/
├── src/main/java/com/example/workshiftcalendar/
│   ├── MainActivity.kt              # Главная активность
│   ├── NotificationScheduler.kt     # Планировщик уведомлений
│   ├── domain/model/                # Доменные модели
│   │   ├── ShiftKind.kt
│   │   ├── ShiftDetails.kt
│   │   ├── ExpenseCategory.kt
│   │   └── Models.kt
│   ├── data/                        # Слой данных
│   │   ├── model/Dto.kt
│   │   ├── mapper/Mappers.kt
│   │   ├── local/WorkshiftLocalDataSource.kt
│   │   └── repository/WorkshiftRepository.kt
│   ├── ui/                          # UI слой
│   │   ├── viewmodel/WorkshiftViewModel.kt
│   │   ├── screens/                 # Экраны
│   │   │   ├── CalendarScreen.kt
│   │   │   ├── StatsScreen.kt
│   │   │   ├── BudgetScreen.kt
│   │   │   ├── TemplatesScreen.kt
│   │   │   └── SettingsScreen.kt
│   │   ├── components/              # Компоненты
│   │   └── theme/                   # Темы
│   └── ...
├── src/test/java/                   # Unit тесты
└── build.gradle
```

## Технологии

- **Jetpack Compose** — декларативный UI
- **Material 3** — современный дизайн
- **DataStore** — хранение данных
- **ViewModel + StateFlow** — управление состоянием
- **WorkManager** — фоновые уведомления
- **Gson** — сериализация JSON
- **MPAndroidChart** — графики статистики

## Требования

- Android 7.0+ (API 26)
- Android Studio Hedgehog+
- Java 17

## Лицензия

MIT

---

**Скачать последнюю версию:** [Releases](https://github.com/USERNAME/REPO/releases)

