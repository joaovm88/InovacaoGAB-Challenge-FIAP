package br.com.fiap.inovagab.repository;

import br.com.fiap.inovagab.model.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Optional;

public interface UsuarioRepository extends MongoRepository<Usuario, String> {
    UserDetails findByEmail(String email);
    Optional<Usuario> findUsuarioByEmail(String email);
}