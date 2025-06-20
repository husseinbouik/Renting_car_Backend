package Car.project.Entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import Car.project.util.EncryptionUtil;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 500)
    private String cname;

    @Column(length = 1000)
    private String adresse;

    @Column(length = 100)
    private String nationalite;

    @Column(length = 1000)
    private String adresseEtranger;

    @Column(length = 1000)
    private String passeport;

    private String delivreLePasseport;

    @Column(length = 1000)
    private String cin;

    private String CinDelivreLe;

    @Column(length = 1000)
    private String tel;

    @Column(length = 1000)
    private String permis;

    private String PermisDelivreLe;
    private String PermisDelivreAu;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Lob
    private byte[] photoCIN;

    @Lob
    private byte[] photoPermis;

    @PrePersist
    @PreUpdate
    private void encryptData() {
        try {
            this.cname = this.cname != null ? EncryptionUtil.encrypt(this.cname) : null;
            this.adresse = this.adresse != null ? EncryptionUtil.encrypt(this.adresse) : null;
            this.adresseEtranger = this.adresseEtranger != null ? EncryptionUtil.encrypt(this.adresseEtranger) : null;
            this.passeport = this.passeport != null ? EncryptionUtil.encrypt(this.passeport) : null;
            this.cin = this.cin != null ? EncryptionUtil.encrypt(this.cin) : null;
            this.tel = this.tel != null ? EncryptionUtil.encrypt(this.tel) : null;
            this.permis = this.permis != null ? EncryptionUtil.encrypt(this.permis) : null;
            this.photoCIN = this.photoCIN != null ? EncryptionUtil.encryptBytes(this.photoCIN) : null;
            this.photoPermis = this.photoPermis != null ? EncryptionUtil.encryptBytes(this.photoPermis) : null;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du chiffrement des données", e);
        }
    }

    @PostLoad
    	private void decryptData() {
    	    try {
    	        this.cname = this.cname != null ? EncryptionUtil.decrypt(this.cname) : null;
    	        this.adresse = this.adresse != null ? EncryptionUtil.decrypt(this.adresse) : null;
    	        this.adresseEtranger = this.adresseEtranger != null ? EncryptionUtil.decrypt(this.adresseEtranger) : null;
    	        this.passeport = this.passeport != null ? EncryptionUtil.decrypt(this.passeport) : null;
    	        this.cin = this.cin != null ? EncryptionUtil.decrypt(this.cin) : null;
    	        this.tel = this.tel != null ? EncryptionUtil.decrypt(this.tel) : null;
    	        this.permis = this.permis != null ? EncryptionUtil.decrypt(this.permis) : null;
    	        this.photoCIN = this.photoCIN != null ? EncryptionUtil.decryptBytes(this.photoCIN) : null;
    	        this.photoPermis = this.photoPermis != null ? EncryptionUtil.decryptBytes(this.photoPermis) : null;

    	    } catch (Exception e) {
    	        throw new RuntimeException("Erreur lors du déchiffrement des données", e);
    	    }
    	}

    public Client orElseThrow(Object object) {
        return null;
    }
}
