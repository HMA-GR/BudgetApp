# BudgetApp yol haritası

Bu dosya projenin kalıcı planı ve ilerleme kaydıdır. Çalışmaya başlamadan önce
`AGENTS.md` ile birlikte okunur; tamamlanan her aşamada durum ve doğrulama
sonucu burada güncellenir. GitHub deposundaki kod ve plan kişisel bütçe verisi
içermez.

## Hedef

Hesap açmadan ve internet bağlantısı olmadan çalışan bir Android bütçe uygulaması.
Veriler yalnızca telefonda tutulur. Kullanıcı tek bir yedek dosyasını başka bir
telefona taşıyıp uygulamadan geri yükleyebilir. Bilgisayarda ekranlar GitHub'da
tarayıcıdan görüntülenebilir; Android Studio Compose Preview isteğe bağlıdır.
Uygulamanın gerçek davranışı Android telefonda test edilir. Web uygulaması ve
emülatör hedeflenmez.

## Aşamalar

| No | Durum | İş | Bitti sayılma koşulu |
| --- | --- | --- | --- |
| 0 | Tamamlandı | Kararlar ve proje iskeleti | `com.hmagr.budgetapp`, Android 11 ve üzeri; debug APK başarıyla derlendi. |
| 1 | Tamamlandı | İlk ekran ve bilgisayar ön izlemesi | İki Compose ekran görüntüsü GitHub'da tarayıcıdan açılıyor; kullanıcı ekran yerleşimine geri bildirim verdi. |
| 2 | Devam ediyor | Aylık bütçe, ay seçimi, gelir/yatırım/gider bölümleri | Tutarlar kuruş cinsinden doğru hesaplanıyor; ilgili ekranlar çalışıyor. |
| 3 | Kodlandı, telefon doğrulaması bekliyor | Telefonda kalıcı kayıt | SQLite kayıtları uygulama yeniden açıldığında duruyor. |
| 4 | Kodlandı, telefon doğrulaması bekliyor | Tek dosya yedek ve geri yükleme | Dosya doğrulanıyor; hatalı dosya veriyi değiştirmiyor; sağlam dosya temiz kurulumda kayıtları geri getiriyor. |
| 5 | Kodlandı, GitHub doğrulaması bekliyor | GitHub otomasyonu ve imzalı APK | GitHub Actions test ve derlemeyi çalıştırıyor; kullanıcının gizli anahtarıyla güncellenebilir APK üretiyor. |
| 6 | Bekliyor | Gerçek telefon doğrulaması ve tasarım düzeltmeleri | Kullanıcı telefonda ve Compose Preview'da inceleyip onaylıyor. |

## Durum kaydı

- 2026-10-11: Depoda yalnızca `README.md` ve ilk Git commit'i vardı.
- 2026-10-11: Yol haritası depoya eklendi. Aşama 0 başladı.
- 2026-10-11: GitHub'da `codex/android-setup` dalına ilk plan commit'i
  gönderildi. Bulut ortamına Android SDK 35 ve Gradle 8.13 doğrulanmış
  indirmelerle kuruldu. Uygulama kodu henüz eklenmedi.
- 2026-10-11: Kullanıcı telefonunun Samsung S22 ve Android 16 olduğunu
  bildirdi. Gradle çalıştırıcısı oluşturuldu.
- 2026-10-11: Android SDK 36 da kuruldu. Android Gradle eklentisi 8.13.2 ve
  Kotlin eklentileri 2.2.20 indirildi; `./gradlew help` başarıyla tamamlandı.
  Bu, henüz APK derlemesi veya ekran ön izlemesi değildir.
- 2026-10-11: Kullanıcı kalıcı uygulama kimliğini `com.hmagr.budgetapp`, en
  eski desteklenen sistemi Android 11 (API 30) olarak seçti.
- 2026-10-11: İlk Android uygulaması ve örnek/boş durum için iki Compose
  Preview eklendi. `:app:assembleDebug` başarılı; APK kimliği, en eski sistem
  sürümü ve imzası doğrulandı. `:app:lintDebug` başarılı; Android 12+ sistem
  yedekleme kuralı ve geçici uygulama simgesi hakkında iki uyarı var.
- 2026-10-11: Kullanıcının tarayıcıdan daha kolay ön izleme isteği üzerine
  Compose ekran görüntüsü testi eklendi. İki ekran gerçek Compose kodundan
  üretildi ve görsel olarak kontrol edildi. `:app:validateDebugScreenshotTest`
  ve eklenti eklendikten sonraki `:app:assembleDebug` başarılı. GitHub
  README'sinde görseller açılabilir.
- 2026-10-12: Kullanıcı ilk görseli inceledi. Kalan üstte, Gelir ve Gider altta;
  altında kaydırmalı Gelir/Yatırım/Gider bölümleri, en altta üç ekleme eylemi
  ve geçmiş ay seçimi istedi. Görsel ince ayar sona bırakıldı. Aşama 1 bitti,
  aşama 2 başladı. Kalan hesabı ve yatırım kaydının kapsamı soruldu.
- 2026-10-12: Kullanıcı Kalan = Gelir − Gider − yatırıma ayrılan tutar
  hesabını; yatırım için ay başına tek toplam güncel değer kaydını seçti.
  Güncel değer Kalan hesabına katılmıyor. Otomatik Android yedekleme ve
  cihazdan cihaza aktarımın kapalı kalmasını seçti.
- 2026-10-12: Geçmiş ay seçicisi, kaydırılabilir üç bölüm ve üç ekleme
  penceresi kodlandı. Gelir/gider/yatırım kayıtları ile yatırımın aylık
  güncel değeri uygulamaya özel SQLite dosyasına yazılıyor. Tutar girişleri
  kuruş cinsinden tutuluyor. Hesaplama için 2 JVM testi, Compose ekran
  görüntüsü doğrulaması, debug APK derlemesi ve lint başarılı. Veritabanının
  telefon yeniden başlatma/uygulama açma davranışı henüz gerçek cihazda
  sınanmadı. Dört ekran görüntüsü görsel olarak incelendi.
- 2026-10-12: Kullanıcı tek dosya yedeğin parolayla şifrelenmesini ve
  doğrulanan dosyanın geri yüklemede mevcut kayıtların tamamının yerini
  almasını seçti. Aşama 4 kodlaması başladı.
- 2026-10-12: AES-GCM ve paroladan türetilmiş anahtarla şifreli tek dosya,
  Android dosya seçicisiyle dışa/içe aktarma ve doğrulama sonrası tek
  veritabanı işlemi içinde tam değiştirme kodlandı. Yanlış parola ve
  değiştirilmiş dosyanın reddi dahil toplam 4 JVM testi; Compose ekran
  görüntüsü doğrulaması, debug APK ve lint başarılı. Dosya seçicisi ve
  veritabanı geri yüklemesi henüz telefonda sınanmadı. Dört tarayıcı
  ön izlemesi tekrar üretildi ve görsel olarak kontrol edildi.
- 2026-10-12: Kullanıcı güncellenebilir APK'yı seçti. GitHub Actions kontrol
  iş akışı ve gizli anahtarla imzalı APK iş akışı eklendi; imza anahtarını
  kullanıcının bilgisayarında oluşturması ve dört GitHub sırrına eklemesi
  için yönerge yazıldı. Geçici bir test anahtarıyla release APK derlendi ve
  Android v2 imzası doğrulandı; test anahtarı depoya konmadı. Gerçek imzalı
  iş akışı GitHub'da henüz koşulmadı.
- 2026-10-12: Bulut ortamında yeniden çalışma için JDK/SDK yollarını ve
  doğrulama komutlarını içeren başlangıç yönergesi taslağı kaydedildi;
  yayımlama kullanıcı ortam ayarlarında yapılacak.
- Sıradaki iş: Kullanıcının GitHub imza sırlarını eklemesinden sonra iş
  akışını ve telefonda kayıt/yedek/geri yüklemeyi doğrula. Kayıt
  düzenleme/silme ve kategori ihtiyaçlarını da netleştir.

## Karar bekleyen konular

- Kalıcı uygulama kimliği ve en eski Android sürümü: kullanıcı tarafından
  `com.hmagr.budgetapp` ve Android 11 olarak kararlaştırıldı.
- Ekran tasarımı ve etkileşimler: ilk Compose Preview hazır olunca kullanıcıyla
  birlikte netleştirilecek; görsel ince ayar sona bırakıldı, nihai simge de
  buna dahil.
- Kalan hesabı, yatırımın aylık toplam güncel değeri ve Android otomatik
  yedeklemesinin kapatılması: kullanıcı tarafından kararlaştırıldı.
- Yedek dosyasının parola koruması ve içe aktarmanın mevcut veriye etkisi:
  parolayla şifreleme ve doğrulanmış tam değiştirme kararlaştırıldı.
- Güncellenebilir APK: kullanıcı onayladı; özel anahtarın GitHub sırlarına
  eklenmesi bekleniyor.
- Gelir ve gider için kategorilerin kapsamı: kullanıcı yanıtı bekleniyor.

## Teknik yön

- Android: Kotlin ve Jetpack Compose.
- Yerel veri: SQLite; parasal tutarlar kuruş cinsinden tam sayı.
- Yedek: sürüm bilgisi bulunan tek dosya; Android dosya seçicisiyle dışa/içe
  aktarma; içe aktarma öncesi doğrulama ve bütünlüklü geri yükleme.
- GitHub: kaynak kodu ve plan; ileride test/derleme ve indirilebilir APK.
- Uygulama hesabı, sunucu, Firebase, otomatik bulut eşitleme yok.

## Karar ve çalışma kuralı

Kullanıcı önemli kararların kendisine sorulmasını istedi. Uygulama kimliği,
desteklenecek cihazlar, veri kaybı riski olan geri yükleme davranışı, yedek
güvenliği ve kullanıcı arayüzünün nihai tasarımı onay alınmadan kesinleştirilmez.
İlgili aşamaya gelene kadar bağımsız, geri alınabilir işlere devam edilir.
