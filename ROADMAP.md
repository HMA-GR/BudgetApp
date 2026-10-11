# BudgetApp yol haritası

Bu dosya projenin kalıcı planı ve ilerleme kaydıdır. Çalışmaya başlamadan önce
`AGENTS.md` ile birlikte okunur; tamamlanan her aşamada durum ve doğrulama
sonucu burada güncellenir. GitHub deposundaki kod ve plan kişisel bütçe verisi
içermez.

## Hedef

Hesap açmadan ve internet bağlantısı olmadan çalışan bir Android bütçe uygulaması.
Veriler yalnızca telefonda tutulur. Kullanıcı tek bir yedek dosyasını başka bir
telefona taşıyıp uygulamadan geri yükleyebilir. Bilgisayarda ekranlar Android
Studio Compose Preview ile, uygulamanın gerçek davranışı Android telefonda test
edilir. Web uygulaması ve emülatör hedeflenmez.

## Aşamalar

| No | Durum | İş | Bitti sayılma koşulu |
| --- | --- | --- | --- |
| 0 | Devam ediyor | Kararlar ve proje iskeleti | Uygulama kimliği ve desteklenecek Android sürümü kullanıcıyla kararlaştırıldı; Android projesi derleniyor. |
| 1 | Bekliyor | İlk ekran ve bilgisayar ön izlemesi | Ana ekran Android Studio Compose Preview içinde açılıyor; kullanıcı tasarımı inceleyebiliyor. |
| 2 | Bekliyor | Aylık bütçe, kategoriler ve gelir/gider | Tutarlar kuruş cinsinden doğru hesaplanıyor; ilgili ekranlar çalışıyor. |
| 3 | Bekliyor | Telefonda kalıcı kayıt | Room/SQLite kayıtları uygulama yeniden açıldığında duruyor. |
| 4 | Bekliyor | Tek dosya yedek ve geri yükleme | Dosya doğrulanıyor; hatalı dosya veriyi değiştirmiyor; sağlam dosya temiz kurulumda kayıtları geri getiriyor. |
| 5 | Bekliyor | GitHub otomasyonu ve APK | GitHub Actions test ve derlemeyi çalıştırıyor; indirilebilir debug APK üretiyor. |
| 6 | Bekliyor | Gerçek telefon doğrulaması ve tasarım düzeltmeleri | Kullanıcı telefonda ve Compose Preview'da inceleyip onaylıyor. |

## Durum kaydı

- 2026-10-11: Depoda yalnızca `README.md` ve ilk Git commit'i vardı.
- 2026-10-11: Yol haritası depoya eklendi. Aşama 0 başladı.
- 2026-10-11: GitHub'da `codex/android-setup` dalına ilk plan commit'i
  gönderildi. Bulut ortamına Android SDK 35 ve Gradle 8.13 doğrulanmış
  indirmelerle kuruldu. Uygulama kodu henüz eklenmedi.
- 2026-10-11: Kullanıcı telefonunun Samsung S22 ve Android 16 olduğunu
  bildirdi. Gradle çalıştırıcısı oluşturuldu; APK kimliği ve desteklenecek
  en eski Android sürümü için kullanıcı kararı bekleniyor.
- Sıradaki iş: Bu iki karardan sonra Android uygulama modülünü oluşturup
  derlemek.

## Karar bekleyen konular

- Kalıcı uygulama kimliği: kullanıcı yanıtı bekleniyor. Bu kimlik APK
  güncellemelerinde ve telefondaki verilerin korunmasında önemlidir.
- En eski desteklenen Android sürümü: Samsung S22 / Android 16 bilgisi alındı;
  daha eski telefonların desteklenip desteklenmeyeceği kullanıcı yanıtını
  bekliyor.
- Ekran tasarımı ve etkileşimler: ilk Compose Preview hazır olunca kullanıcıyla
  birlikte netleştirilecek.
- Yedek dosyasının parola koruması ve içe aktarmanın mevcut veriye etkisi:
  yedek aşamasına geçmeden önce kullanıcıyla kararlaştırılacak.

## Teknik yön

- Android: Kotlin ve Jetpack Compose.
- Yerel veri: Room/SQLite; parasal tutarlar kuruş cinsinden tam sayı.
- Yedek: sürüm bilgisi bulunan tek dosya; Android dosya seçicisiyle dışa/içe
  aktarma; içe aktarma öncesi doğrulama ve bütünlüklü geri yükleme.
- GitHub: kaynak kodu ve plan; ileride test/derleme ve indirilebilir APK.
- Uygulama hesabı, sunucu, Firebase, otomatik bulut eşitleme yok.

## Karar ve çalışma kuralı

Kullanıcı önemli kararların kendisine sorulmasını istedi. Uygulama kimliği,
desteklenecek cihazlar, veri kaybı riski olan geri yükleme davranışı, yedek
güvenliği ve kullanıcı arayüzünün nihai tasarımı onay alınmadan kesinleştirilmez.
İlgili aşamaya gelene kadar bağımsız, geri alınabilir işlere devam edilir.
