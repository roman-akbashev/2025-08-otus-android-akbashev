# Проектная работа: Приложение для изучения английских слов

### Цель

Сдать и защитить проект

### Задание

- Проект должен быть реализован по Single Activity Application паттерну, то есть в приложении
  должна использоваться только одна активити, а остальные экраны реализуются через фрагменты или
  Compose.
- Навигацию можно организовать с использованием библиотеки Navigation Component или другой
  популярной библиотеки.
- Для презентационного слоя используйте архитектуру MVVM/MVI. Если будете использовать MVI, можете
  сделать самописный вариант или взять популярную библиотеку (при использовании сторонней
  библиотеки согласуйте это предварительно с руководителем курса).
- Сделайте разбивку на слои. Слоистая или Чистая архитектура — выбирайте сами.
- Желательно использовать Jetpack Compose, а не фрагменты, но это не обязательно.
- Приложение должно быть многомодульным — декомпозируйте фичи по модулям.
- Обязательно используйте DI для организации архитектуры. Желательно Dagger2 или Hilt; можно
  использовать Koin, если будете делать KMP-проект.
- Для асинхронных операций используйте Kotlin Coroutines.
- Для сетевого взаимодействия используйте Retrofit/Ktor. Для сериализации/десериализации JSON —
  Gson, Moshi или Kotlin Serialization.
- Вы можете сделать проект с поддержкой KMP. Это будет плюсом, но не обязательно.
- Покройте unit тестами 5 классов. Обязательно должна быть покрыта ViewModel (или её аналог, если
  используете MVI). Напишите UI тесты для одного пользовательского сценария.
- Подключите к проекту статический анализатор Detekt.

### LinguaCards

LinguaCards – Android-приложение для изучения английских слов с использованием алгоритма
интервальных повторений. Позволяет создавать колоды карточек, отслеживать прогресс и
повторять слова в оптимальные моменты.

### Возможности

- Создание и управление колодами карточек
- Добавление, редактирование и удаление карточек (слово, перевод, транскрипция, пример
  использования)
- Автоматический подбор транскрипции и примера через Free Dictionary API
- Режим изучения с переворотом карточки и выбором оценки (Не знаю / Сложно / Нормально / Легко)
- Отслеживание даты следующего повторения на основе алгоритма SM-2
- Поиск по колодам и карточкам
- Тёмная / светлая тема с поддержкой динамических цветов (Android 12+)
- Полная поддержка русского и английского языков интерфейса

<img src="img/DeckList.png" width="200" />
<img src="img/DeckDetails.png" width="200" />
<img src="img/AddCard.png" width="200" />
<img src="img/Study.png" width="200" />

### Технологии:

- Язык – Kotlin
- UI – Jetpack Compose (Material 3)
- Навигация – Compose Navigation
- DI – Dagger Hilt
- База данных – Room (SQLite)
- Сеть – Retrofit + OkHttp, сериализация kotlinx.serialization (JSON)
- Асинхронность – Kotlin Coroutines + Flow
- Архитектура – Clean Architecture (слои: presentation, domain, data) + MVVM (StateFlow)
- Тестирование
    - Unit-тесты: JUnit, Turbine, MockK
    - UI-тесты: Kaspresso, Compose Test
- Статический анализ – Detekt + кастомные правила (ComposeModifierMissingRule,
  NoMutableStateWithoutRememberRule)
- Сборка – Gradle Kotlin DSL, version catalogs

### Структура проекта

```
LinguaCards/
├── app/                        Главный модуль приложения
├── core/                       Базовые модули
│   ├── model/                  Модели данных (Deck, Card, SrsGrade)
│   ├── domain/                 Use cases и репозитории (интерфейсы)
│   ├── data/                   Реализация репозиториев
│   ├── database/               Room (сущности, DAO)
│   └── network/                Retrofit-клиент, DTO, мапперы
├── features/                   Фичи (экраны)
│   ├── about/                  О приложении
│   ├── decklist/               Список колод
│   ├── deckdetail/             Детали колоды + список карточек
│   ├── cardedit/               Создание/редактирование карточки
│   ├── study/                  Режим изучения
│   └── about/                  Экран "О приложении"
├── detekt-rules/               Кастомные правила Detekt
├── ci/                         Docker/Jenkins для сборки и тестирования
└── buildSrc/                   Кастомный плагин для публикации приложения
```

### Диаграмма зависимостей:

```
          ┌─────────────────┐
          │     :app        │
          │    (сборка)     │
          └────────┬────────┘
                   │
         ┌─────────┼─────────┐
         │                   │
         ▼                   ▼
┌────────────────┐  ┌────────────────┐
│ :features:*    │  │ :core:data     │
│ (UI слои)      │  │ (реализации)   │
└───────┬────────┘  └───────┬────────┘
        │                   │
        │                   ▼
        │          ┌────────────────┐
        │          │ :core:database │
        │          │ :core:network  │
        │          └───────┬────────┘
        │                  │
        ▼                  ▼
┌─────────────────────────────────────┐
│          :core:domain               │
│    (интерфейсы и бизнес-логика)     │
└─────────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────┐
│          :core:model                │
│      (общие модели данных)          │
└─────────────────────────────────────┘
```

### Архитектура

<img src="img/architecture.png" width="600" />

- Presentation – Compose UI + ViewModel (Hilt). Управляет состоянием через StateFlow.
- Domain – Независимые сущности (Deck, Card), интерфейсы репозиториев и бизнес-логика.
- Data – Реализация репозиториев, работа с Room (сущности, DAO) и сетью (Retrofit, DTO, мапперы).

### Сборка и запуск

**Требования:**
- Android Studio Panda (2025.3.2) или новее
- JDK 21
- Android SDK (minSdk 24, compileSdk 36)

**Шаги:**
- git clone https://github.com/roman-akbashev/2025-08-otus-android-akbashev.git
- cd 2025-08-otus-android-akbashev/hw-17-lingua-cards
- Откройте проект в Android Studio.
- Дождитесь синхронизации Gradle.
- Подключите физическое устройство или запустите эмулятор.
- Нажмите Run (зелёная стрелка) или выполните команду:
  - ./gradlew installDebug

### Тестирование

**Unit-тесты:**
Запуск всех unit-тестов (модули core, features):
- ./gradlew test

Запуск тестов конкретного модуля, например core/domain:
- ./gradlew :core:domain:test

**UI-тесты :**
- ./gradlew connectedAndroidTest

### Статический анализ (Detekt)

**Проект настроен на использование Detekt с кастомными правилами (модуль detekt-rules):**
- ComposeModifierMissingRule – требует указания параметра modifier у Composable-функций (кроме Preview).
- NoMutableStateWithoutRememberRule – запрещает использование mutableStateOf без обёртки в remember внутри Composable-функций.

**Запуск проверки:**
- ./gradlew detekt
- ./gradlew build

  Конфигурация находится в config/detekt/detekt.yml.

###  CI/CD
**Сборка агента Jenkins (все необходимое окружение для сборки и тестирования):**
- git clone https://github.com/roman-akbashev/2025-08-otus-android-akbashev.git
- cd 2025-08-otus-android-akbashev/hw-17-lingua-cards/ci/android-agent
- docker build -t android-agent .
- для тестирования окружения, в корне проекта вызвать:
- docker run -it --rm -v $(pwd):/workspace -v gradle-cache:/root/.gradle -v android-sdk-cache:/opt/android-sdk android-agent /bin/bash

в консоли контейнера попытаться собрать и протестировать проект, чтобы убедиться, что все работает

**Сборка и запуск сервера Jenkins:**
- git clone https://github.com/roman-akbashev/2025-08-otus-android-akbashev.git
- cd 2025-08-otus-android-akbashev/hw-17-lingua-cards/ci/jenkins-master
- docker build -t jenkins-master .
- docker run -d -p 8080:8080 -p 50000:50000 -v jenkins_home:/var/jenkins_home -v /var/run/docker.sock:/var/run/docker.sock -u root --name jenkins-master jenkins-master
- открыть http://localhost:8080, получить пароль из логов контейнера: docker exec CONTAINER_ID cat /var/jenkins_home/secrets/initialAdminPassword
- установить предложенные умолчанию плагины.
- в Jenkins создайте и настройте новую задачу типа "Multibranch Pipline"
- запустите сборку

<img src="img/jenkins.png" width="800" />

###  Публикация
./gradlew :app:publishToPlayStore -PplayTrack=beta -PplayReleaseNotes="Fixed bugs"

###  Лицензия
MIT License. Подробнее в файле LICENSE 

###  Контакты
Разработчик: Роман Акбашев
Email: roman-akbashev@mail.ru