# Güncellenebilir APK için imza anahtarı

Android, bir uygulamanın yeni sürümünü eskisinin üzerine kurmak için her
sürümün **aynı özel anahtarla** imzalanmasını ister. Bu anahtar küçük bir
dosyadır; telefondaki bütçe kayıtlarını şifrelemek için kullanılan yedek
parolasından ayrıdır. GitHub iş akışı hazırdır ama anahtar yalnızca sizde
olmalı. GitHub **repository secret** alanları, anahtar dosyasını ve parolaları
kodda göstermeden derleme sırasında kullanır. Anahtarı veya parolaları Git
deposuna ya da bu sohbete göndermeyin.

## 1. Bilgisayarında anahtarı oluştur

Ücretsiz Java JDK'nin içindeki `keytool` aracı yeterlidir; Android Studio ve
emülatör gerekmez. **Windows'ta** Başlat menüsünden PowerShell'i aç ve
`keytool -help` yaz. Komut bulunamazsa [Eclipse Temurin JDK 21'in Windows
kurucusunu](https://adoptium.net/temurin/releases/?version=21) indirip kur.
Kurucuda komutları PATH'e ekleme seçeneği varsa etkinleştir. PowerShell'i
kapatıp yeniden aç; `keytool -help` komutunu tekrar dene.

Dosya Gezgini'nde **Belgeler** içinde `BudgetApp-imza` adlı bir klasör oluştur.
Bu klasörü aç, üstteki adres çubuğuna `powershell` yazıp Enter'a bas. Açılan
PowerShell penceresinde şu komutu çalıştır:

```powershell
keytool -genkeypair -keystore BudgetApp-release.jks -storetype JKS -alias budgetapp -keyalg RSA -keysize 3072 -validity 10000 -dname "CN=BudgetApp"
```

Sorulduğunda güçlü bir parola belirle. Anahtar için ayrıca parola sorulursa
Enter'a basarak aynı parolayı kullanabilirsin. Oluşan `BudgetApp-release.jks`
dosyasını ve parolayı güvenli iki yerde sakla. Dosya veya parola kaybolursa
aynı uygulamaya güncelleme yayımlayamazsın. `budgetapp` burada anahtarın
takma adıdır; değiştirmene gerek yok.

Parolayı yazarken ekranda karakter görünmemesi normaldir. Bu parola, uygulama
içindeki yedek dosyası parolasından farklı olabilir.
Komut sonunda JKS biçimiyle ilgili bir uyarı görebilirsin; dosya oluştuysa
bu uyarı işlemi durdurmaz.

## 2. GitHub sırlarını ekle

GitHub'da **BudgetApp > Settings > Secrets and variables > Actions > New
repository secret** yolunu aç. Dört sır ekle:

| Ad | Değer |
| --- | --- |
| `BUDGET_KEYSTORE_B64` | JKS dosyasının Base64 kodlanmış içeriği |
| `BUDGET_KEYSTORE_PASSWORD` | Dosyanın parolası |
| `BUDGET_KEY_ALIAS` | `budgetapp` |
| `BUDGET_KEY_PASSWORD` | Anahtarın parolası; aynı parola seçtiysen dosya parolası |

`BUDGET_KEYSTORE_B64` değerini üretmek için Windows PowerShell'de, JKS
dosyasının bulunduğu klasörde:

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("BudgetApp-release.jks")) | Set-Clipboard
```

Komut sonucu panoya kopyalar; GitHub sırrına yapıştır. macOS'ta
`base64 -i BudgetApp-release.jks | pbcopy` kullanılabilir. Linux'ta
`base64 -w 0 BudgetApp-release.jks` çıktısı kopyalanabilir. Base64 bir
şifreleme değildir; bu metni yalnızca GitHub'ın **secret** alanına koy.
Windows'ta GitHub'a yapıştırdıktan sonra PowerShell'de
`Set-Clipboard -Value ''` çalıştırarak panoyu temizleyebilirsin.

## 3. APK'yı indir

**Actions > Signed Android APK** iş akışını aç. Yeni dalda kod gönderilince
otomatik başlar. Sırlar eklenmeden başlayan başarısız çalışmayı **Re-run**
ile yeniden başlatabilirsin. İşlem başarılı olduğunda çalışmanın
**Artifacts** bölümünden `BudgetApp-signed-apk` dosyasını indir. ZIP
içindeki `app-release.apk` telefonuna kurulacak dosyadır.

İlk imzalı APK, geliştirme amaçlı debug APK ile farklı anahtar kullanır.
Debug APK kuruluysa önce uygulamada şifreli yedek oluştur, debug APK'yı
kaldır, sonra imzalı APK'yı yükleyip yedeği geri al. Bundan sonraki
imzalı sürümler aynı anahtarla üretildiğinde eski sürümün üzerine kurulur.
İş akışı her çalışmada Android sürüm numarasını artırır.
