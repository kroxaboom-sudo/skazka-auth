# Skazka Auth

**RU:** Переиспользуемый контур авторизации: запуск входа, возврат в приложение, сессия и безопасное хранение состояния.

**EN:** Reusable authentication flow: sign-in launch, app return, session handling and safe client state.

## Что здесь будет / What belongs here

- auth contracts
- browser/Custom Tabs return flow
- session state
- token/session adapters
- тестовые реализации без production-секретов

## Граница / Boundary

Конкретные серверные секреты, OAuth credentials и production-policy остаются в закрытом серверном контуре.

## Статус / Status

Миграция началась. Репозиторий уже выделен из общей архитектуры Skazka, но рабочий код переносится небольшими проверяемыми шагами. Пока API не помечен как stable, совместимость между версиями не гарантируется.

Migration has started. The repository is separated at the architecture level, while working code is being moved in small, verifiable steps. Until an API is marked stable, compatibility between versions is not guaranteed.

## Принципы / Principles

- RU — основной язык, EN — обязательный второй.
- Публичный код не содержит ключей, production-конфигурации, приватных маршрутов или закрытых endpoints.
- Общая логика не должна знать о конкретном приложении больше, чем требуется её публичному API.
- Исправление в общем модуле должно быть пригодно для всех клиентов Skazka, которые его подключают.

---

Skazka is built as a set of small reusable components instead of one growing monolith.
