# Skazka Auth

> RU — основной язык · EN — required second language

## RU

Переиспользуемый контур авторизации, вынесенный из Skazka Hub без production endpoints и app-specific UI.

**Статус:** `0.1.0-preview` — два независимых модуля уже собираются на HOSTKEY.

- `auth-core` — PKCE S256, state, callback validation и проверка HTTPS base URL. Чистая Java 17, без runtime-зависимостей.
- `auth-android` — шифрованное хранение bearer session и pending PKCE через Android Keystore.
- UI, список провайдеров, RuntimePack и профиль пользователя остаются на уровне приложения/сервера.

Проверено: core self-test — PASS; `:auth-android:assembleDebug` — PASS.

## EN

Reusable authentication building blocks extracted from Skazka Hub without production endpoints or app-specific UI.

**Status:** `0.1.0-preview` — both modules build successfully on HOSTKEY.

- `auth-core` — PKCE S256, state, callback validation, and HTTPS base URL validation. Pure Java 17 with no runtime dependencies.
- `auth-android` — encrypted bearer-session and pending-PKCE storage backed by Android Keystore.
- UI, provider lists, RuntimePack, and user-profile presentation stay at the app/server layer.

Verified: core self-test — PASS; `:auth-android:assembleDebug` — PASS.

## Coordinates / Координаты

- `com.kroxaboom.skazka:auth-core:0.1.0-preview`
- `com.kroxaboom.skazka:auth-android:0.1.0-preview`

## Security boundary / Граница безопасности

Provider secrets, passwords, cookies, signing keys, private routes, and production configuration are not part of this repository.

See [DEVELOPMENT_RULES.md](DEVELOPMENT_RULES.md).

> A license will be selected before the first stable public release. Until then, publication of the source does not grant reuse rights.
