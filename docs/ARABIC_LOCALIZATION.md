# 🌐 Arabic Localization & Morphology Guide | دليل التعريب وتجهيز اللغة العربية

This document provides a comprehensive guide for developers on how Arabic localization (l10n), string extraction, text normalization, and Arabic morphological processing (قواعد النحو والصرف والبحث الدلالي) are integrated into **Privacify**.

---

## 📌 Table of Contents / فهرس المحتويات
1. [Overview & String Extraction Methodology](#1-overview--string-extraction-methodology)
2. [Arabic Language Rules in Software (النحو والصرف)](#2-arabic-language-rules-in-software-النحو-والصرف)
   - [Diacritics Removal (تجريد التشكيل)](#a-diacritics-removal-تجريد-التشكيل)
   - [Character Normalization (توحيد الأحرف المتشابهة)](#b-character-normalization-توحيد-الأحرف-المتشابهة)
   - [Prefix Stripping for Semantic Search (التجريد الدلالي واللواحق)](#c-prefix-stripping-for-semantic-search-التجريد-الدلالي-واللواحق)
   - [Pluralization Engine (أحكام المثنى والجمع والعد)](#d-pluralization-engine-أحكام-المثنى-والجمع-والعد)
3. [Jetpack Compose & RTL Support (دعم الاتجاه من اليمين لليسار)](#3-jetpack-compose--rtl-support)
4. [Integrating with Translation Platforms (Weblate / Crowdin)](#4-integrating-with-translation-platforms-weblate--crowdin)

---

## 1. Overview & String Extraction Methodology

To make Privacify multi-language ready, all hardcoded text strings in Jetpack Compose UI components (`Text("...")`, `title = "..."`, etc.) are decoupled and moved into Android's XML string resources:

* **Default Strings (English):** `app/src/main/res/values/strings.xml`
* **Arabic Strings (العربية):** `app/src/main/res/values-ar/strings.xml`

### Kotlin Code Pattern:
```kotlin
// ❌ Incorrect (Hardcoded):
Text(text = "Exempted Apps")

// ✅ Correct (Localized):
Text(text = stringResource(R.string.exemptions_title))
```

---

## 2. Arabic Language Rules in Software (النحو والصرف)

Arabic is a highly inflectional language with complex morphology. When implementing search functionality (e.g. searching app names or permission descriptions), standard naive string matching fails. Privacify adopts Arabic morphological rules for search indexing and text rendering.

### A. Diacritics Removal (تجريد التشكيل)
Arabic diacritics (الحركات) modify character pronunciation but should be ignored during semantic search.

| Character | Unicode Code | Name |
|-----------|--------------|------|
| `َ` | `U+064B` | Fathatan |
| `ُ` | `U+064C` | Dammatan |
| `ِ` | `U+064D` | Kasratan |
| `َ` | `U+064E` | Fatha |
| `ُ` | `U+064F` | Damma |
| `ِ` | `U+0650` | Kasra |
| `ّ` | `U+0651` | Shadda |
| `ْ` | `U+0652` | Sukun |

**Kotlin Implementation:**
```kotlin
fun String.removeArabicDiacritics(): String {
    val diacriticsRegex = Regex("[\\u064B-\\u0652]")
    return this.replace(diacriticsRegex, "")
}
```

---

### B. Character Normalization (توحيد الأحرف المتشابهة)
Users often interchange letter variations (such as `أ`, `إ`, `آ` or `ة` vs `ه`). Normalization transforms variants to canonical forms before search comparison.

```kotlin
fun String.normalizeArabicText(): String {
    return this.removeArabicDiacritics()
        .replace(Regex("[أإآٱ]"), "ا")  // Normalize Alef forms to bare Alef
        .replace('ى', 'ي')              // Normalize Alef Maqsura to Ya
        .replace('ة', 'ه')              // Normalize Ta Marbuta to Ha
}
```

---

### C. Prefix Stripping for Semantic Search (التجريد الدلالي واللواحق)
In Arabic semantic search, prefixes like the definite article (`ال`), conjunctions (`و`, `ف`), or prepositions (`ب`, `ك`, `ل`) are stripped to find root matches.

```kotlin
fun String.stripArabicPrefixes(): String {
    var text = this.normalizeArabicText()
    val prefixes = listOf("وال", "فال", "بال", "كال", "لال", "ال", "و", "ف", "ب", "ك", "ل")
    for (prefix in prefixes) {
        if (text.startsWith(prefix) && text.length > prefix.length + 2) {
            text = text.substring(prefix.length)
            break
        }
    }
    return text
}
```

---

### D. Pluralization Engine (أحكام المثنى والجمع والعد)
Arabic has 6 plural forms based on quantity (0, 1, 2, 3-10, 11-99, 100+). Android's `<plurals>` resource supports all 6 forms.

**`res/values-ar/strings.xml` example:**
```xml
<plurals name="apps_count">
    <item quantity="zero">لا توجد تطبيقات</item>
    <item quantity="one">تطبيق واحد</item>
    <item quantity="two">تطبيقان</item>
    <item quantity="few">%d تطبيقات</item>
    <item quantity="many">%d تطبيقاً</item>
    <item quantity="other">%d تطبيق</item>
</plurals>
```

**Kotlin Access:**
```kotlin
val text = pluralStringResource(R.plurals.apps_count, count, count)
```

---

## 3. Jetpack Compose & RTL Support

Arabic is read Right-to-Left (RTL). Jetpack Compose natively supports RTL when standard Layout components are used.

### 1. Vector Asset Mirroring:
Icons representing direction (like back arrows) automatically flip in RTL:
```kotlin
Icon(
    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
    contentDescription = stringResource(R.string.action_back)
)
```

### 2. Alignment & Padding:
Use `Modifier.padding(start = ..., end = ...)` instead of `left` or `right` to ensure margins flip correctly in Arabic.

---

## 4. Integrating with Translation Platforms (Weblate / Crowdin)

To connect Privacify with platforms like Weblate:
1. Ensure all strings reside in `app/src/main/res/values/strings.xml`.
2. Link the repository path `app/src/main/res/values/strings.xml` and target `app/src/main/res/values-*/strings.xml`.
3. Weblate will automatically sync community translations into `values-ar/strings.xml` via pull requests.

---
*Created by Privacify Open Source Contributors for Multi-language & Arabic Support.*
