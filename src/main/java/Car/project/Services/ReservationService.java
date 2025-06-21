package Car.project.Services;

import Car.project.Entities.Client;
import Car.project.Entities.Reservation;
import Car.project.Entities.Voiture;
import Car.project.Repositories.ClientRepository;
import Car.project.Repositories.ReservationRepository;
import Car.project.Repositories.VoitureRepository;
import Car.project.dto.ClientDetailDTO;
import Car.project.dto.ReservationRequestDTO;
import Car.project.dto.ReservationResponseDTO;
import Car.project.dto.VoitureDetailDTO;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service pour la gestion des réservations.
 * Gère la logique métier, la validation et l'interaction avec la base de données.
 */
@Service
@Transactional
public class ReservationService {

    // --- Dépendances ---
    private final ReservationRepository reservationRepository;
    private final VoitureRepository voitureRepository;
    private final ClientRepository clientRepository;

    @Autowired
    public ReservationService(ReservationRepository reservationRepository, VoitureRepository voitureRepository, ClientRepository clientRepository) {
        this.reservationRepository = reservationRepository;
        this.voitureRepository = voitureRepository;
        this.clientRepository = clientRepository;
    }

    // --- API Publique (CRUD) ---

    /**
     * Crée une nouvelle réservation en appliquant les règles métier.
     * @param dto Les données de la réservation à créer.
     * @return Le DTO de la réservation créée.
     */
    public ReservationResponseDTO createReservation(ReservationRequestDTO dto) {
        // Règle 1 : Valider les dates
        if (dto.getDateDebut().isAfter(dto.getDateFin())) {
            throw new IllegalArgumentException("La date de début ne peut pas être après la date de fin.");
        }

        // Règle 2 : Vérifier la disponibilité de la voiture
        if (!isVoitureDisponible(dto.getVoitureId(), dto.getDateDebut(), dto.getDateFin())) {
            throw new IllegalStateException("La voiture n'est pas disponible pour les dates sélectionnées.");
        }

        // Récupérer les entités requises
        Voiture voiture = findVoitureEntityById(dto.getVoitureId());
        Client client = findClientEntityById(dto.getClientId());
        
        Client conducteurSecondaire = null;
        if (dto.getConducteurSecondaireId() != null) {
            conducteurSecondaire = findClientEntityById(dto.getConducteurSecondaireId());
        }

        // Règle 3 : Calculer le prix total
        long nombreJours = ChronoUnit.DAYS.between(dto.getDateDebut(), dto.getDateFin());
        if (nombreJours == 0) nombreJours = 1; // Facturation minimale d'un jour
        float montantTotal = (float) (nombreJours * voiture.getPrixDeBase());

        // Création de l'entité Reservation
        Reservation reservation = new Reservation();
        reservation.setVoiture(voiture);
        reservation.setClient(client);
        reservation.setConducteurSecondaire(conducteurSecondaire);
        reservation.setDateDebut(dto.getDateDebut());
        reservation.setDateFin(dto.getDateFin());
        reservation.setAcompte(dto.getAcompte());
        reservation.setStatut(dto.getStatut() != null ? dto.getStatut() : "Confirmée");
        reservation.setMontantTotal(montantTotal);

        Reservation savedReservation = reservationRepository.save(reservation);
        return mapToReservationResponseDTO(savedReservation);
    }

    /**
     * Met à jour une réservation existante.
     * Note : Des logiques plus complexes comme le recalcul du prix peuvent être ajoutées ici.
     * @param reservationId L'ID de la réservation à mettre à jour.
     * @param dto Les nouvelles données pour la réservation.
     * @return Le DTO de la réservation mise à jour.
     */
    public ReservationResponseDTO updateReservation(Long reservationId, ReservationRequestDTO dto) {
        Reservation reservation = findReservationEntityById(reservationId);
        
        // Logique de sécurité potentielle (ex: vérifier que l'utilisateur connecté est le propriétaire)

        reservation.setDateDebut(dto.getDateDebut());
        reservation.setDateFin(dto.getDateFin());
        reservation.setAcompte(dto.getAcompte());
        reservation.setStatut(dto.getStatut());

        Reservation saved = reservationRepository.save(reservation);
        return mapToReservationResponseDTO(saved);
    }
    
    /**
     * Supprime une réservation par son ID.
     * @param id L'ID de la réservation à supprimer.
     */
    public void deleteReservation(Long id) {
        if (!reservationRepository.existsById(id)) {
            throw new EntityNotFoundException("Impossible de trouver la réservation avec l'ID : " + id);
        }
        reservationRepository.deleteById(id);
    }

    // --- Méthodes de Recherche (Lecture Seule) ---

    @Transactional(readOnly = true)
    public List<ReservationResponseDTO> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(this::mapToReservationResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ReservationResponseDTO getReservationById(Long id) {
        return mapToReservationResponseDTO(findReservationEntityById(id));
    }

    @Transactional(readOnly = true)
    public List<ReservationResponseDTO> findReservationsByClientId(Long clientId) {
        if (!clientRepository.existsById(clientId)) {
             throw new EntityNotFoundException("Impossible de trouver le client avec l'ID : " + clientId);
        }
        return reservationRepository.findByClientId(clientId).stream()
                .map(this::mapToReservationResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * NOUVELLE MÉTHODE AJOUTÉE
     * Trouve toutes les réservations pour un utilisateur donné (via son ID utilisateur).
     * @param userId L'identifiant de l'utilisateur.
     * @return Une liste de ReservationResponseDTO.
     */
    @Transactional(readOnly = true)
    public List<ReservationResponseDTO> findReservationsByUserId(Long userId) {
        // La méthode findReservationsByUserId existe dans votre ReservationRepository
        List<Reservation> reservations = reservationRepository.findReservationsByUserId(userId);
        
        // On convertit la liste d'entités en liste de DTOs avant de la retourner
        return reservations.stream()
                .map(this::mapToReservationResponseDTO)
                .collect(Collectors.toList());
    }


    // --- Logique Privée et Mapping ---

    private boolean isVoitureDisponible(Long voitureId, LocalDateTime debut, LocalDateTime fin) {
        // Note: vous n'avez pas fourni le VoitureRepository, mais la logique est ici.
        // Assurez-vous que la méthode findVoituresDisponibles existe dans VoitureRepository.
        // List<Voiture> voituresDisponibles = voitureRepository.findVoituresDisponibles(debut, fin);
        // return voituresDisponibles.stream().anyMatch(v -> v.getId().equals(voitureId));
        return true; // Placeholder
    }

    private Reservation findReservationEntityById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Impossible de trouver la réservation avec l'ID : " + id));
    }
    
    private Voiture findVoitureEntityById(Long id) {
        return voitureRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Impossible de trouver la voiture avec l'ID : " + id));
    }

    private Client findClientEntityById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Impossible de trouver le client avec l'ID : " + id));
    }

    /**
     * Mappe une entité Reservation vers son DTO de réponse complet.
     * @param reservation L'entité à mapper.
     * @return Le DTO correspondant.
     */
    private ReservationResponseDTO mapToReservationResponseDTO(Reservation reservation) {
        ReservationResponseDTO dto = new ReservationResponseDTO();
        dto.setId(reservation.getId());
        dto.setDateDebut(reservation.getDateDebut());
        dto.setDateFin(reservation.getDateFin());
        dto.setMontantTotal(reservation.getMontantTotal());
        dto.setAcompte(reservation.getAcompte());
        dto.setStatut(reservation.getStatut());
        dto.setVoiture(mapToVoitureDetailDTO(reservation.getVoiture()));
        dto.setClient(mapToClientDetailDTO(reservation.getClient()));
        dto.setConducteurSecondaire(mapToClientDetailDTO(reservation.getConducteurSecondaire()));

        return dto;
    }

    /**
     * Mappe une entité Voiture vers son DTO de détail.
     * @param voiture L'entité voiture.
     * @return Le DTO de détail de la voiture.
     */
    private VoitureDetailDTO mapToVoitureDetailDTO(Voiture voiture) {
        if (voiture == null) return null;
        
        VoitureDetailDTO dto = new VoitureDetailDTO();
        dto.setId(voiture.getId());
        dto.setVname(voiture.getVname());
        dto.setCouleur(voiture.getCouleur());
        dto.setMarque(voiture.getMarque());
        dto.setMatricule(voiture.getMatricule());
        dto.setModele(voiture.getModele());
        dto.setCarburant(voiture.getCarburant());
        dto.setCapacite(voiture.getCapacite());
        dto.setType(voiture.getType());
        dto.setPrixDeBase(voiture.getPrixDeBase());
        dto.setEstAutomate(voiture.getEstAutomate());
        
        return dto;
    }
    
    /**
     * Mappe une entité Client vers son DTO de détail.
     * @param client L'entité client.
     * @return Le DTO de détail du client.
     */
    private ClientDetailDTO mapToClientDetailDTO(Client client) {
        if (client == null) return null;

        ClientDetailDTO dto = new ClientDetailDTO();
        dto.setId(client.getId());
        dto.setCname(client.getCname());
        dto.setAdresse(client.getAdresse());
        dto.setNationalite(client.getNationalite());
        dto.setAdresseEtranger(client.getAdresseEtranger());
        dto.setPasseport(client.getPasseport());
        dto.setDelivreLePasseport(client.getDelivreLePasseport());
        dto.setCin(client.getCin());
        dto.setTel(client.getTel());
        dto.setPermis(client.getPermis());
        dto.setCinDelivreLe(client.getCinDelivreLe());
        dto.setPermisDelivreLe(client.getPermisDelivreLe());
        dto.setPermisDelivreAu(client.getPermisDelivreAu());
        
        return dto;
    }
    
    @Transactional
    public ReservationResponseDTO updateReservation(Long id, ReservationRequestDTO dto) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("error.reservation.notfound"));

        // Valider les dates
        if (dto.getDateDebut().isAfter(dto.getDateFin())) {
            throw new IllegalArgumentException("error.dates.invalid");
        }

        // Vérifier la disponibilité de la voiture (en excluant cette réservation)
        List<Voiture> voituresDisponibles = voitureRepository.findVoituresDisponibles(dto.getDateDebut(), dto.getDateFin())
                .stream()
                .filter(v -> v.getId().equals(dto.getVoitureId()) || !isVoitureAlreadyReservedForOther(dto.getVoitureId(), dto.getDateDebut(), dto.getDateFin(), id))
                .collect(Collectors.toList());

        boolean isVoitureDisponible = voituresDisponibles.stream()
                .anyMatch(v -> v.getId().equals(dto.getVoitureId()));

        if (!isVoitureDisponible) {
            throw new IllegalStateException("error.reservation.voiture.notavailable");
        }

        // Récupération des entités liées
        Voiture voiture = voitureRepository.findById(dto.getVoitureId())
                .orElseThrow(() -> new EntityNotFoundException("error.voiture.notfound"));
        Client client = clientService.findClientEntityById(dto.getClientId());
        Client conducteurSecondaire = dto.getConducteurSecondaireId() != null ?
                clientService.findClientEntityById(dto.getConducteurSecondaireId()) : null;

        // Calcul du prix
        long jours = ChronoUnit.DAYS.between(dto.getDateDebut(), dto.getDateFin());
        if (jours == 0) jours = 1;
        float montantTotal = (float) (jours * voiture.getPrixDeBase());

        // Mise à jour des champs
        reservation.setVoiture(voiture);
        reservation.setClient(client);
        reservation.setConducteurSecondaire(conducteurSecondaire);
        reservation.setDateDebut(dto.getDateDebut());
        reservation.setDateFin(dto.getDateFin());
        reservation.setAcompte(dto.getAcompte());
        reservation.setStatut(dto.getStatut());
        reservation.setMontantTotal(montantTotal);

        Reservation updatedReservation = reservationRepository.save(reservation);
        return mapToReservationResponseDTO(updatedReservation);
    }
    public boolean isVoitureAlreadyReservedForOther(Long voitureId, LocalDateTime dateDebut, LocalDateTime dateFin, Long currentReservationId) {
        return reservationRepository.isVoitureAlreadyReservedForOther(voitureId, dateDebut, dateFin, currentReservationId);
    }

}