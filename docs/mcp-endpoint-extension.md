# Rozszerzenie endpointu MCP o nowe narzędzia

Poniższe kroki opisują, jak dodać własne narzędzia (tools) do serwera MCP w IntelliJ.

## 1. Utworzenie zestawu narzędzi

1. Zaimplementuj klasę implementującą `McpToolset`.
2. Każdą funkcję, która ma być udostępniona jako narzędzie, oznacz adnotacją `@McpTool`.
3. Używaj `@McpDescription` do opisu funkcji oraz jej parametrów.
4. Parametry powinny być typami prymitywnymi lub serializowalnymi przez `kotlinx.serialization`.
5. Dozwolone są różne typy wyników: prymitywy, obiekty serializowalne, `McpToolCallResult`, `McpToolCallResultContent` lub `Unit` (interpretuje się jako sukces). W przypadku spodziewanych błędów zgłaszaj `McpExpectedError`.

Przykładowa implementacja zestawu narzędzi:

```kotlin
class MyToolset : McpToolset {
  @McpTool
  @McpDescription("Powitanie użytkownika")
  fun greet(
    @McpDescription("Imię użytkownika") name: String,
    @McpDescription("Liczba powitań") count: Int = 1,
  ): McpToolCallResult {
    val text = List(count) { "Cześć, $name!" }.joinToString("\n")
    return McpToolCallResult.text(text)
  }

  @McpTool(name = "fail_example")
  fun failExample(): Unit = mcpFail("Niepowodzenie przykładowe")
}
```

## 2. Rejestracja w plugin.xml

Dodaj wpis do `plugin.xml` w sekcji rozszerzeń:

```xml
<mcpServer.mcpToolset implementation="twoj.pakiet.MyToolset" />
```

Alternatywnie możesz użyć `McpToolsProvider` i zwrócić listę narzędzi w metodzie `getTools()`.

## 3. Klasy i ich API

### McpToolset
Interfejs oznaczający zestaw narzędzi. Nie posiada metod; narzędzia są zwykłymi metodami w klasie i odkrywane przez MCP na podstawie adnotacji. Rejestracja odbywa się poprzez extension point `com.intellij.mcpServer.mcpToolset`.

API narzędzia zdefiniowanego w `McpToolset` to zwykła funkcja Kotlina:

```kotlin
@McpTool
@McpDescription("Zwraca odwrotny tekst")
fun reverse(
  @McpDescription("Tekst wejściowy") text: String
): String = text.reversed()
```

Parametry opcjonalne deklaruje się standardowo:

```kotlin
@McpTool
fun repeat(text: String, times: Int = 1): String = text.repeat(times)
```

### McpToolsProvider
Alternatywny extension point (`com.intellij.mcpServer.mcpToolsProvider`), w którym należy zaimplementować metodę `fun getTools(): List<McpTool>` zwracającą gotowe obiekty narzędzi.

Przykładowa implementacja:

```kotlin
class EchoToolsProvider : McpToolsProvider {
  override fun getTools(): List<McpTool> = listOf(EchoTool())
}

class EchoTool : McpTool {
  override val descriptor = McpToolDescriptor(
    name = "echo",
    description = "Zwraca otrzymany tekst",
    inputSchema = McpToolSchema.ofPropertiesMap(
      properties = mapOf("text" to JsonPrimitive("string")),
      requiredProperties = setOf("text"),
      definitions = emptyMap()
    )
  )

  override suspend fun call(args: JsonObject): McpToolCallResult {
    val text = args["text"]?.jsonPrimitive?.content ?: return McpToolCallResult.error("Brak parametru text")
    return McpToolCallResult.text(text)
  }
}
```

### McpTool
Interfejs reprezentujący pojedyncze narzędzie. Wymaga implementacji:
- właściwości `descriptor: McpToolDescriptor` opisującej nazwę, opis i schemat wejścia/wyjścia,
- funkcji `suspend fun call(args: JsonObject): McpToolCallResult` zawierającej logikę narzędzia.

Implementacja przykładowego narzędzia:

```kotlin
class SumTool : McpTool {
  override val descriptor = McpToolDescriptor(
    name = "sum",
    description = "Dodaje dwie liczby",
    inputSchema = McpToolSchema.ofPropertiesMap(
      properties = mapOf(
        "a" to buildJsonObject { put("type", "number") },
        "b" to buildJsonObject { put("type", "number") }
      ),
      requiredProperties = setOf("a", "b"),
      definitions = emptyMap()
    )
  )

  override suspend fun call(args: JsonObject): McpToolCallResult {
    val a = args["a"]!!.jsonPrimitive.double
    val b = args["b"]!!.jsonPrimitive.double
    return McpToolCallResult.text((a + b).toString())
  }
}
```

### McpToolDescriptor
Opis narzędzia przekazywany klientowi. Zawiera pola:
- `name: String`,
- `description: String`,
- `inputSchema: McpToolSchema`,
- `outputSchema: McpToolSchema?` (opcjonalny opis danych zwracanych).

Tworzenie `McpToolDescriptor`:

```kotlin
val descriptor = McpToolDescriptor(
  name = "sum",
  description = "Dodaje dwie liczby",
  inputSchema = McpToolSchema.ofPropertiesMap(
    properties = mapOf(
      "a" to buildJsonObject { put("type", "number") },
      "b" to buildJsonObject { put("type", "number") }
    ),
    requiredProperties = setOf("a", "b"),
    definitions = emptyMap()
  ),
  outputSchema = McpToolSchema.ofPropertiesMap(
    properties = mapOf("result" to buildJsonObject { put("type", "number") }),
    requiredProperties = setOf("result"),
    definitions = emptyMap()
  )
)
```

### McpToolSchema
Reprezentuje schemat wejścia lub wyjścia w formacie JSON Schema. Udostępnia konstruktory pomocnicze `ofPropertiesMap` i `ofPropertiesSchema` oraz metodę `prettyPrint()` zwracającą sformatowany schemat.

```kotlin
val input = McpToolSchema.ofPropertiesMap(
  properties = mapOf(
    "text" to buildJsonObject { put("type", "string") }
  ),
  requiredProperties = setOf("text"),
  definitions = emptyMap()
)

println(input.prettyPrint())
```

### McpToolCallResult i McpToolCallResultContent
Wynik wywołania narzędzia. `McpToolCallResult` zawiera tablicę `content: Array<McpToolCallResultContent>`, opcjonalne `structuredContent: JsonObject` oraz flagę `isError`. Udostępnia metody fabryczne `text()` i `error()`. Jedyną dostępną implementacją `McpToolCallResultContent` jest `Text` przechowująca ciąg znaków.

```kotlin
// zwrócenie sukcesu
McpToolCallResult.text("Gotowe")

// zwrócenie błędu
McpToolCallResult.error("Coś poszło nie tak")

// własna konstrukcja z kilkoma blokami tekstu
McpToolCallResult(
  content = arrayOf(
    McpToolCallResultContent.Text("Linia 1"),
    McpToolCallResultContent.Text("Linia 2")
  )
)
```

### McpExpectedError i `mcpFail`
Klasa `McpExpectedError` reprezentuje błąd, który powinien zostać przekazany klientowi bez modyfikacji. Funkcja pomocnicza `mcpFail(message)` rzuca taki wyjątek.

```kotlin
@McpTool
fun readFile(path: String): String {
  val file = Paths.get(path)
  if (!Files.exists(file)) mcpFail("Plik nie istnieje: $path")
  return Files.readString(file)
}
```

### Adnotacje
- `@McpTool(name: String = "")` – oznacza metodę jako narzędzie (z opcjonalną nazwą).
- `@McpDescription("...")` – dodaje opis do narzędzia, parametru lub typu.

Przykład użycia adnotacji:

```kotlin
@McpTool(name = "hello")
@McpDescription("Proste przywitanie")
fun hello(@McpDescription("Imię odbiorcy") name: String): String = "Hello $name"
```

## 4. Dostęp do projektu i obsługa błędów

W ciele narzędzia projekt można pobrać z `CoroutineContext.project`. W celu zwrócenia błędu klientowi należy użyć funkcji `mcpFail()`.

```kotlin
@McpTool
suspend fun projectName(context: CoroutineContext): McpToolCallResult {
  val project = context.project ?: mcpFail("Brak projektu")
  return McpToolCallResult.text(project.name)
}
```

## 5. Testy

Po dodaniu narzędzi uruchom testy modułu MCP Server, np.:

```bash
bazel test //plugins/mcp-server:mcpserver_test
```

W razie braku środowiska bazel mogą pojawić się błędy pobierania – należy je rozwiązać przed uruchomieniem testów.
