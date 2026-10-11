# BudgetApp

Hesap gerektirmeyen, Android için yerel bütçe uygulaması. Proje şu anda **ilk
ekran ön izlemesi** aşamasında: aylık bütçe kaydetme ve yedekleme henüz
uygulanmadı. Güncel aşamalar ve alınmış kararlar [ROADMAP.md](ROADMAP.md)
dosyasında tutulur.

## Bilgisayarda ekranları ön izleme

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
