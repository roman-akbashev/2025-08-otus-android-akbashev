# Домашнее задание: Dependency Injection на Dagger 2

### Цель
Научиться проектировать DI-граф в Android-приложении с помощью Dagger 2

### Задание

1. Переведите приложение с паттерна Service Locator на DI, используя Dagger 2.
2. Для одной фичи используйте подход Component Dependencies, для второй — Subcomponent

### Описание проекта
Приложение **Products** с двумя экранами:
- **Products Screen** - список продуктов
- **Favorites Screen** - список избранных продуктов

<image width="200" src="https://github.com/Otus-Android/DIHomework/blob/master/img/1.png?raw=true"> <image width="200" src="https://github.com/Otus-Android/DIHomework/blob/master/img/2.png?raw=true">

**Технологии:**
- Jetpack Compose для UI
- Navigation Compose для навигации
- Retrofit для загрузки данных
- DataStore для хранения избранного
- Clean Architecture
- Dagger 2 для DI

### Подробнее что надо сделать

#### Создайте AppComponent (Singleton)
Главный компонент приложения с областью видимости `@Singleton`.

**Что должен предоставлять:**
- Сетевые зависимости (Retrofit, OkHttp, ProductApiService)
- Репозитории (ProductRepository, FavoritesRepository)
- Use Cases (ConsumeProductsUseCase, ConsumeFavoritesUseCase, ToggleFavoriteUseCase)
- Утилиты (PriceFormatter)
- Context приложения

**Подсказки:**
- Используйте `@BindsInstance` для передачи `Context` в компонент
- AppComponent реализует только ProductsDependencies (не FavoritesDependencies!)
- Для Favorites используется метод `favoritesComponent()` который возвращает фабрику сабкомпонента
- Не забудьте добавить SubcomponentsModule в список модулей
- Создайте компонент в классе Application

#### Создайте ProductsComponent (Component Dependencies)

Для фичи Products сделайте компонент через Component Dependencies

```kotlin
@FeatureScope
@Component(dependencies = [ProductsDependencies::class])
interface ProductsComponent {
    fun viewModelFactory(): ProductsViewModelFactory

    @Component.Factory
    interface Factory {
        fun create(dependencies: ProductsDependencies): ProductsComponent
    }
}
```

#### Создайте FavoritesComponent (Subcomponent)

Для фичи Favorites сделайте компонент через Subcomponent

```kotlin
@FeatureScope
@Subcomponent
interface FavoritesComponent {
    fun viewModelFactory(): FavoritesViewModelFactory

    @Subcomponent.Factory
    interface Factory {
        fun create(): FavoritesComponent
    }
}
```

**Подсказки:**
- Используйте собственную аннотацию `@FeatureScope` в модуле `common:di`
- Products использует Component Dependencies (через интерфейс)
- Favorites использует Subcomponent (прямая зависимость от AppComponent)
- Каждый компонент предоставляет только ViewModelFactory для своей фичи
- Базовый интерфейс `Dependencies` должен быть пустым маркером
- Объявляйте только минимально необходимые зависимости
- AppComponent реализует интерфейс ProductsDependencies
- Получайте зависимости, например, через `findDependencies<ProductsDependencies>()`, как показано ниже:

```kotlin
interface Dependencies

interface DependenciesProvider {
   fun getDependencies(): Dependencies
}

inline fun <reified T : Dependencies> Context.findDependencies(): T {
   return (applicationContext as DependenciesProvider).getDependencies() as T
}
```
Это один из способов. Можете сделать иначе.

#### DON'T (Не делайте так)

1. **Не передавайте весь компонент**
```kotlin
// Плохо
@Component(dependencies = [AppComponent::class])

// Хорошо
@Component(dependencies = [FeatureDependencies::class])
```

2. **Не используйте скоуп для stateless классов**
```kotlin
// Плохо - use case без состояния
@Singleton
class MyUseCase @Inject constructor(...)

// Хорошо
class MyUseCase @Inject constructor(...)
```

**Удачи в освоении Dependency Injection! 🚀**
