# GitHub Actions CI/CD для Android проекта

## Настройка автоматической сборки

### 1. Workflow файл
Расположен: `.github/workflows/android-ci.yml`

### 2. Что делает workflow:
- ✅ Сборка при каждом push в main/master
- ✅ Запуск тестов
- ✅ Сборка debug APK
- ✅ Сборка release APK (без подписи)
- ✅ Создание релиза при создании тега (v1.0.0)

### 3. Настройка подписи для релизных сборок

#### Шаг 1: Создайте keystore
```bash
keytool -genkey -v -keystore workshift.keystore -alias workshift -keyalg RSA -keysize 2048 -validity 10000
```

#### Шаг 2: Добавьте секреты в GitHub
Перейдите в репозиторий → Settings → Secrets and variables → Actions → New repository secret

Добавьте следующие секреты:

| Secret Name | Описание |
|-------------|----------|
| `KEYSTORE_PATH` | Путь к keystore (обычно `workshift.keystore`) |
| `KEYSTORE_PASSWORD` | Пароль от keystore |
| `KEY_ALIAS` | Алиас ключа |
| `KEY_PASSWORD` | Пароль ключа |

#### Шаг 3: Загрузите keystore
```bash
# В настройках репозитория: Settings → Secrets and variables → Actions
# Нажмите "New repository secret" и добавьте:
# Имя: KEYSTORE_FILE
# Значение: base64-кодированный файл keystore
```

Или используйте GitHub Encrypted Secrets для файла:
```bash
# Зашифровать и загрузить keystore
git crypt add-gcm-credentials  # если используете git-crypt
```

### 4. Обновлённый build.gradle для GitHub Actions

Для поддержки подписи через GitHub Secrets, обновите `app/build.gradle`:

```groovy
signingConfigs {
    release {
        def keystorePath = System.getenv("KEYSTORE_PATH")
        if (keystorePath && file(keystorePath).exists()) {
            storeFile file(keystorePath)
            storePassword System.getenv("KEYSTORE_PASSWORD")
            keyAlias System.getenv("KEY_ALIAS")
            keyPassword System.getenv("KEY_PASSWORD")
        }
    }
}
```

### 5. Создание релиза

```bash
# Создайте тег версии
git tag v4.0.0
git push origin v4.0.0
```

GitHub Actions автоматически:
1. Соберёт подписанный APK
2. Создаст релиз на GitHub
3. Прикрепит APK к релизу

### 6. Артефакты сборки

После каждой сборки артефакты доступны:
- **Debug APK**: Вкладка Actions → выбранная сборка → артефакт `app-debug`
- **Release APK**: Вкладка Releases (при создании тега)

### 7. Локальная сборка

```bash
# Debug сборка
./gradlew assembleDebug

# Release сборка
./gradlew assembleRelease

# Запуск тестов
./gradlew test

# Полная сборка
./gradlew build
```

### 8. Статус сборки

Значок статуса можно добавить в README.md:
```markdown
![Android CI](https://github.com/USERNAME/REPO/workflows/Android%20CI/badge.svg)
```
