package br.com.fiap.inovagab.controller;

import br.com.fiap.inovagab.dto.LoginDTO;
import br.com.fiap.inovagab.dto.TokenDTO;
import br.com.fiap.inovagab.model.Usuario;
import br.com.fiap.inovagab.repository.UsuarioRepository;
import br.com.fiap.inovagab.security.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/registro")
    public ResponseEntity<Usuario> registrar(@RequestBody Usuario usuario) {
        if (usuarioRepository.findByEmail(usuario.getEmail()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        // Criptografa a senha antes de persistir no MongoDB
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        Usuario novoUsuario = usuarioRepository.save(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoUsuario);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenDTO> login(@RequestBody LoginDTO dadosLogin) {
        var authToken = new UsernamePasswordAuthenticationToken(dadosLogin.email(), dadosLogin.senha());
        var autenticacao = authenticationManager.authenticate(authToken);

        Usuario usuarioAutenticado = (Usuario) autenticacao.getPrincipal();
        String jwt = tokenService.gerarToken(usuarioAutenticado);

        return ResponseEntity.ok(new TokenDTO(jwt, "Bearer", usuarioAutenticado.getNivelAcesso().name()));
    }
}