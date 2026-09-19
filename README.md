# Skazka Auth

> RU — основной язык · EN — required second language

## RU

Переиспользуемый контур авторизации Skazka, вынесенный из Skazka Hub.

Репозиторий разделён на два слоя:

- `auth-core` — чистая Java: PKCE, state/callback validation, проверка HTTPS base URL;
- `auth-android` — Android Keystore + локальное хранение bearer-сессии и pending PKCE.

UI, список провайдеров, Runtime Config и production endpoints намеренно остаются за пределами библиотеки.

**Статус:** `0.1.0-preview`. Auth Core self-test PASS; `:auth-android:assembleDebug` собран на HOSTKEY, AAR сформирован.

## EN

Reusable Skazka authentication components extracted from Skazka Hub.

The repository has two layers:

- `auth-core` — pure Java: PKCE, state/callback validation, HTTPS base URL validation;
- `auth-android` — Android Keystore-backed bearer session and pending PKCE storage.

UI, provider selection, Runtime Config, and production endpoints intentionally stay outside the library.

**Status:** `0.1.0-preview`. Auth Core self-test passes; `:auth-android:assembleDebug` has been built on HOSTKEY and produced an AAR.

## Coordinates / Координаты

- `com.kroxaboom.skazka:skazka-auth-core:0.1.0-preview`
- `com.kroxaboom.skazka:skazka-auth-android:0.1.0-preview`

## Verification / Проверка

```bash
bash ci/verify-core.sh
gradle :auth-android:assembleDebug
```

See [DEVELOPMENT_RULES.md](DEVELOPMENT_RULES.md).

> A license will be selected before the first stable public release. Until then, publication of the source does not grant reuse rights.
