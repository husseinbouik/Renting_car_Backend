package Car.project.Services;

import Car.project.Entities.Client;
import Car.project.Repositories.ClientRepository;
import Car.project.dto.ClientDTO; // J'utilise ClientDTO comme vous l'avez fait dans le contrôleur
import Car.project.dto.ClientDetailDTO;
import Car.project.exception.ResourceAlreadyExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    // --- MÉTHODES PUBLIQUES APPELÉES PAR LE CONTROLLER ---
    @Transactional(readOnly = true)
    public List<ClientDetailDTO> getAllClientsDto() {
        return clientRepository.findAll().stream()
                .map(this::mapToClientDetailDTO)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public ClientDetailDTO getClientDtoById(Long id) {
        Client client = findClientEntityById(id);
        return mapToClientDetailDTO(client);
    }

    /**
     * NOUVELLE SIGNATURE : Accepte un ClientDTO
     */
    public ClientDetailDTO createClient(ClientDTO dto) throws IOException {
        if (dto.getCin() != null && clientRepository.existsByCin(dto.getCin())) {
            throw new ResourceAlreadyExistsException("error.client.cin.exists");
        }

        Client client = new Client();
        mapDtoToEntity(dto, client); // On mappe le DTO vers l'entité

        Client savedClient = clientRepository.save(client);
        return mapToClientDetailDTO(savedClient); // On retourne un DTO de détail
    }

    /**
     * NOUVELLE SIGNATURE : Accepte un Long et un ClientDTO
     */
    public ClientDetailDTO updateClient(Long id, ClientDTO dto) throws IOException {
        Client existingClient = findClientEntityById(id); // On trouve l'entité existante

        mapDtoToEntity(dto, existingClient); // On met à jour l'entité avec les données du DTO

        Client updatedClient = clientRepository.save(existingClient);
        return mapToClientDetailDTO(updatedClient);
    }

    public void deleteClient(Long id) {
        if (!clientRepository.existsById(id)) {
            throw new EntityNotFoundException("error.client.notfound");
        }
        clientRepository.deleteById(id);
    }


    // --- MÉTHODES INTERNES ET DE MAPPING ---

    Client findClientEntityById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("error.client.notfound"));
    }
    public byte[] getClientCinPhotoById(Long id) {
        Client client = findClientEntityById(id);
        if (client.getPhotoCIN() == null) {
            throw new EntityNotFoundException("error.client.photocin.notfound");
        }
        return client.getPhotoCIN();
    }

    public byte[] getClientPermisPhotoById(Long id) {
        Client client = findClientEntityById(id);
        if (client.getPhotoPermis() == null) {
            throw new EntityNotFoundException("error.client.photopermis.notfound");
        }
        return client.getPhotoPermis();
    }

    /**
     * Méthode d'aide pour mapper un DTO de requête vers une entité Client.
     */
    private void mapDtoToEntity(ClientDTO dto, Client client) throws IOException {
        client.setCname(dto.getCname());
        client.setAdresse(dto.getAdresse());
        client.setNationalite(dto.getNationalite());
        client.setAdresseEtranger(dto.getAdresseEtranger());
        client.setPasseport(dto.getPasseport());
        client.setDelivreLePasseport(dto.getDelivreLePasseport());
        client.setCin(dto.getCin());
        client.setCinDelivreLe(dto.getCinDelivreLe());
        client.setTel(dto.getTel());
        client.setPermis(dto.getPermis());
        client.setPermisDelivreLe(dto.getPermisDelivreLe());
        client.setPermisDelivreAu(dto.getPermisDelivreAu());

        if (dto.getPhotoCIN() != null && !dto.getPhotoCIN().isEmpty()) {
            client.setPhotoCIN(dto.getPhotoCIN().getBytes());
        }
        if (dto.getPhotoPermis() != null && !dto.getPhotoPermis().isEmpty()) {
            client.setPhotoPermis(dto.getPhotoPermis().getBytes());
        }
    }
    
    /**
     * Méthode d'aide pour mapper une entité Client vers un DTO de réponse.
     */
    private ClientDetailDTO mapToClientDetailDTO(Client client) {
        if (client == null) {
            return null;
        }
        ClientDetailDTO dto = new ClientDetailDTO();
        dto.setId(client.getId());
        dto.setCname(client.getCname());
        dto.setAdresse(client.getAdresse());
        dto.setNationalite(client.getNationalite());
        dto.setAdresseEtranger(client.getAdresseEtranger());
        dto.setPasseport(client.getPasseport());
        dto.setDelivreLePasseport(client.getDelivreLePasseport());
        dto.setCin(client.getCin());
        dto.setCinDelivreLe(client.getCinDelivreLe());
        dto.setTel(client.getTel());
        dto.setPermis(client.getPermis());
        dto.setPermisDelivreLe(client.getPermisDelivreLe());
        dto.setPermisDelivreAu(client.getPermisDelivreAu());
        return dto;
    }
}