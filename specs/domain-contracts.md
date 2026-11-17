# Модели

```kotlin
// model/Rule.kt
data class Rule(
    val id: Long,
    val name: String,
    val enabled: Boolean,
    val priority: Int,
    val actionType: ActionType,
    val actionValue: String,
    val simSlot: Int?,
    val conditions: List<RuleCondition>
)

enum class ActionType { STRIP, PREPEND, STRIP_PREPEND }

data class RuleCondition(
    val type: ConditionType,
    val value: String
)

enum class ConditionType { LENGTH, STARTS_WITH, LABEL }
```

```kotlin
// model/Exception.kt
data class Exception(
    val id: Long,
    val type: TargetType,
    val value: String,
    val locked: Boolean
)

enum class TargetType { NUMBER, LABEL }
```

```kotlin
// model/RuleTarget.kt  — белый список
data class RuleTarget(
    val ruleId: Long,
    val type: TargetType,
    val value: String
)
```

```kotlin
// model/DialContext.kt — вход в движок
data class DialContext(
    val number: String,
    val simSlot: Int?,
    val contactLabels: Set<String>
)
```

```kotlin
// model/TransformResult.kt
sealed interface TransformResult {
    data class Transformed(
        val dialedNumber: String,
        val appliedRuleId: Long?
    ) : TransformResult

    data class Unchanged(val originalNumber: String) : TransformResult

    data class Blocked(val reason: BlockReason) : TransformResult
}

enum class BlockReason { EXCEPTION_MATCH, NO_RULE_MATCHED }
```

`Blocked` нужен, если исключение означает «не звонить» — но по ТЗ исключения означают «не применять правила», не «блокировать». Поэтому в текущей модели `Blocked` **не используется** для исключений: исключение → `Unchanged`. `Blocked` оставлен под будущие сценарии (чёрный список). Пока — не задействован, но тип на месте.

## Движок

```kotlin
// engine/RuleEngine.kt
interface RuleEngine {
    fun transform(context: DialContext): TransformResult
}
```

Одна функция. Вся логика first-match, исключений, белого списка — внутри `DefaultRuleEngine`. Никаких `loadRules()` — движок получает правила через конструктор, состояние иммутабельно.

```kotlin
// engine/DefaultRuleEngine.kt — сигнатура конструктора
class DefaultRuleEngine(
    private val rules: List<Rule>,          // отсортированы по priority ASC
    private val whitelist: List<RuleTarget>,
    private val exceptions: List<Exception>,
    private val transformer: NumberTransformer
) : RuleEngine
```

Иммутабельный снимок правил. При изменении правил в БД — создаётся новый экземпляр. Это дёшево (правил десятки, не тысячи) и убирает гонки.

## Трансформер

```kotlin
// transform/NumberTransformer.kt
interface NumberTransformer {
    fun apply(number: String, actionType: ActionType, actionValue: String): String
}
```

`DefaultNumberTransformer` — чистая функция, без состояния. `STRIP` — отрезать N, `PREPEND` — добавить, `STRIP_PREPEND` — обе операции (формат `actionValue` для этого типа надо зафиксировать: `"strip:1|prepend:9"` или два поля? — **вопрос к тебе, см. ниже**).

## Матчинг условий

```kotlin
// engine/ConditionMatcher.kt
interface ConditionMatcher {
    fun matches(condition: RuleCondition, context: DialContext): Boolean
}
```

Отдельный интерфейс, потому что условий три типа и они будут расти. `DefaultConditionMatcher` — `when` по `ConditionType`. Тестируется отдельно от движка.

## Репозитории

```kotlin
// repository/RuleRepository.kt
interface RuleRepository {
    fun observeRules(): Flow<List<Rule>>
    suspend fun getSnapshot(): RuleSnapshot
}

data class RuleSnapshot(
    val rules: List<Rule>,
    val whitelist: List<RuleTarget>,
    val exceptions: List<Exception>
)
```

```kotlin
// repository/CallLogExtrasRepository.kt
interface CallLogExtrasRepository {
    suspend fun save(extras: CallLogExtras)
    suspend fun getByCallLogId(callLogId: Long): CallLogExtras?
}
```

## Юнит-тесты, которые пишутся вместе с этим

| Тест | Что проверяет |
|---|---|
| `firstMatchWins` | Два правила матчатся → применяется с меньшим priority |
| `exceptionBlocksAllRules` | Номер в exceptions → `Unchanged`, правила не тронуты |
| `whitelistOverridesPriority` | Номер в whitelist → правило X применено, даже если priority выше |
| `conditionAndLogic` | Правило с LENGTH+STARTS_WITH матчится только при обоих |
| `simSlotNullFailSafe` | `simSlot=null` в контексте → правило с `simSlot!=null` не матчится |
| `stripPrepend` | `STRIP_PREPEND` даёт ожидаемую строку |
| `disabledRuleSkipped` | `enabled=false` не участвует |

---

## Один вопрос, который надо решить до реализации

**`STRIP_PREPEND` — как хранить обе операции в одном `actionValue`?**

| Вариант | Плюс | Минус |
|---|---|---|
| Строка `"1\|9"` (strip\|prepend) | Одно поле, простая миграция | Парсинг, хрупко при пустых значениях |
| Два поля в `Rule`: `stripCount: Int?`, `prependValue: String?` | Типобезопасно, читаемо | Меняет схему ТЗ (раздел 5) |
| JSON в `actionValue` | Гибко | Оверхед, парсинг ради парсинга |
