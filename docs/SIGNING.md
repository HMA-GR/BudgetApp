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

PowerShell'de şu komutu çalıştır. Dosya yolu tam yazıldığı için PowerShell'in
hangi klasörde açıldığı önemli değildir:

```powershell
keytool -genkeypair -keystore "$env:USERPROFILE\BudgetApp-release.jks" -storetype JKS -alias budgetapp -keyalg RSA -keysize 3072 -validity 10000 -dname "CN=BudgetApp"
```

Sorulduğunda güçlü bir parola belirle. Anahtar için ayrıca parola sorulursa
Enter'a basarak aynı parolayı kullanabilirsin. Oluşan `BudgetApp-release.jks`
dosyası Windows kullanıcı klasöründedir. PowerShell'de
`Test-Path "$env:USERPROFILE\BudgetApp-release.jks"` komutu `True` döndürmelidir.
Dosyayı ve kullandığın iki farklı parolayı güvenli iki yerde sakla. GitHub
sırlarına erişim de kaybolursa bu bilgiler olmadan aynı uygulamaya güncelleme
yayımlayamazsın. `budgetapp` anahtarın takma adıdır; değiştirmene gerek yok.

Parolayı yazarken ekranda karakter görünmemesi normaldir. Bu parola, uygulama
içindeki yedek dosyası parolasından farklı olabilir.
Komut sonunda JKS biçimiyle ilgili bir uyarı görebilirsin; dosya oluştuysa
bu uyarı işlemi durdurmaz. Uyarıdaki dönüştürme komutunu çalıştırman gerekmez.

## 2. GitHub sırlarını ekle

GitHub'da **BudgetApp > Settings > Secrets and variables > Actions > New
repository secret** yolunu aç. Dört sır ekle:

| Ad | Değer | Ne işe yarar? |
| --- | --- | --- |
| `BUDGET_KEYSTORE_B64` | JKS dosyasının Base64 kodlanmış içeriği | GitHub'a imzalama dosyasını aktarır. Base64 şifreleme değildir. |
| `BUDGET_KEYSTORE_PASSWORD` | Dosyanın parolası | GitHub'ın JKS dosyasını açmasını sağlar. |
| `BUDGET_KEY_ALIAS` | `budgetapp` | Dosyanın içindeki doğru anahtarı seçer. Parola değildir. |
| `BUDGET_KEY_PASSWORD` | Anahtarın parolası; aynı parola seçtiysen dosya parolası | Seçilen özel anahtarla APK'yı imzalar. |

Senin dosya parolan ile anahtar parolan farklıysa `BUDGET_KEYSTORE_PASSWORD`
alanına **ilk parolayı**, `BUDGET_KEY_PASSWORD` alanına **ikinci parolayı** yaz.

`BUDGET_KEYSTORE_B64` değerini üretmek için Windows PowerShell'de:

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("$env:USERPROFILE\BudgetApp-release.jks")) | Set-Clipboard
```

Komut sonucu panoya kopyalar; GitHub sırrına yapıştır. macOS'ta
`base64 -i BudgetApp-release.jks | pbcopy` kullanılabilir. Linux'ta
`base64 -w 0 BudgetApp-release.jks` çıktısı kopyalanabilir. Base64 bir
şifreleme değildir; bu metni yalnızca GitHub'ın **secret** alanına koy.
Windows'ta GitHub'a yapıştırdıktan sonra PowerShell'de
`Set-Clipboard -Value 'Temizlendi'` çalıştırarak panodaki mevcut metni
değiştirebilirsin. Bazı PowerShell sürümleri boş metni (`''`) kabul etmez.
Windows pano geçmişi açıksa (Win+V), Base64 kaydını oradan da sil.

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

## Bilgisayar değiştirirsen

Parolalar tek başına yeterli değildir: **aynı `BudgetApp-release.jks` dosyası**,
dosya parolası, anahtar parolası ve `budgetapp` takma adı birlikte gerekir.
GitHub deposu ve dört sırrı korunuyorsa yeni bilgisayardan GitHub iş akışını
çalıştırıp APK üretebilirsin. GitHub sırları silinirse veya başka bir depoya
geçersen sakladığın dosya ve iki parolayla sırları yeniden oluşturman gerekir.
Bu yüzden JKS dosyasının en az bir bağımsız kopyasını, örneğin güvenle saklanan
bir USB bellekte veya şifreli dosya kasasında tut; iki parolayı da güvenli
bir parola yöneticisinde sakla. GitHub sırları kaydedildikten sonra değerleri
arayüzden tekrar gösterilmez. Hem GitHub sırları hem de JKS kopyaları kaybolursa
aynı uygulama kurulumunu güncellemek mümkün olmaz. Yeni anahtarla APK üretmek
eski uygulamanın üzerine kurulum sağlamaz; telefondaki verileri korumak için
önce uygulama içinden şifreli veri yedeği almak gerekir.
Android'in mevcut kurulumun üzerine güncelleme yapabilmesi için uygulama
kimliği aynı kalmalı ve yeni APK'nın sürüm numarası daha yüksek olmalıdır.
