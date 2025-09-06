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

## 3. Najważniejsze klasy i adnotacje

- `McpToolset` – interfejs i zasady tworzenia zestawu narzędzi.
- `McpToolsProvider` – opcjonalny sposób ręcznego dostarczania narzędzi.
- `McpTool` i `McpDescription` – adnotacje używane przy definicji narzędzi.

## 4. Dostęp do projektu i obsługa błędów

W ciele narzędzia projekt można pobrać z `CoroutineContext.project`. W celu zwrócenia błędu klientowi należy użyć funkcji `mcpFail()`.

## 5. Testy

Po dodaniu narzędzi uruchom testy modułu MCP Server, np.:

```bash
bazel test //plugins/mcp-server:mcpserver_test
```

W razie braku środowiska bazel mogą pojawić się błędy pobierania – należy je rozwiązać przed uruchomieniem testów.

