package com.nexis.chatapp.Model;

public class MessageRequest {
    private String kanalId;
    private String kullaniciId;
    private String kullaniciIsim;
    private String kullaniciProfil;

    public MessageRequest(String kanalId, String kullaniciId, String kullaniciIsim,String kullaniciProfil) {
        this.kanalId = kanalId;
        this.kullaniciId = kullaniciId;
        this.kullaniciIsim = kullaniciIsim;
        this.kullaniciProfil=kullaniciProfil;
    }

    public MessageRequest() {
    }

    public String getKullaniciProfil() {
        return kullaniciProfil;
    }

    public void setKullaniciProfil(String kullaniciProfil) {
        this.kullaniciProfil = kullaniciProfil;
    }

    public String getKullaniciIsim() {
        return kullaniciIsim;
    }

    public void setKullaniciIsim(String kullaniciIsim) {
        this.kullaniciIsim = kullaniciIsim;
    }

    public String getKanalId() {
        return kanalId;
    }

    public void setKanalId(String kanalId) {
        this.kanalId = kanalId;
    }

    public String getKullaniciId() {
        return kullaniciId;
    }

    public void setKullaniciId(String kullaniciId) {
        this.kullaniciId = kullaniciId;
    }
}
