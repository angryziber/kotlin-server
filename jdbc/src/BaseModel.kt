package klite.jdbc

import java.sql.ResultSet
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaField

interface BaseModel {
  val id: UUID
}

abstract class Persistent<out T>: BaseModel {
  override lateinit var id: UUID

  val hasId get() = this::id.isInitialized
  fun withId(id: UUID = UUID.randomUUID()): T {
    require(!hasId) { "id already initialized: $id" }
    this.id = id
    @Suppress("UNCHECKED_CAST") return this as T
  }
}

inline fun <reified T: Any> T.toValues(vararg provided: Pair<KProperty1<T, *>, Any?>): Map<String, Any?> {
  val values = mapOf(*provided)
  return toValuesSkipping(values.keys) + values.mapKeys { it.key.name }
}

inline fun <reified T: Any> T.toValuesSkipping(vararg skip: KProperty1<T, *>) = toValuesSkipping(setOf(*skip))

inline fun <reified T: Any> T.toValuesSkipping(skip: Set<KProperty1<T, *>>): Map<String, Any?> =
  toValues(T::class.memberProperties - skip)

fun <T: Any> T.toValues(props: Iterable<KProperty1<T, *>>): Map<String, Any?> {
  if (this is Persistent<*> && !hasId) withId()
  return props.filter { it.javaField != null }.associate { it.name to it.javaField?.apply { trySetAccessible() }?.get(this) }
}

inline fun <reified T: Any> ResultSet.fromValues(vararg provided: Pair<KProperty1<T, *>, Any?>) = fromValues(T::class, *provided)

fun <T: Any> ResultSet.fromValues(type: KClass<T>, vararg provided: Pair<KProperty1<T, *>, Any?>) = type.primaryConstructor!!.let { constructor ->
  val extraArgs = provided.associate { it.first.name to it.second }
  val args = constructor.parameters.associateWith {
    if (extraArgs.containsKey(it.name)) extraArgs[it.name] else JdbcConverter.from(getObject(it.name), it.type)
  }
  constructor.callBy(args).apply { if (this is Persistent<*>) withId(getId()) }
}
