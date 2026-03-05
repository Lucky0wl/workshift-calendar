# 🚀 Развёртывание WorkshiftCalendar v4.0.0

## ✅ Что сделано

- [x] Обновление зависимостей (Kotlin 2.1.0, AGP 8.7.0, Compose BOM 2025.02.00)
- [x] Новая архитектура (Clean Architecture)
- [x] GitHub Actions CI/CD
- [x] 5 тестовых файлов
- [x] Экспорт/импорт данных
- [x] Статистика с графиками
- [x] Режим отпуска
- [x] Кастомные цвета
- [x] Уведомления на WorkManager

---

## 📦 Отправка на GitHub

### 1. Создайте репозиторий на GitHub

```
https://github.com/USERNAME/workshift.git
```

### 2. Отправьте код

```bash
cd c:\Users\Server\Desktop\workshift
git remote add origin https://github.com/USERNAME/workshift.git
git push -u origin master
git push origin v4.0.0
```

### 3. GitHub Actions запустится автоматически

Перейдите в репозиторий → **Actions** → увидите сборку.

---

## 🔐 Настройка подписи (опционально)

### Для подписанных релизных APK:

1. **Создайте keystore:**
```bash
keytool -genkey -v -keystore workshift.keystore -alias workshift ^
  -keyalg RSA -keysize 2048 -validity 10000 ^
  -storepass workshift2024 -keypass workshift2024 ^
  -dname "CN=Workshift, OU=Dev, O=Workshift, L=Moscow, S=Moscow, C=RU"
```

2. **Добавьте секреты в GitHub:**
   - Repository → Settings → Secrets and variables → Actions
   - `KEYSTORE_PATH` = `workshift.keystore`
   - `KEYSTORE_PASSWORD` = `workshift2024`
   - `KEY_ALIAS` = `workshift`
   - `KEY_PASSWORD` = `workshift2024`

3. **Загрузите keystore:**
```bash
git add workshift.keystore
git commit -m "Add keystore"
git push
```

⚠️ **Внимание:** Для публичных репозиториев НЕ коммитьте keystore!

---

## 📥 Загрузка APK

### После сборки GitHub Actions:

1. Перейдите в **Actions** → последняя сборка
2. В разделе **Artifacts** скачайте `app-debug.apk`
3. Установите на устройство

### Для релиза:

1. Создайте тег: `git tag v4.0.1 && git push origin v4.0.1`
2. GitHub Actions создаст релиз с APK
3. Перейдите в **Releases** → скачайте APK

---

## 🧪 Локальная сборка

```bash
cd c:\Users\Server\Desktop\workshift

# Debug APK
gradlew.bat assembleDebug

# Release APK
gradlew.bat assembleRelease

# Тесты
gradlew.bat test

# Полная очистка и сборка
gradlew.bat clean build
```

APK файлы:
- `app\build\outputs\apk\debug\app-debug.apk`
- `app\build\outputs\apk\release\app-release.apk`

---

## 📱 Установка на устройство

1. Включите **Отладку по USB** на устройстве
2. Подключите к ПК
3. Установите APK:
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

Или просто передайте APK файл на устройство и откройте его.

---

## 🔧 Troubleshooting

### Ошибка: "SDK location not found"
Создайте `local.properties`:
```
sdk.dir=C\:\\Users\\Server\\AppData\\Local\\Android\\Sdk
```

### Ошибка компиляции Java
Убедитесь, что Java 17 установлена:
```bash
java -version
```

### Gradle не загружает зависимости
Очистите кэш:
```bash
gradlew.bat --refresh-dependencies
```

---

## 📊 Мониторинг сборок

- **Actions:** https://github.com/USERNAME/workshift/actions
- **Releases:** https://github.com/USERNAME/workshift/releases
- **Issues:** https://github.com/USERNAME/workshift/issues

---

## 📝 Следующие шаги

1. Отправьте код на GitHub
2. Настройте секреты для подписи
3. Протестируйте сборку в GitHub Actions
4. Создайте первый релиз
5. Поделитесь приложением с пользователями!

---

**Успешной разработки! 🎉**
