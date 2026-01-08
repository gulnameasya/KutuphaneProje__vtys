/* =========================================================
   UNIVERSITY LIBRARY DB (PostgreSQL) - SINGLE SCRIPT
   Fixes:
   - sp_KitapAra_Dinamik param-index bug fixed (WHERE static, ORDER BY dynamic)
   - sp_KitapTeslimAl cleaned (single read, null checks)
   Includes:
   - Tables, procedures, functions, triggers, views, seed
   ========================================================= */

BEGIN;

-- =====================
-- TABLES
-- =====================

CREATE TABLE IF NOT EXISTS ROL(
  RolID BIGSERIAL PRIMARY KEY,
  RolAdi TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS KULLANICI(
  KullaniciID BIGSERIAL PRIMARY KEY,
  KullaniciAdi TEXT NOT NULL UNIQUE,
  SifreHash TEXT NOT NULL,
  RolID BIGINT NOT NULL REFERENCES ROL(RolID),
  AdSoyad TEXT NOT NULL,
  AktifMi BOOLEAN NOT NULL DEFAULT TRUE,
  KayitTarihi TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS UYE(
  UyeID BIGSERIAL PRIMARY KEY,
  AdSoyad TEXT NOT NULL,
  Telefon TEXT NOT NULL,
  Email TEXT NOT NULL,
  ToplamBorc NUMERIC(12,2) NOT NULL DEFAULT 0,
  KayitTarihi TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS KITAP(
  KitapID BIGSERIAL PRIMARY KEY,
  KitapAdi TEXT NOT NULL,
  Yazar TEXT NOT NULL,
  Kategori TEXT NOT NULL,
  Yayinevi TEXT NOT NULL,
  BasimYili INT NOT NULL CHECK (BasimYili >= 0),
  ToplamAdet INT NOT NULL CHECK (ToplamAdet >= 0),
  MevcutAdet INT NOT NULL CHECK (MevcutAdet >= 0 AND MevcutAdet <= ToplamAdet),
  KayitTarihi TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS ODUNC(
  OduncID BIGSERIAL PRIMARY KEY,
  UyeID BIGINT NOT NULL REFERENCES UYE(UyeID),
  KitapID BIGINT NOT NULL REFERENCES KITAP(KitapID),
  IslemYapanKullaniciID BIGINT NOT NULL REFERENCES KULLANICI(KullaniciID),
  OduncTarihi DATE NOT NULL DEFAULT CURRENT_DATE,
  SonTeslimTarihi DATE NOT NULL,
  TeslimTarihi DATE NULL
);

-- Aynı üye aynı kitabı aynı anda 2 kez aktif alamaz
CREATE UNIQUE INDEX IF NOT EXISTS ux_odunc_active_member_book
ON ODUNC(UyeID, KitapID)
WHERE TeslimTarihi IS NULL;

CREATE TABLE IF NOT EXISTS CEZA(
  CezaID BIGSERIAL PRIMARY KEY,
  OduncID BIGINT NOT NULL UNIQUE REFERENCES ODUNC(OduncID),
  UyeID BIGINT NOT NULL REFERENCES UYE(UyeID),
  GecikmeGun INT NOT NULL CHECK (GecikmeGun >= 0),
  Tutar NUMERIC(12,2) NOT NULL CHECK (Tutar >= 0),
  OlusmaTarihi TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS LOG_ISLEM(
  LogID BIGSERIAL PRIMARY KEY,
  TabloAdi TEXT NOT NULL,
  IslemTuru TEXT NOT NULL,
  IslemZamani TIMESTAMP NOT NULL DEFAULT now(),
  Aciklama TEXT NOT NULL
);

-- =====================
-- STORED PROCEDURES / FUNCTIONS
-- =====================

/* 1) sp_YeniOduncVer
   - aktif ödünç sayısı kontrol (limit 5)
   - stok kontrol (FOR UPDATE)
   - ODUNC insert
   Not: stok azaltma + log TR_ODUNC_INSERT trigger’ında. */
CREATE OR REPLACE PROCEDURE sp_YeniOduncVer(
  IN pUyeID BIGINT,
  IN pKitapID BIGINT,
  IN pIslemYapanKullaniciID BIGINT
)
LANGUAGE plpgsql
AS $$
DECLARE
  vActiveCount INT;
  vMevcutAdet INT;
  vOduncTarihi DATE := CURRENT_DATE;
  vSonTeslim DATE := (CURRENT_DATE + INTERVAL '15 day')::DATE;
BEGIN
  IF pUyeID IS NULL OR pKitapID IS NULL OR pIslemYapanKullaniciID IS NULL THEN
    RAISE EXCEPTION 'Parametreler boş olamaz. UyeID=%, KitapID=%, KullaniciID=%',
      pUyeID, pKitapID, pIslemYapanKullaniciID;
  END IF;

  IF NOT EXISTS (SELECT 1 FROM UYE WHERE UyeID = pUyeID) THEN
    RAISE EXCEPTION 'Üye bulunamadı. UyeID=%', pUyeID;
  END IF;

  IF NOT EXISTS (SELECT 1 FROM KULLANICI WHERE KullaniciID=pIslemYapanKullaniciID AND AktifMi=true) THEN
    RAISE EXCEPTION 'İşlemi yapan kullanıcı bulunamadı veya pasif. KullaniciID=%', pIslemYapanKullaniciID;
  END IF;

  SELECT COUNT(*) INTO vActiveCount
  FROM ODUNC
  WHERE UyeID = pUyeID AND TeslimTarihi IS NULL;

  IF vActiveCount >= 5 THEN
    RAISE EXCEPTION 'Ödünç limiti aşıldı. Aktif ödünç=% (limit 5).', vActiveCount;
  END IF;

  SELECT MevcutAdet INTO vMevcutAdet
  FROM KITAP
  WHERE KitapID = pKitapID
  FOR UPDATE;

  IF vMevcutAdet IS NULL THEN
    RAISE EXCEPTION 'Kitap bulunamadı. KitapID=%', pKitapID;
  END IF;

  IF vMevcutAdet <= 0 THEN
    RAISE EXCEPTION 'Stok yok. KitapID=%', pKitapID;
  END IF;

  INSERT INTO ODUNC(UyeID, KitapID, IslemYapanKullaniciID, OduncTarihi, SonTeslimTarihi, TeslimTarihi)
  VALUES(pUyeID, pKitapID, pIslemYapanKullaniciID, vOduncTarihi, vSonTeslim, NULL);
END;
$$;


/* 2) sp_KitapTeslimAl (clean)
   - TeslimTarihi günceller
   - gecikme varsa CEZA insert (günlük 5 TL)
   Not: stok artırma + log TR_ODUNC_UPDATE_TESLIM trigger’ında
        borç artırma + log TR_CEZA_INSERT trigger’ında */
CREATE OR REPLACE PROCEDURE sp_KitapTeslimAl(
  IN pOduncID BIGINT,
  IN pTeslimTarihi DATE
)
LANGUAGE plpgsql
AS $$
DECLARE
  vSonTeslim DATE;
  vUyeID BIGINT;
  vTeslimTarihiMevcut DATE;
  vGecikme INT;
  vTutar NUMERIC(12,2);
  vGunlukCeza NUMERIC(12,2) := 5;
BEGIN
  IF pOduncID IS NULL THEN
    RAISE EXCEPTION 'OduncID boş olamaz.';
  END IF;

  IF pTeslimTarihi IS NULL THEN
    RAISE EXCEPTION 'TeslimTarihi boş olamaz.';
  END IF;

  SELECT UyeID, SonTeslimTarihi, TeslimTarihi
    INTO vUyeID, vSonTeslim, vTeslimTarihiMevcut
  FROM ODUNC
  WHERE OduncID = pOduncID
  FOR UPDATE;

  IF vUyeID IS NULL THEN
    RAISE EXCEPTION 'Ödünç kaydı bulunamadı. OduncID=%', pOduncID;
  END IF;

  IF vTeslimTarihiMevcut IS NOT NULL THEN
    RAISE EXCEPTION 'Bu ödünç zaten teslim alınmış. OduncID=%', pOduncID;
  END IF;

  UPDATE ODUNC
  SET TeslimTarihi = pTeslimTarihi
  WHERE OduncID = pOduncID;

  IF pTeslimTarihi > vSonTeslim THEN
    vGecikme := (pTeslimTarihi - vSonTeslim);
    vTutar := vGecikme * vGunlukCeza;

    INSERT INTO CEZA(OduncID, UyeID, GecikmeGun, Tutar)
    VALUES(pOduncID, vUyeID, vGecikme, vTutar);
  END IF;
END;
$$;


/* 3) sp_UyeOzetRapor */
CREATE OR REPLACE FUNCTION sp_UyeOzetRapor(pUyeID BIGINT)
RETURNS TABLE(
  UyeID BIGINT,
  ToplamAldigiKitapSayisi BIGINT,
  IadeEtmedigiKitapSayisi BIGINT,
  ToplamCezaTutari NUMERIC(12,2),
  GuncelToplamBorc NUMERIC(12,2)
)
LANGUAGE sql
AS $$
  SELECT
    u.UyeID,
    (SELECT COUNT(*) FROM ODUNC o WHERE o.UyeID=u.UyeID) AS ToplamAldigiKitapSayisi,
    (SELECT COUNT(*) FROM ODUNC o WHERE o.UyeID=u.UyeID AND o.TeslimTarihi IS NULL) AS IadeEtmedigiKitapSayisi,
    COALESCE((SELECT SUM(c.Tutar) FROM CEZA c WHERE c.UyeID=u.UyeID), 0) AS ToplamCezaTutari,
    u.ToplamBorc AS GuncelToplamBorc
  FROM UYE u
  WHERE u.UyeID = pUyeID;
$$;

-- =====================
-- TRIGGERS
-- =====================

/* TR_ODUNC_INSERT: stok azalt + log */
CREATE OR REPLACE FUNCTION fn_tr_odunc_insert()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
  UPDATE KITAP
  SET MevcutAdet = MevcutAdet - 1
  WHERE KitapID = NEW.KitapID;

  IF EXISTS (SELECT 1 FROM KITAP WHERE KitapID=NEW.KitapID AND MevcutAdet < 0) THEN
    RAISE EXCEPTION 'Stok negatif olamaz. KitapID=%', NEW.KitapID;
  END IF;

  INSERT INTO LOG_ISLEM(TabloAdi, IslemTuru, Aciklama)
  VALUES('ODUNC', 'INSERT', format('ODUNC eklendi: OduncID=%s, UyeID=%s, KitapID=%s', NEW.OduncID, NEW.UyeID, NEW.KitapID));

  RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS TR_ODUNC_INSERT ON ODUNC;
CREATE TRIGGER TR_ODUNC_INSERT
AFTER INSERT ON ODUNC
FOR EACH ROW
EXECUTE FUNCTION fn_tr_odunc_insert();


/* TR_ODUNC_UPDATE_TESLIM: teslim alınınca stok artır + log */
CREATE OR REPLACE FUNCTION fn_tr_odunc_update_teslim()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
  IF OLD.TeslimTarihi IS NULL AND NEW.TeslimTarihi IS NOT NULL THEN
    UPDATE KITAP
    SET MevcutAdet = MevcutAdet + 1
    WHERE KitapID = NEW.KitapID;

    IF EXISTS (SELECT 1 FROM KITAP WHERE KitapID=NEW.KitapID AND MevcutAdet > ToplamAdet) THEN
      RAISE EXCEPTION 'MevcutAdet, ToplamAdet’i geçemez. KitapID=%', NEW.KitapID;
    END IF;

    INSERT INTO LOG_ISLEM(TabloAdi, IslemTuru, Aciklama)
    VALUES('ODUNC', 'UPDATE', format('Teslim alındı: OduncID=%s, TeslimTarihi=%s', NEW.OduncID, NEW.TeslimTarihi));
  END IF;

  RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS TR_ODUNC_UPDATE_TESLIM ON ODUNC;
CREATE TRIGGER TR_ODUNC_UPDATE_TESLIM
AFTER UPDATE ON ODUNC
FOR EACH ROW
EXECUTE FUNCTION fn_tr_odunc_update_teslim();


/* TR_CEZA_INSERT: borç artır + log */
CREATE OR REPLACE FUNCTION fn_tr_ceza_insert()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
  UPDATE UYE
  SET ToplamBorc = ToplamBorc + NEW.Tutar
  WHERE UyeID = NEW.UyeID;

  INSERT INTO LOG_ISLEM(TabloAdi, IslemTuru, Aciklama)
  VALUES('CEZA', 'INSERT', format('Ceza eklendi: CezaID=%s, OduncID=%s, UyeID=%s, Tutar=%s', NEW.CezaID, NEW.OduncID, NEW.UyeID, NEW.Tutar));

  RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS TR_CEZA_INSERT ON CEZA;
CREATE TRIGGER TR_CEZA_INSERT
AFTER INSERT ON CEZA
FOR EACH ROW
EXECUTE FUNCTION fn_tr_ceza_insert();


/* TR_UYE_DELETE_BLOCK: borç/aktif ödünç varsa üye silinemez */
CREATE OR REPLACE FUNCTION fn_tr_uye_delete_block()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
  vActiveLoan INT;
BEGIN
  SELECT COUNT(*) INTO vActiveLoan
  FROM ODUNC
  WHERE UyeID=OLD.UyeID AND TeslimTarihi IS NULL;

  IF vActiveLoan > 0 THEN
    RAISE EXCEPTION 'Üye silinemez: aktif ödünç var. UyeID=%', OLD.UyeID;
  END IF;

  IF OLD.ToplamBorc > 0 THEN
    RAISE EXCEPTION 'Üye silinemez: borç var (ToplamBorc=%). UyeID=%', OLD.ToplamBorc, OLD.UyeID;
  END IF;

  RETURN OLD;
END;
$$;

DROP TRIGGER IF EXISTS TR_UYE_DELETE_BLOCK ON UYE;
CREATE TRIGGER TR_UYE_DELETE_BLOCK
BEFORE DELETE ON UYE
FOR EACH ROW
EXECUTE FUNCTION fn_tr_uye_delete_block();

-- =====================
-- DYNAMIC SEARCH (FIXED)
-- =====================

CREATE OR REPLACE FUNCTION sp_KitapAra_Dinamik(
  pKitapAdi TEXT DEFAULT NULL,
  pYazar TEXT DEFAULT NULL,
  pKategori TEXT DEFAULT NULL,
  pBasimYilMin INT DEFAULT NULL,
  pBasimYilMax INT DEFAULT NULL,
  pSadeceMevcut BOOLEAN DEFAULT FALSE,
  pSortColumn TEXT DEFAULT 'KitapAdi',
  pSortDir TEXT DEFAULT 'ASC'
)
RETURNS TABLE(
  KitapID BIGINT,
  KitapAdi TEXT,
  Yazar TEXT,
  Kategori TEXT,
  Yayinevi TEXT,
  BasimYili INT,
  ToplamAdet INT,
  MevcutAdet INT
)
LANGUAGE plpgsql
AS $$
DECLARE
  sql TEXT;
  order_col TEXT;
  order_dir TEXT;
BEGIN
  order_col := CASE lower(pSortColumn)
    WHEN 'kitapadi' THEN 'k.KitapAdi'
    WHEN 'yazar'    THEN 'k.Yazar'
    WHEN 'basimyili' THEN 'k.BasimYili'
    WHEN 'kategori'  THEN 'k.Kategori'
    ELSE 'k.KitapAdi'
  END;

  order_dir := CASE upper(pSortDir)
    WHEN 'DESC' THEN 'DESC'
    ELSE 'ASC'
  END;

  sql := format($q$
    SELECT k.KitapID, k.KitapAdi, k.Yazar, k.Kategori, k.Yayinevi, k.BasimYili, k.ToplamAdet, k.MevcutAdet
    FROM KITAP k
    WHERE
      ($1 IS NULL OR length(trim($1))=0 OR k.KitapAdi ILIKE '%%' || $1 || '%%')
      AND ($2 IS NULL OR length(trim($2))=0 OR k.Yazar ILIKE '%%' || $2 || '%%')
      AND ($3 IS NULL OR length(trim($3))=0 OR k.Kategori = $3)
      AND ($4 IS NULL OR k.BasimYili >= $4)
      AND ($5 IS NULL OR k.BasimYili <= $5)
      AND ($6 = FALSE OR k.MevcutAdet > 0)
    ORDER BY %s %s
  $q$, order_col, order_dir);

  RETURN QUERY EXECUTE sql
  USING pKitapAdi, pYazar, pKategori, pBasimYilMin, pBasimYilMax, pSadeceMevcut;
END;
$$;

-- =====================
-- VIEWS (Static Reports)
-- =====================

CREATE OR REPLACE VIEW vw_TarihBazliOduncRaporu AS
SELECT 
    o.OduncID,
    u.AdSoyad AS UyeAdi,
    k.KitapAdi,
    k.Kategori,
    o.OduncTarihi,
    o.SonTeslimTarihi,
    o.TeslimTarihi,
    CASE 
        WHEN o.TeslimTarihi IS NULL THEN 'Teslim Edilmedi'
        ELSE 'Teslim Edildi'
    END AS Durum
FROM ODUNC o
JOIN UYE u ON o.UyeID = u.UyeID
JOIN KITAP k ON o.KitapID = k.KitapID;

CREATE OR REPLACE VIEW vw_GecikenKitaplarRaporu AS
SELECT 
    u.AdSoyad AS UyeAdi,
    u.Telefon,
    k.KitapAdi,
    o.OduncTarihi,
    o.SonTeslimTarihi,
    (CURRENT_DATE - o.SonTeslimTarihi) AS GecikmeGunu
FROM ODUNC o
JOIN UYE u ON o.UyeID = u.UyeID
JOIN KITAP k ON o.KitapID = k.KitapID
WHERE o.TeslimTarihi IS NULL AND o.SonTeslimTarihi < CURRENT_DATE;

CREATE OR REPLACE VIEW vw_EnCokOduncAlinanKitaplar AS
SELECT 
    k.KitapID,
    k.KitapAdi,
    k.Yazar,
    k.Kategori,
    COUNT(o.OduncID) AS OduncSayisi
FROM KITAP k
LEFT JOIN ODUNC o ON k.KitapID = o.KitapID
GROUP BY k.KitapID, k.KitapAdi, k.Yazar, k.Kategori
ORDER BY OduncSayisi DESC;

-- =====================
-- SEED (minimal)
-- =====================

INSERT INTO ROL(RolAdi) VALUES ('ADMIN') ON CONFLICT DO NOTHING;
INSERT INTO ROL(RolAdi) VALUES ('GOREVLI') ON CONFLICT DO NOTHING;

INSERT INTO KULLANICI(KullaniciAdi, SifreHash, RolID, AdSoyad, AktifMi)
VALUES (
  'admin',
  'x',
  (SELECT RolID FROM ROL WHERE RolAdi='ADMIN'),
  'Sistem Admin',
  TRUE
)
ON CONFLICT (KullaniciAdi) DO NOTHING;

-- En az 1 üye + 2 kitap (tekrar çalıştırınca kopya basmasın diye basit kontrol)
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM UYE) THEN
    INSERT INTO UYE (AdSoyad, Telefon, Email)
    VALUES
      ('Ahmet Yılmaz','05321112233','ahmet@mail.com'),
      ('Ayşe Demir','05332223344','ayse@mail.com'),
      ('Mehmet Kaya','05343334455','mehmet@mail.com'),
      ('Elif Şahin','05354445566','elif@mail.com'),
      ('Can Aksoy','05365556677','can@mail.com'),
      ('Zeynep Arslan','05376667788','zeynep@mail.com'),
      ('Murat Çelik','05387778899','murat@mail.com'),
      ('Selin Koç','05398889900','selin@mail.com'),
      ('Burak Aydın','05319990011','burak@mail.com'),
      ('Derya Polat','05301234567','derya@mail.com');
  END IF;

  IF NOT EXISTS (SELECT 1 FROM KITAP) THEN
    INSERT INTO KITAP
      (KitapAdi, Yazar, Kategori, Yayinevi, BasimYili, ToplamAdet, MevcutAdet)
    VALUES
      ('Suç ve Ceza','Dostoyevski','Roman','İş Bankası',2019,5,5),
      ('Sefiller','Victor Hugo','Roman','Can',2018,4,4),
      ('1984','George Orwell','Distopya','Can',2021,6,6),
      ('Hayvan Çiftliği','George Orwell','Distopya','Can',2020,7,7),
      ('Kürk Mantolu Madonna','Sabahattin Ali','Roman','YKY',2017,5,5),
      ('Tutunamayanlar','Oğuz Atay','Roman','İletişim',2016,4,4),
      ('Beyaz Diş','Jack London','Roman','Can',2015,3,3),
      ('Martin Eden','Jack London','Roman','Can',2018,5,5),
      ('Simyacı','Paulo Coelho','Roman','Can',2019,6,6),
      ('Uçurtma Avcısı','Khaled Hosseini','Roman','Everest',2020,4,4),

      ('Fareler ve İnsanlar','John Steinbeck','Roman','Can',2017,5,5),
      ('Şeker Portakalı','Vasconcelos','Roman','Can',2016,6,6),
      ('Yabancı','Albert Camus','Roman','Can',2018,4,4),
      ('Dönüşüm','Kafka','Roman','İş Bankası',2019,3,3),
      ('Olasılıksız','Adam Fawer','Bilim','April',2020,5,5),

      ('Sapiens','Yuval Noah Harari','Tarih','Kolektif',2021,6,6),
      ('Homo Deus','Yuval Noah Harari','Tarih','Kolektif',2022,4,4),
      ('İnsan Ne ile Yaşar','Tolstoy','Klasik','İş Bankası',2015,3,3),
      ('Anna Karenina','Tolstoy','Roman','İş Bankası',2018,5,5),
      ('Savaş ve Barış','Tolstoy','Roman','İş Bankası',2017,4,4),

      ('Karamazov Kardeşler','Dostoyevski','Roman','Can',2016,6,6),
      ('Yeraltından Notlar','Dostoyevski','Roman','Can',2019,3,3),
      ('Cesur Yeni Dünya','Aldous Huxley','Distopya','Can',2021,4,4),
      ('Fahrenheit 451','Ray Bradbury','Distopya','Can',2020,5,5),
      ('Saatleri Ayarlama Enstitüsü','Tanpınar','Roman','Dergah',2018,4,4),

      ('İnce Memed','Yaşar Kemal','Roman','YKY',2017,6,6),
      ('Kuyucaklı Yusuf','Sabahattin Ali','Roman','YKY',2016,5,5),
      ('Aylak Adam','Yusuf Atılgan','Roman','İletişim',2019,3,3),
      ('Serenad','Zülfü Livaneli','Roman','Doğan',2020,4,4),
      ('Çalıkuşu','Reşat Nuri','Roman','İnkılap',2015,6,6),

      ('Bülbülü Öldürmek','Harper Lee','Roman','Sel',2018,5,5),
      ('Lolita','Nabokov','Roman','İletişim',2019,3,3),
      ('Don Kişot','Cervantes','Klasik','İş Bankası',2016,4,4),
      ('İlahi Komedya','Dante','Klasik','YKY',2014,2,2),
      ('Od','İskender Pala','Roman','Kapı',2021,3,3),

      ('Simyacı 2','Paulo Coelho','Roman','Can',2022,4,4),
      ('Zamanın Kısa Tarihi','Stephen Hawking','Bilim','Alfa',2020,3,3),
      ('Kozmos','Carl Sagan','Bilim','Alfa',2019,4,4),
      ('Gen Bencildir','Richard Dawkins','Bilim','Alfa',2018,3,3),
      ('İnsanın Anlam Arayışı','Viktor Frankl','Psikoloji','Okuyan Us',2021,5,5);
  END IF;
END $$;
COMMIT;