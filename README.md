# BudgetApp

Hesap gerektirmeyen, Android için yerel bütçe uygulaması. Proje şu anda **ilk
ekran ön izlemesi** aşamasında: aylık bütçe kaydetme ve yedekleme henüz
uygulanmadı. Güncel aşamalar ve alınmış kararlar [ROADMAP.md](ROADMAP.md)
dosyasında tutulur.

## Tarayıcıdan ekran ön izlemesi

Android Studio kurmadan GitHub'da şu iki ekranı açabilirsin:

- [Örnek bütçe ekranı](docs/previews/sample-budget.png)
- [Boş bütçe ekranı](docs/previews/empty-budget.png)

<a href="docs/previews/sample-budget.png"><img src="docs/previews/sample-budget.png" alt="Örnek bütçe ekranı" width="280"></a>
<a href="docs/previews/empty-budget.png"><img src="docs/previews/empty-budget.png" alt="Boş bütçe ekranı" width="280"></a>

Bu görüntüler Android uygulamasının Compose ekranından üretilir. Yalnızca
görsel ön izlemedir; tıklanabilir uygulama veya web uygulaması değildir.
Örnek rakamlar telefona kaydedilmez.

## Android Studio ile canlı ekran ön izlemesi (isteğe bağlı)

1. [Android Studio](https://developer.android.com/studio) kur. Emülatör
   kurman gerekmiyor.
2. GitHub'daki `codex/android-setup` dalını bilgisayarına indir veya klonla.
   Android Studio'da **File > Open** ile `BudgetApp` klasörünü aç.
3. Android Studio'nun **Gradle Sync** işlemini bitirmesini bekle. İsterse
   Android SDK 36 bileşenlerini kur.
4. `app/src/main/java/com/hmagr/budgetapp/ui/BudgetHomeScreen.kt` dosyasını
   aç. Editörün **Split** veya **Design** görünümünden **Preview** panelini
   açıp **Build & Refresh** düğmesine bas.
5. **Örnek bütçe** ve **Boş bütçe** adlı iki ekran görünür. Örnek bütçedeki
   rakamlar yalnızca tasarımı göstermek içindir; telefona kaydedilmez.

Android Studio uygun bir JDK'yı kendisi sunar. Komut satırından derleme için
JDK 17 veya üzeri ve Android SDK 36 gerekir.

## İlk APK'yı derleme

Android Studio'da **Build > Build APK(s)** seçeneğini kullanabilirsin. Komut
satırında macOS/Linux için `./gradlew :app:assembleDebug`, Windows için
`gradlew.bat :app:assembleDebug` çalıştır. Debug APK şu dosyada oluşur:
`app/build/outputs/apk/debug/app-debug.apk`.

Bu ilk APK boş ekran düzenini açar. Gerçek bütçe, kayıt ve yedekleme
işlevleri yol haritasının sonraki aşamalarında eklenecek.

## Ekran görüntülerini yenileme

Ekran kodu değişince Android SDK ve JDK kurulu bilgisayarda
`./gradlew :app:updateDebugScreenshotTest` çalıştır. Üretilen referans
görüntüler `app/src/screenshotTestDebug/reference/` klasöründedir.
Tarayıcıda gösterilen `docs/previews/` kopyalarını da güncelle ve
`./gradlew :app:validateDebugScreenshotTest` ile doğrula.
