# Rozszerzenie endpointu MCP o nowe narzędzia

Poniższe kroki opisują, jak dodać własne narzędzia (tools) do serwera MCP w IntelliJ.

## 1. Utworzenie zestawu narzędzi

1. Zaimplementuj klasę implementującą `McpToolset`.
2. Każdą funkcję, która ma być udostępniona jako narzędzie, oznacz adnotacją `@McpTool`.
3. Używaj `@McpDescription` do opisu funkcji oraz jej parametrów.
4. Parametry powinny być typami prymitywnymi lub serializowalnymi przez `kotlinx.serialization`.
5. Dozwolone są różne typy wyników: prymitywy, obiekty serializowalne, `McpToolCallResult`, `McpToolCallResultContent` lub `Unit` (interpretuje się jako sukces). W przypadku spodziewanych błędów zgłaszaj `McpExpectedError`.

## 2. Rejestracja w plugin.xml

Dodaj wpis do `plugin.xml` w sekcji rozszerzeń:

```xml
<mcpServer.mcpToolset implementation="twoj.pakiet.MyToolset" />
```

Alternatywnie możesz użyć `McpToolsProvider` i zwrócić listę narzędzi w metodzie `getTools()`.

## 3. Klasy i ich API

### McpToolset
Interfejs oznaczający zestaw narzędzi. Nie posiada metod; narzędzia są zwykłymi metodami w klasie i odkrywane przez MCP na podstawie adnotacji. Rejestracja odbywa się poprzez extension point `com.intellij.mcpServer.mcpToolset`.

### McpToolsProvider
Alternatywny extension point (`com.intellij.mcpServer.mcpToolsProvider`), w którym należy zaimplementować metodę `fun getTools(): List<McpTool>` zwracającą gotowe obiekty narzędzi.

### McpTool
Interfejs reprezentujący pojedyncze narzędzie. Wymaga implementacji:
- właściwości `descriptor: McpToolDescriptor` opisującej nazwę, opis i schemat wejścia/wyjścia,
- funkcji `suspend fun call(args: JsonObject): McpToolCallResult` zawierającej logikę narzędzia.

### McpToolDescriptor
Opis narzędzia przekazywany klientowi. Zawiera pola:
- `name: String`,
- `description: String`,
- `inputSchema: McpToolSchema`,
- `outputSchema: McpToolSchema?` (opcjonalny opis danych zwracanych).

### McpToolSchema
Reprezentuje schemat wejścia lub wyjścia w formacie JSON Schema. Udostępnia konstruktory pomocnicze `ofPropertiesMap` i `ofPropertiesSchema` oraz metodę `prettyPrint()` zwracającą sformatowany schemat.

### McpToolCallResult i McpToolCallResultContent
Wynik wywołania narzędzia. `McpToolCallResult` zawiera tablicę `content: Array<McpToolCallResultContent>`, opcjonalne `structuredContent: JsonObject` oraz flagę `isError`. Udostępnia metody fabryczne `text()` i `error()`. Jedyną dostępną implementacją `McpToolCallResultContent` jest `Text` przechowująca ciąg znaków.

### McpExpectedError i `mcpFail`
Klasa `McpExpectedError` reprezentuje błąd, który powinien zostać przekazany klientowi bez modyfikacji. Funkcja pomocnicza `mcpFail(message)` rzuca taki wyjątek.

### Adnotacje
- `@McpTool(name: String = "")` – oznacza metodę jako narzędzie (z opcjonalną nazwą).
- `@McpDescription("...")` – dodaje opis do narzędzia, parametru lub typu.

## 4. Dostęp do projektu i obsługa błędów

W ciele narzędzia projekt można pobrać z `CoroutineContext.project`. W celu zwrócenia błędu klientowi należy użyć funkcji `mcpFail()`.

## 5. Testy

Po dodaniu narzędzi uruchom testy modułu MCP Server, np.:

```bash
bazel test //plugins/mcp-server:mcpserver_test
```

W razie braku środowiska bazel mogą pojawić się błędy pobierania – należy je rozwiązać przed uruchomieniem testów.
