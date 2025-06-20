package Car.project.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import Car.project.Entities.Client;
import java.util.Optional;

@Repository
public interface ClientRepository  extends JpaRepository<Client, Long> {
	boolean existsByCin(String cin);
	@Query("SELECT c.id FROM Client c WHERE c.user.id = :userId")
	Long findClientIdByUserId(@Param("userId") Long userId);

}

