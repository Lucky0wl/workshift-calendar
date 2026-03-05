# Настройка CI/CD для WorkshiftCalendar

## 🚀 Быстрый старт

### 1. Push в репозиторий

```bash
git init
git add .
git commit -m "Initial commit v4.0.0"
git remote add origin https://github.com/USERNAME/workshift.git
git push -u origin main
```

### 2. GitHub Actions автоматически запустит сборку

Перейдите в репозиторий → **Actions** → увидите запущенную сборку.

### 3. Скачайте APK

После успешной сборки:
- **Actions** → выбранная сборка → **Artifacts** → `app-debug.apk`

---

## 📦 Создание релиза

### С тегом версии

```bash
git tag v4.0.0
git push origin v4.0.0
```

GitHub Actions:
1. Соберёт release APK
2. Создаст релиз на GitHub
3. Прикрепит APK к релизу

---

## 🔐 Настройка подписи (опционально)

### Для подписи релизных сборок:

1. **Создайте keystore:**
```bash
keytool -genkey -v -keystore workshift.keystore -alias workshift \
  -keyalg RSA -keysize 2048 -validity 10000
```

2. **Добавьте секреты в GitHub:**
   - Repository → Settings → Secrets and variables → Actions
   - Добавьте:
     - `KEYSTORE_PATH` = `workshift.keystore`
     - `KEYSTORE_PASSWORD` = ваш пароль
     - `KEY_ALIAS` = `workshift`
     - `KEY_PASSWORD` = пароль ключа

3. **Загрузите keystore в репозиторий:**
```bash
git add workshift.keystore
git commit -m "Add keystore"
git push
```

⚠️ **Внимание:** Не коммитьте keystore в публичный репозиторий! Используйте приватный репозиторий или храните ключ отдельно.

---

## 📥 Локальная сборка

```bash
# Debug APK
./gradlew assembleDebug

# Release APK
./gradlew assembleRelease

# Тесты
./gradlew test

# Полная сборка
./gradlew build
```

APK файлы будут в:
- `app/build/outputs/apk/debug/app-debug.apk`
- `app/build/outputs/apk/release/app-release.apk`

---

## 🏷️ Версионирование

Обновляйте версию в `app/build.gradle`:

```groovy
versionCode 22      // Увеличивайте на 1 при каждом релизе
versionName "4.0.0" // Семантическая версионность
```

---

## 📊 Статус сборки

Добавьте бейдж в README.md:

```markdown
![Android CI](https://github.com/USERNAME/REPO/actions/workflows/android-ci.yml/badge.svg)
```

---

## 🔧 Troubleshooting

### Сборка падает с ошибкой подписи

Убедитесь, что `signingConfigs` настроен корректно:

```groovy
signingConfigs {
    release {
        def keystorePath = System.getenv("KEYSTORE_PATH")
        if (keystorePath && file(keystorePath).exists()) {
            storeFile file(keystorePath)
            storePassword System.getenv("KEYSTORE_PASSWORD") ?: ""
            keyAlias System.getenv("KEY_ALIAS") ?: ""
            keyPassword System.getenv("KEY_PASSWORD") ?: ""
        }
    }
}
```

### Gradle не находит зависимости

Проверьте `settings.gradle`:
```groovy
repositories {
    google()
    mavenCentral()
    maven { url 'https://jitpack.io' }
}
```

### Ошибка Java версии

Убедитесь, что используется Java 17:
```bash
java -version
```

В GitHub Actions Java 17 настраивается автоматически.
