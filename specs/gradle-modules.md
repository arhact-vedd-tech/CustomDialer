# Gradle-модули

```
:app                    — Application, DI-граф, MainActivity, навигация
:core:data              — Room, репозитории, Telecom/CallLog/Contacts providers
:core:domain            — RuleEngine, NumberTransformer, модели, use cases. Без Android SDK
:core:ui                — тема, общие Compose-компоненты (кнопки, бейджи)
:feature:dialer         — экран набора, T9
:feature:incall         — InCallService, экран активного вызова
:feature:calllog        — журнал вызовов
:feature:contacts       — контакты, избранное
:feature:rules          — конструктор правил, исключения, белый список
:feature:settings       — настройки, onboarding, импорт/экспорт
:service:redirection    — CallRedirectionService (изолирован, т.к. критичен)
```

**Почему `:service:redirection` отдельно:** сервис объявлен в манифесте, его нельзя «сломать» правкой UI-модуля. Компиляционная граница = защита от случайной зависимости.

## Внутри модуля

```
:core:domain
├── model/          — Rule, RuleCondition, RuleTarget, Exception, Label, TransformResult
├── engine/         — RuleEngine (интерфейс), DefaultRuleEngine (impl)
├── transform/      — NumberTransformer (интерфейс), DefaultNumberTransformer
└── repository/     — интерфейсы: RuleRepository, ExceptionRepository, CallLogExtrasRepository
```

```
:core:data
├── db/             — Room: AppDatabase, Entity, Dao, миграции
├── repository/     — реализации интерфейсов из :core:domain
├── telecom/        — TelecomManager-обёртка, PhoneAccountHandle → simSlot
├── calllog/        — CallLog Provider
└── contacts/       — Contacts Provider, ContactIndex
```

```
:feature:dialer
├── ui/             — Compose-экраны, ViewModel
├── domain/         — use cases, специфичные для набора (T9-поиск)
└── di/             — Hilt-модуль фичи
```

## Правила зависимостей

| Модуль | Зависит от | Не зависит от |
|---|---|---|
| `:core:domain` | ничего (только Kotlin stdlib + coroutines) | Android SDK |
| `:core:data` | `:core:domain` | любых `:feature:*` |
| `:feature:*` | `:core:domain`, `:core:ui` | других `:feature:*` |
| `:service:redirection` | `:core:domain`, `:core:data` | `:feature:*` |
| `:app` | всё | — |

**Правило:** фичи не зависят друг от друга. Навигация между ними — через `:app`. Если фиче нужен экран из другой — это сигнал, что use case общий и его место в `:core:domain`.

## Что это даёт

- `RuleEngine` тестируется юнит-тестами без эмулятора — он в `:core:domain`, чистая Kotlin.
- `CallRedirectionService` не может случайно потянуть UI-зависимости — компилятор не даст.
- Параллельная работа: `:feature:calllog` и `:feature:rules` не пересекаются.
